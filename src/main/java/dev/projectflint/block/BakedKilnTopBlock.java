package dev.projectflint.block;

import dev.projectflint.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Permanent upper half of the baked two-block kiln. */
public final class BakedKilnTopBlock extends Block {
	public static final BooleanProperty LIT = BooleanProperty.create("lit");
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	public BakedKilnTopBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(LIT, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
		if (below.is(ModBlocks.BAKED_CLAY_KILN) && below.hasProperty(BakedClayKilnBlock.FACING)) {
			return defaultBlockState().setValue(FACING, below.getValue(BakedClayKilnBlock.FACING));
		}
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return level.getBlockState(pos.below()).is(ModBlocks.BAKED_CLAY_KILN);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean isMoving) {
		if (!state.canSurvive(level, pos)) {
			level.destroyBlock(pos, false);
			return;
		}
		super.neighborChanged(state, level, pos, block, orientation, isMoving);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, FACING);
	}
}
