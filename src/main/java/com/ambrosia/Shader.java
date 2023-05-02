package com.ambrosia;

import java.io.IOException;
import java.io.InputStream;

import static org.lwjgl.opengl.GL46C.*;

public class Shader {
	public enum Type {
		VERTEX, 
		FRAGMENT
	}
	
	private int id;
	public String source;
	
	public Shader(String path, int type) {
		StringBuilder src = new StringBuilder();
		source = ShaderParsingUtils.readShaderSource(path);
		source = ShaderParsingUtils.resolveIncludes(source);
		
		id = glCreateShader(type);
		
		glShaderSource(id, source);
	}
	
	public Shader(String path, Type type) {
		this(path, type == Type.VERTEX ? GL_VERTEX_SHADER : GL_FRAGMENT_SHADER);
	}
	
	public Shader(String path) {
		this(path, path.substring(path.lastIndexOf('.') + 1).equals("vert") ? Type.VERTEX : Type.FRAGMENT);
	}
	
	public Shader compile() {
		glCompileShader(id);
		
		if(glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
			String error = glGetShaderInfoLog(id);
			int secondColumnIndex = error.indexOf(':', error.indexOf(':') + 1);
			int thirdColumnIndex = error.indexOf(':', secondColumnIndex + 1);
			int lineNumber = Integer.parseInt(error.substring(secondColumnIndex + 1, thirdColumnIndex));
			
			System.err.println("Relevant code for the error:");
			System.err.println("----------------------------");
			System.err.println(ShaderParsingUtils.getLines(source, lineNumber - 2, lineNumber + 2));
			System.err.println("----------------------------");
			
			System.err.println(glGetShaderInfoLog(id));
		}
		
		return this;
	}
	
	public Shader attachToProgram(int programId) {
		glAttachShader(programId, this.id);
		return this;
	}
}
