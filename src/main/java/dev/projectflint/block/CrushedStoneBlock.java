package dev.projectflint.block;

import net.minecraft.util.ColorRGBA;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Falling crushed stone that remembers whether it came from a player's block-item placement.
 * Natural/hammer-created crushed stone keeps the default false state; BlockItem placement sets true.
 */
public final class CrushedStoneBlock extends ColoredFallingBlock {
	public static final BooleanProperty PLAYER_PLACED = BooleanProperty.create("player_placed");

	public CrushedStoneBlock(ColorRGBA dustColor, BlockBehaviour.Properties properties) {
		super(dustColor, properties);
		registerDefaultState(stateDefinition.any().setValue(PLAYER_PLACED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(PLAYER_PLACED, true);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
		builder.add(PLAYER_PLACED);
	}

	public static boolean isPlayerPlaced(BlockState state) {
		return state.hasProperty(PLAYER_PLACED) && state.getValue(PLAYER_PLACED);
	}
}
