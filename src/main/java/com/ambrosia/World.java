package com.ambrosia;

import org.joml.Vector3i;
import org.lwjgl.BufferUtils;
import org.spongepowered.noise.LatticeOrientation;
import org.spongepowered.noise.Noise;
import org.spongepowered.noise.NoiseQualitySimplex;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class World {
	public static final class ID {
		public static final int AIR = 0;
		public static final int GRASS = 1;
		public static final int STONE = 2;
		public static final int WATER = 3;
		public static final int LOG = 4;
		public static final int LEAVES = 5;
	}
	
	public final int[][][] blocks;
	public final int size;
	public final Random random;
	public final int seed;
	
	// Internal variables
	private final ArrayList<Vector3i> surfaceBlocks;
	
	private final Camera camera;
	
	private IntBuffer data;
	private boolean shouldUpdate;
	
	public World(int size, Camera camera) {
		this.size = size;
		
		blocks = new int[this.size][this.size][this.size];
		random = new Random();
		seed = random.nextInt();
		
		surfaceBlocks = new ArrayList<>();
		
		this.camera = camera;
		
		data = null;
		shouldUpdate = true;
	}
	
	public static float counter = 0.0f;

	private void generateFirstPass(int x, int y, int z) {
		blocks[x][y][z] = ID.AIR;
		
		float worldX = x + camera.pos.x;
		float worldY = y + camera.pos.y;
		float worldZ = z + camera.pos.z;
		
		int baseGrassLevel = 80;
		baseGrassLevel += (int) (
			Noise.simplexStyleGradientCoherentNoise3D(
				worldX * 0.05, 1.0, worldZ * 0.05, 
				this.seed, 
				LatticeOrientation.XZ_BEFORE_Y, NoiseQualitySimplex.SMOOTH
			) * 10.0 - 5.0
		);
		
		

		if(y < baseGrassLevel) {
			blocks[x][y][z] = ID.GRASS;
			return;
		}
		if(y < 80) {
			blocks[x][y - 1][z] = ID.WATER;
		}
	}
	
	private void markSurfaceBlocks(int x, int y, int z) {
		try {
			if(blocks[x][y + 1][z] == ID.AIR && blocks[x][y][z] == ID.GRASS) {
				surfaceBlocks.add(new Vector3i(x, y, z));
			}
		} catch(ArrayIndexOutOfBoundsException ignored) {
			
		}
	}
	
	private void generateTrees() {
		for(Vector3i pos : surfaceBlocks) {
			if(random.nextDouble() < 0.99) continue;
			
			final int treeHeight = random.nextInt(4, 12);
			
			// tree trunk
			for(int i = 1; i < treeHeight; i++) {
				try {
					blocks[pos.x][pos.y + i][pos.z] = ID.LOG;
					
					if(i > treeHeight - 3) {
						for(int x = -1; x <= 1; x++) {
							for(int z = -1; z <= 1; z++) {
								blocks[pos.x + x][pos.y + i][pos.z + z] = ID.LEAVES;
							}
						}
					}
				} catch(ArrayIndexOutOfBoundsException ignored) {
					
				}
			}
			
			
		}
	}
	
	public void clear() {
		for (int[][] block : blocks) {
			for (int[] ints : block) {
				Arrays.fill(ints, ID.AIR);
			}
		}
	}
	
	public void generate() {
		for(int i = 0; i < blocks.length; i++) {
			for(int j = 0; j < blocks[i].length; j++) {
				for(int k = 0; k < blocks[i][j].length; k++) {
					generateFirstPass(i, j, k);
				}
			}
		}
		
//		for(int i = 0; i < blocks.length; i++) {
//			for(int j = 0; j < blocks[i].length; j++) {
//				for(int k = 0; k < blocks[i][j].length; k++) {
//					markSurfaceBlocks(i, j, k);
//				}
//			}
//		}
//		
//		generateTrees();
	}
	
	public IntBuffer getData() {
		if(!shouldUpdate) {
			//return this.data;
		}
		
		IntBuffer buffer = BufferUtils.createIntBuffer(this.size * this.size * this.size);
		
		for(int x = 0; x < this.size; x++) {
			for(int y = 0; y < this.size; y++) {
				for(int z = 0; z < this.size; z++) {
					buffer.put(blocks[x][y][z]);
				}
			}
		}
		
		buffer.flip();
		//buffer.position(0);
		
		// We just updated
		shouldUpdate = false;
		return data = buffer;
	}
}
