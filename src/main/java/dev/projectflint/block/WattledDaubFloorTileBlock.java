package dev.projectflint.block;

import dev.projectflint.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Permanent interior flooring made from fired wattled daub.
 *
 * It is intentionally a full support surface so beds/workstations may sit on it,
 * but it cannot be stacked with itself or used as a foundation for structural
 * wattled-daub wall pieces. The door is the one explicit structural exception.
 */
public final class WattledDaubFloorTileBlock extends Block {
	public WattledDaubFloorTileBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		BlockPos below = pos.below();
		BlockState support = context.getLevel().getBlockState(below);
		if (support.is(ModBlocks.WATTLED_DAUB_BRICKS)
			|| support.is(ModBlocks.WATTLED_DAUB_WINDOW)
			|| support.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)
			|| support.is(ModBlocks.WATTLED_DAUB_DOOR)) {
			return null;
		}
		return support.isFaceSturdy(context.getLevel(), below, Direction.UP) ? defaultBlockState() : null;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		BlockState support = level.getBlockState(below);
		return !support.is(ModBlocks.WATTLED_DAUB_BRICKS)
			&& !support.is(ModBlocks.WATTLED_DAUB_WINDOW)
			&& !support.is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)
			&& !support.is(ModBlocks.WATTLED_DAUB_DOOR)
			&& support.isFaceSturdy(level, below, Direction.UP);
	}
}
