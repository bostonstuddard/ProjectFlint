package dev.projectflint.block;

import dev.projectflint.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Creative-only prototype door for the Wattled Daub construction tier. */
public final class WattledDaubDoorBlock extends DoorBlock {
	public WattledDaubDoorBlock(BlockBehaviour.Properties properties) {
		super(BlockSetType.OAK, properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		if (!context.getLevel().getBlockState(pos.below()).is(ModBlocks.WATTLED_DAUB_FLOOR_TILE)) return null;
		return super.getStateForPlacement(context);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
			return level.getBlockState(pos.below()).is(ModBlocks.WATTLED_DAUB_FLOOR_TILE);
		}
		return super.canSurvive(state, level, pos);
	}
}
