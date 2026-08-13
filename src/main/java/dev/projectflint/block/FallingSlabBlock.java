package dev.projectflint.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Slab variant with the same gravity behavior as sand/gravel-style blocks. */
public final class FallingSlabBlock extends SlabBlock {
	public FallingSlabBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		level.scheduleTick(pos, this, 2);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTicks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		scheduledTicks.scheduleTick(pos, this, 2);
		return super.updateShape(state, level, scheduledTicks, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (FallingBlock.isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinY()) {
			BlockState fallingState = state;
			if (state.getValue(TYPE) == SlabType.TOP) {
				fallingState = state.setValue(TYPE, SlabType.BOTTOM);
			}
			FallingBlockEntity.fall(level, pos, fallingState);
		}
	}
}
