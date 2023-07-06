package com.ambrosia;

import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class InputHandler {
	private final Camera camera;
	private final long window;
	
	public InputHandler(Camera camera, long window) {
		this.camera = camera;
		this.window = window;
		
		glfwSetKeyCallback(window, (wndw, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(wndw, true);
			}
		});
	}
	
	public float handleInput(float movementSpeed) {
		Vector3f forward = camera.getForwardVectorXZ().mul(movementSpeed);
		Vector3f right = camera.getRightVector(forward).mul(movementSpeed);
		
		if(glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) movementSpeed = 3.0f;
		else if(glfwGetKey(window, GLFW_KEY_LEFT_ALT) == GLFW_PRESS) movementSpeed = 0.025f;
		else movementSpeed = 0.25f;
		
		if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) camera.translatePos(forward, 1.0f);
		if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) camera.translatePos(forward, -1.0f);
		if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) camera.translatePos(right, 1.0f);
		if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) camera.translatePos(right, -1.0f);
		if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) camera.translatePos(0.0f, movementSpeed, 0.0f);
		if(glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) camera.translatePos(0.0f, -movementSpeed, 0.0f);
	
		return movementSpeed;
	}
}
