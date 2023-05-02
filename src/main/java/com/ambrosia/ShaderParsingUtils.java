package com.ambrosia;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ShaderParsingUtils {
	public static String readShaderSource(String location) {
		try {
			InputStream stream = ShaderParsingUtils.class.getResourceAsStream(location);
			
			StringBuilder string = new StringBuilder();
			
			for (int ch; (ch = stream.read()) != -1; ) {
				string.append((char) ch);
			}
			
			return string.toString();
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String resolveIncludes(String source) {
		int beginIndex = source.indexOf("#include ");;
		
		while (beginIndex != -1) {
			int quoteBegin = source.indexOf("\"", beginIndex) + 1;
			int quoteEnd = source.indexOf("\"", quoteBegin);
			
			String includePath = source.substring(quoteBegin, quoteEnd);
			
			String includeSource = readShaderSource("/" + includePath);
			source = source.substring(0, beginIndex) + includeSource + source.substring(quoteEnd + 1);
			
			beginIndex = source.indexOf("#include ");
		}
		
		return source;
	}
	
	public static String getLines(String source, int begin, int end) {
		ArrayList<Integer> newlineIndices = new ArrayList<>();
		int index = 0;
		
		while((index != -1 && index < source.length()) || newlineIndices.size() < end - begin + 1) {
			newlineIndices.add(index);
			index = source.indexOf("\n", index + 1);
		}
		
		if(begin >= newlineIndices.size()) {
			throw new IllegalArgumentException("Tried to query line number that isn't in source string");
		}
		
		return source.substring(newlineIndices.get(begin), newlineIndices.get(end));
	}
}
