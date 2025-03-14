package ru.bclib.world.generator;

import java.util.Random;

import ru.bclib.world.biomes.BCLBiome;

public class BiomeChunk {
	protected static final int WIDTH = 16;
	private static final int SM_WIDTH = WIDTH >> 1;
	private static final int MASK_OFFSET = SM_WIDTH - 1;
	protected static final int MASK_WIDTH = WIDTH - 1;

	private final BCLBiome[][] biomes;

	public BiomeChunk(BiomeMap map, Random random, BiomePicker picker) {
		BCLBiome[][] PreBio = new BCLBiome[SM_WIDTH][SM_WIDTH];
		biomes = new BCLBiome[WIDTH][WIDTH];

		for (int x = 0; x < SM_WIDTH; x++)
			for (int z = 0; z < SM_WIDTH; z++)
				PreBio[x][z] = picker.getBiome(random);

		for (int x = 0; x < WIDTH; x++)
			for (int z = 0; z < WIDTH; z++)
				biomes[x][z] = PreBio[offsetXZ(x, random)][offsetXZ(z, random)].getSubBiome(random);
	}

	public BCLBiome getBiome(int x, int z) {
		return biomes[x & MASK_WIDTH][z & MASK_WIDTH];
	}

	private int offsetXZ(int x, Random random) {
		return ((x + random.nextInt(2)) >> 1) & MASK_OFFSET;
	}
}
