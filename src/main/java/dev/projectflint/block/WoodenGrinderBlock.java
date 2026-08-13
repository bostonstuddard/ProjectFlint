package dev.projectflint.block;

import dev.projectflint.menu.WoodenGrinderMenu;
import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Two-input upgrade to the Clay Grinder; it also supports all Clay Grinder recipes. */
public final class WoodenGrinderBlock extends HorizontalDirectionalBlock {
	private static final Component TITLE = Component.translatable("container.projectflint.wooden_grinder");

	public WoodenGrinderBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return simpleCodec(WoodenGrinderBlock::new);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return open(level, pos, player);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		return open(level, pos, player);
	}

	private InteractionResult open(Level level, BlockPos pos, Player player) {
		if (!level.isClientSide()) {
			MenuProvider provider = new SimpleMenuProvider(
				(syncId, inventory, p) -> new WoodenGrinderMenu(syncId, inventory, ContainerLevelAccess.create(level, pos)),
				TITLE
			);
			player.openMenu(provider);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTicks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
			scheduledTicks.scheduleTick(pos, this, 1);
		}
		return super.updateShape(state, level, scheduledTicks, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) {
			Block.popResource(level, pos, new ItemStack(ModItems.WOODEN_GRINDER));
			level.removeBlock(pos, false);
			return;
		}
		super.tick(state, level, pos, random);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean isMoving) {
		if (!state.canSurvive(level, pos)) {
			level.scheduleTick(pos, this, 1);
			return;
		}
		super.neighborChanged(state, level, pos, block, orientation, isMoving);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
	}
}
