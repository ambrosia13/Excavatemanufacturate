package com.ambrosia;

import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {
	private float pitch;
	private float yaw;
	
	public final Vector3f cameraPosition;
	
	public Camera() {
		pitch = 0.0f;
		yaw = 0.0f;
		
		cameraPosition = new Vector3f(0.0f, 90.0f, 0.0f);
	}
	
	public Vector3f getForwardVector() {
		Vector3f forward = new Vector3f();
		forward.z = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		forward.y = Math.sin(Math.toRadians(pitch));
		forward.x = Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		forward.normalize();
		
		return forward;
	}
	public Vector3f getForwardVectorXZ() {
		var forward = getForwardVector();
		forward.y = 0.0f;
		
		return forward.normalize();
	}
	
	public Vector3f getRightVector(Vector3f forwardVector) {
		Vector3f right = new Vector3f(0.0f, 0.0f, 0.0f);
		forwardVector.cross(new Vector3f(0.0f, 1.0f, 0.0f), right);
		right.mul(-1.0f);
		right.normalize();
		
		return right;
	}
	
	public void updatePitchAndYaw(float amount) {
		Vector2f mouseDXY = Cursor.getMouseDXY();
		
		pitch -= mouseDXY.y * amount;
		yaw += mouseDXY.x * amount;
		
		pitch = Math.clamp(-89.99f, 89.99f, pitch);
	}
	
	public void translatePos(Vector3f vector, float mag) {
		final float[] originalValues = new float[] {vector.x, vector.y, vector.z};
		cameraPosition.add(vector.mul(mag));
		
		vector.x = originalValues[0];
		vector.y = originalValues[1];
		vector.z = originalValues[2];
	}
	public void translatePos(Vector3f vector) {
		translatePos(vector, 1);
	}
	public void translatePos(float x, float y, float z) {
		translatePos(new Vector3f(x, y, z));
	}
	
	public Vector3f getPos() {
		return cameraPosition;
	}
	public float getPitch() {
		return pitch;
	}
	public float getYaw() {
		return yaw;
	}
}
