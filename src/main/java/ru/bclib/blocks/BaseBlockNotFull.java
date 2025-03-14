package ru.bclib.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class BaseBlockNotFull extends BaseBlock {

	public BaseBlockNotFull(Properties settings) {
		super(settings);
	}

	public boolean canSuffocate(BlockState state, BlockGetter view, BlockPos pos) {
		return false;
	}

	public boolean isSimpleFullBlock(BlockState state, BlockGetter view, BlockPos pos) {
		return false;
	}

	public boolean allowsSpawning(BlockState state, BlockGetter view, BlockPos pos, EntityType<?> type) {
		return false;
	}
}
