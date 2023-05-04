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
	
	private double[] mouseX;
	private double[] mouseY;
	private double[] previousMouseX;
	private double[] previousMouseY;
	private Vector2f mouseDXY;
	
	// Uniforms..
	
	// u_pitch
	public float pitch;
	
	// u_yaw
	public float yaw;
	
	// u_cameraPosition
	public Vector3f cameraPosition;
	private double movementSpeed = 0.25;
	
	// u_resolution
	public int width = 800;
	public int height = 600;
	
	// u_randomSeed
	public final int randomSeed = new java.util.Random().nextInt();
	
	// todo: consolidate these into separate classes
	private Vector3f getForwardVector(float pitch, float yaw) {
		Vector3f forward = new Vector3f();
		forward.z = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		forward.y = Math.sin(Math.toRadians(pitch));
		forward.x = Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		forward.normalize();
		
		return forward;
	}
	private Vector3f getForwardVectorXZ(float pitch, float yaw) {
		var forward = getForwardVector(pitch, yaw);
		forward.y = 0.0f;
		
		return forward.normalize();
	}
	
	// todo: consolidate these into classes
	private Vector3f getRightVector(Vector3f forward) {
		Vector3f right = new Vector3f(0.0f, 0.0f, 0.0f);
		forward.cross(new Vector3f(0.0f, 1.0f, 0.0f), right);
		right.mul(-1.0f);
		right.normalize();
		
		return right;
	}
	
	// Initialize glfw window
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
		
		mouseX = new double[1];
		mouseY = new double[1];
		previousMouseX = new double[1];
		previousMouseY = new double[1];
		mouseDXY = new Vector2f();
		
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		if(glfwRawMouseMotionSupported()) {
			glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
		}
		glfwGetCursorPos(window, mouseX, mouseY);
		
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(window, true);
				//glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
			} else {
//				if(key == GLFW_KEY_UP) pitch += 5.0;
//				if(key == GLFW_KEY_DOWN) pitch -= 5.0;
//				if(key == GLFW_KEY_RIGHT) yaw += 5.0;
//				if(key == GLFW_KEY_LEFT) yaw -= 5.0;
				
				pitch = org.joml.Math.clamp(-90.0f, 90.0f, pitch);
				
				Vector3f forward = getForwardVectorXZ(pitch, yaw);
				Vector3f right = getRightVector(forward);
				
				if(key == GLFW_KEY_LEFT_CONTROL) {
					if(movementSpeed == 0.25) movementSpeed = 2.0;
					else if(movementSpeed == 3.0) movementSpeed = 0.25;
				}
				
				forward.mul((float) movementSpeed);
				right.mul((float) movementSpeed);
				
				if(key == GLFW_KEY_W) cameraPosition.add(forward);
				if(key == GLFW_KEY_S) cameraPosition.sub(forward);
				
				if(key == GLFW_KEY_A) cameraPosition.sub(right);
				if(key == GLFW_KEY_D) cameraPosition.add(right);
				if(key == GLFW_KEY_SPACE) cameraPosition.y += movementSpeed;
				if(key == GLFW_KEY_LEFT_SHIFT) cameraPosition.y -= movementSpeed;
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
			previousMouseX[0] = mouseX[0];
			previousMouseY[0] = mouseY[0];
			glfwGetCursorPos(window, mouseX, mouseY);
			
			mouseDXY.x = (float) (mouseX[0] - previousMouseX[0]);
			mouseDXY.y = (float) (mouseY[0] - previousMouseY[0]);
			
			pitch -= mouseDXY.y;
			yaw += mouseDXY.x;
			
			pitch = Math.clamp(-90.0f, 90.0f, pitch);
			
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glUseProgram(program);
			
			// Uniform uploading
			glUniform2i(0, width, height);
			glUniform1f(1, pitch);
			glUniform1f(2, yaw);
			glUniform3f(3, cameraPosition.x, cameraPosition.y, cameraPosition.z);
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