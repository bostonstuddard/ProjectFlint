package dev.projectflint.block;

import dev.projectflint.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Transparent structural wall piece restricted to the middle two wall layers. */
public final class WattledDaubWindowBlock extends WattledDaubBricksBlock {
	public WattledDaubWindowBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		return isValidWindowPosition(context.getLevel(), pos) ? defaultBlockState() : null;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return isValidWindowPosition(level, pos);
	}

	/** Hide the otherwise-visible internal cube face between adjacent windows. */
	@Override
	protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
		if (adjacentState.is(ModBlocks.WATTLED_DAUB_WINDOW)) return true;
		return super.skipRendering(state, adjacentState, direction);
	}

	private static boolean isValidWindowPosition(LevelReader level, BlockPos pos) {
		// Windows must physically sit on a normal Wattled Daub Brick; they cannot
		// float from roof support, stack directly on another window, or sit on a door.
		if (!level.getBlockState(pos.below()).is(ModBlocks.WATTLED_DAUB_BRICKS)) return false;
		int layer = WattledDaubBricksBlock.verticalStructuralLayer(level, pos);
		return layer == 2 || layer == 3;
	}
}
