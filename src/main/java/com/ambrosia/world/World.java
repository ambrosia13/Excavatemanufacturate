package com.ambrosia.world;

import org.joml.Vector3i;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {
	public List<Chunk> chunks;
	
	/**
	 * View distance in chunks
	 */
	public int viewDistance;
	
	private final Random random;
	public final int seed;
	
	private IntBuffer data;
	
	public World(int viewDistance) {
		this.chunks = new ArrayList<>();
		this.viewDistance = viewDistance;
		
		this.random = new Random();
		this.seed = random.nextInt();
		
		this.data = null;
		
		fill();
	}
	
	/**
	 * Fills the world with empty chunks
	 */
	public void fill() {
		for(int i = -viewDistance; i <= viewDistance; i++) {
			for(int j = -viewDistance; j <= viewDistance; j++) {
				for(int k = -viewDistance; k <= viewDistance; k++) {
					chunks.add(new Chunk(new Vector3i(i, j, k)));
				}
			}
		}
	}
	
	public void generate() {
		for(Chunk chunk : chunks) {
			chunk.generate(this.seed);
		}
	}
	
	public IntBuffer asIntBuffer() {
		if(data != null) return data;
		
		int size = (viewDistance * 2 + 1) * 16 * 16 * 16 + 1;
		IntBuffer buffer = BufferUtils.createIntBuffer(size * size * size);
		
		for(Chunk chunk : chunks) {
			buffer.put(chunk.asIntBuffer());
		}
		
		buffer.position(0);
		return data = buffer;
	}
}
