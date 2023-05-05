package com.ambrosia;

import org.joml.*;

import org.joml.Math;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46C.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
	public long window;
	
	public Camera camera;
	private float movementSpeed = 4.25f;
	
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
		
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(window, true);
				//glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
			} else {
				Vector3f forward = camera.getForwardVectorXZ();
				Vector3f right = camera.getRightVector(forward);
				
				if(key == GLFW_KEY_LEFT_CONTROL) {
					if(movementSpeed == 0.25f) movementSpeed = 300.0f;
					else if(movementSpeed == 300.0f) movementSpeed = 0.25f;
				}
				
				forward.mul(movementSpeed);
				right.mul(movementSpeed);
				
				if(key == GLFW_KEY_W) camera.translatePos(forward, 1.0f);
				if(key == GLFW_KEY_S) camera.translatePos(forward, -1.0f);
				
				if(key == GLFW_KEY_D) camera.translatePos(right, 1.0f);
				if(key == GLFW_KEY_A) camera.translatePos(right, -1.0f);

				if(key == GLFW_KEY_SPACE) camera.translatePos(0.0f, movementSpeed, 0.0f);
				if(key == GLFW_KEY_LEFT_SHIFT) camera.translatePos(0.0f, -movementSpeed, 0.0f);
			}
		});
		
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
			camera.updatePitchAndYaw(1.0f);
			
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glUseProgram(program);
			
			// Uniform uploading
			glUniform2i(0, width, height);
			glUniform1f(1, camera.getPitch());
			glUniform1f(2, camera.getYaw());
			glUniform3f(3, camera.getPos().x, camera.getPos().y, camera.getPos().z);
			glUniform1i(4, randomSeed); // random seed to use in shader
			
			glBindVertexArray(vao);
			glDrawArrays(GL_QUADS, 0, 4);
			
			glfwSwapBuffers(window);
			glfwPollEvents();
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