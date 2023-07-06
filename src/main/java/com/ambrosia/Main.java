package com.ambrosia;

import com.ambrosia.world.World;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46C.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
	public long window;
	
	public Camera camera;
	private float movementSpeed = 0.25f;
	
	public World world;
	
	public InputHandler inputHandler;
	
	// u_resolution
	public int width = 800;
	public int height = 600;
	
	// u_randomSeed
	public final int randomSeed = new java.util.Random().nextInt();
	
	// Initialize glfw window
	private void init() {
		GLFWErrorCallback.createPrint(System.err).set();
		
		if(!glfwInit()) {
			throw new IllegalStateException("Unable to initialize glfw");
		}
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		
		window = glfwCreateWindow(width, height, "Excavatemanufacturate", NULL, NULL);
		if(window == NULL) {
			throw new RuntimeException("Unable to create window");
		}
		
		Cursor.init(window);
		camera = new Camera();
		world = new World(2);
		world.generate();
		inputHandler = new InputHandler(camera, window);
		
		try(MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			
			glfwGetWindowSize(window, pWidth, pHeight);
			
			GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			
			glfwSetWindowPos(
				window,
				(vidMode.width() - pWidth.get(0)) / 2,
				(vidMode.height() - pHeight.get(0)) / 2
			);
		}
		
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		
		glfwSwapInterval(1); // vsync
		
		glfwShowWindow(window);
	}
	
	public void loop() {
		glClearColor(1, 0, 0, 0);
		
		int program = glCreateProgram();
		
		// Vertex shader
		new Shader("/main.vert")
			.compile()
			.attachToProgram(program);
		
		// Fragment shader
		new Shader("/main.frag")
			.compile()
			.attachToProgram(program);
		
		glLinkProgram(program);
		
		final float[] vertices = {
			-1.0f,  1.0f, 0.0f,
			-1.0f, -1.0f, 0.0f,
			1.0f, -1.0f, 0.0f,
			1.0f,  1.0f, 0.0f
		};
		
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);
		
		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
		
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(0);
		
		while(!glfwWindowShouldClose(window)) {
			Cursor.update();
			camera.updatePitchAndYaw(1.0f); // looking around
			movementSpeed = inputHandler.handleInput(movementSpeed); // moving around
			
			// Generating world texture
			int worldTextureId = glGenTextures();
			glBindTexture(GL_TEXTURE_3D, worldTextureId);
			
			glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			
			final int size = world.viewDistance * 2 + 1;
			glTexImage3D(GL_TEXTURE_3D, 0, GL_R8UI, size, size, size, 0, GL_RED_INTEGER, GL_UNSIGNED_INT, world.asIntBuffer());
			int textureUnit = 5;
			glActiveTexture(GL_TEXTURE0 + textureUnit);
			glBindTexture(GL_TEXTURE_3D, worldTextureId);
			
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glUseProgram(program);
			
			// Uniform uploading
			glUniform2i(0, width, height);
			glUniform1f(1, camera.getPitch());
			glUniform1f(2, camera.getYaw());
			glUniform3f(3, camera.pos.x, camera.pos.y, camera.pos.z);
			glUniform1i(4, randomSeed); // random seed to use in shader
			glUniform1i(5, textureUnit);
			
			glBindVertexArray(vao);
			glDrawArrays(GL_QUADS, 0, 4);
			
			glfwSwapBuffers(window);
			glfwPollEvents();
			
			glDeleteTextures(worldTextureId);
		}
	}
	
	public void run() {
		System.out.println("Running");
		
		init();
		loop();
		
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	public static void main(String[] args) {
		try {
			new Main().run();
		} catch(Throwable throwable) {
			throwable.printStackTrace();
		}
	}
}