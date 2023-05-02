package com.ambrosia;

import org.joml.*;

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
	private static int width = 800;
	private static int height = 600;
	
	private long window;
	
	private float pitch;
	private float yaw;
	
	private Vector3f cameraPosition;
	
	private void init() {
		GLFWErrorCallback.createPrint(System.err).set();
		
		if(!glfwInit()) {
			throw new IllegalStateException("Unable to initialize glfw");
		}
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		
		window = glfwCreateWindow(width, height, "Application", NULL, NULL);
		if(window == NULL) {
			throw new RuntimeException("Unable to create window");
		}
		
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(window, true);
			} else if(true) {
				if(key == GLFW_KEY_UP) pitch += 0.1;
				if(key == GLFW_KEY_DOWN) pitch -= 0.1;
				if(key == GLFW_KEY_RIGHT) yaw += 0.1;
				if(key == GLFW_KEY_LEFT) yaw -= 0.1;
				
				if(key == GLFW_KEY_W) cameraPosition.x += 0.25;
				if(key == GLFW_KEY_S) cameraPosition.x -= 0.25;
				if(key == GLFW_KEY_A) cameraPosition.z += 0.25;
				if(key == GLFW_KEY_D) cameraPosition.z -= 0.25;
				if(key == GLFW_KEY_SPACE) cameraPosition.y += 0.25;
				if(key == GLFW_KEY_LEFT_SHIFT) cameraPosition.y -= 0.25;
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
		
		pitch = 0.0f;
		yaw = 0.0f;
		
		cameraPosition = new Vector3f(0.0f, 85.0f, 0.0f);
	}
	
	public void loop() {
		glClearColor(1, 0, 0, 0);
		
		int program = glCreateProgram();
		
		Shader vertexShader = new Shader("/main.vert")
			.compile()
			.attachToProgram(program);
		
		Shader fragmentShader = new Shader("/main.frag")
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
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			glUseProgram(program);
			
			// Uniform uploading
			glUniform2i(0, width, height);
			glUniform1f(1, pitch);
			glUniform1f(2, yaw);
			glUniform3f(3, cameraPosition.x, cameraPosition.y, cameraPosition.z);
			
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