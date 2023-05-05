package com.ambrosia;

import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class Cursor {
	private static long window;
	
	private static final double[] mouseX = new double[1];
	private static final double[] mouseY = new double[1];
	
	private static final double[] previousMouseX = new double[1];
	private static final double[] previousMouseY = new double[1];
	
	private static final Vector2f mouseDXY = new Vector2f();
	
	public static void init(long aWindow) {
		window = aWindow;
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		if(glfwRawMouseMotionSupported()) {
			glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
		}
		glfwGetCursorPos(window, mouseX, mouseY);
	}
	
	public static void update() {
		previousMouseX[0] = mouseX[0];
		previousMouseY[0] = mouseY[0];
		
		glfwGetCursorPos(window, mouseX, mouseY);
		
		mouseDXY.x = (float) (mouseX[0] - previousMouseX[0]);
		mouseDXY.y = (float) (mouseY[0] - previousMouseY[0]);
	}
	
	public static double getMouseX() {
		return mouseX[0];
	}
	public static double getMouseY() {
		return mouseY[0];
	}
	
	public static double getPreviousMouseX() {
		return previousMouseX[0];
	}
	public static double getPreviousMouseY() {
		return previousMouseY[0];
	}
	
	public static Vector2f getMouseDXY() {
		return mouseDXY;
	}
}
