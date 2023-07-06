package com.ambrosia.world;

import com.ambrosia.World;
import org.joml.Vector3i;
import org.lwjgl.BufferUtils;
import org.spongepowered.noise.LatticeOrientation;
import org.spongepowered.noise.Noise;
import org.spongepowered.noise.NoiseQualitySimplex;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

public class Chunk {
	public final int[][][] data;
	public final Vector3i offset;
	
	private ArrayList<Vector3i> surfaceBlocks;
	
	public Chunk(Vector3i offset) {
		data = new int[16][16][16];
		
		this.offset = offset;
		surfaceBlocks = new ArrayList<>();
	}
	
	/**
	 * Does the first pass of world generation, generating the grass and water landscape.
	 * @param seed the seed to use; should be the same for all chunks generated
	 */
	private void generateFirstPass(int x, int y, int z, int seed) {
		float posX = x + offset.x;
		float posY = x + offset.y;
		float posZ = x + offset.z;
		
		int baseGrassLevel = 8;
		baseGrassLevel += (int) (
			Noise.simplexStyleGradientCoherentNoise3D(
				posX * 0.05, 1.0, posZ * 0.05,
				seed,
				LatticeOrientation.XZ_BEFORE_Y, NoiseQualitySimplex.STANDARD
			) * 10.0 - 5.0
		);
		
		if(y < baseGrassLevel) {
			data[x][y][z] = BlockID.GRASS;
			return;
		}
		if(y < 80) {
			data[x][y - 1][z] = BlockID.WATER;
		}
	}
	
	/**
	 * Marks the surface blocks for trees to be placed on.
	 */
	private void generateSecondPass(int x, int y, int z) {
		try {
			if(data[x][y + 1][z] == BlockID.AIR && data[x][y][z] == BlockID.GRASS) {
				surfaceBlocks.add(new Vector3i(x, y, z));
			}
		} catch(ArrayIndexOutOfBoundsException ignored) {
			
		}
	}
	
	private void generateThirdPass() {
		// todo: shared random
		Random random = new Random();
		
		for(Vector3i pos : surfaceBlocks) {
			if(random.nextDouble() < 0.99) continue;
			
			final int treeHeight = random.nextInt(4, 12);
			
			// tree trunk
			for(int i = 1; i < treeHeight; i++) {
				try {
					data[pos.x][pos.y + i][pos.z] = BlockID.LOG;
					
					if(i > treeHeight - 3) {
						for(int x = -1; x <= 1; x++) {
							for(int z = -1; z <= 1; z++) {
								data[pos.x + x][pos.y + i][pos.z + z] = BlockID.LEAVES;
							}
						}
					}
				} catch(ArrayIndexOutOfBoundsException ignored) {
					
				}
			}
		}
	}
	
	public void generate(int seed) {
		for(int i = 0; i < 16; i++) {
			for(int j = 0; j < 16; j++) {
				for(int k = 0; k < 16; k++) {
					generateFirstPass(i, j, k, seed);
				}
			}
		}
		
		for(int i = 0; i < 16; i++) {
			for(int j = 0; j < 16; j++) {
				for(int k = 0; k < 16; k++) {
					generateSecondPass(i, j, k);
				}
			}
		}
		
		for(int i = 0; i < 16; i++) {
			for(int j = 0; j < 16; j++) {
				for(int k = 0; k < 16; k++) {
					generateThirdPass();
				}
			}
		}
	}
	
	public IntBuffer asIntBuffer() {
		IntBuffer buffer = BufferUtils.createIntBuffer(16 * 16 * 16);
		
		for(int x = 0; x < 16; x++) {
			for(int y = 0; y < 16; y++) {
				for(int z = 0; z < 16; z++) {
					buffer.put(data[x][y][z]);
				}
			}
		}
		
		buffer.position(0);
		return buffer;
	}
}
