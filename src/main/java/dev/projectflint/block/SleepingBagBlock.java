package dev.projectflint.block;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ToolCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Two-block primitive bedroll assembled from Woven Wool and recolored with vanilla dyes. */
public final class SleepingBagBlock extends BedBlock {
	public static final EnumProperty<Direction> FACING = BedBlock.FACING;
	public static final EnumProperty<BedPart> PART = BedBlock.PART;
	public static final BooleanProperty OCCUPIED = BedBlock.OCCUPIED;
	public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

	public SleepingBagBlock(BlockBehaviour.Properties properties) {
		super(DyeColor.GREEN, properties);
		registerDefaultState(defaultBlockState().setValue(COLOR, DyeColor.GREEN));
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction facing = context.getHorizontalDirection().getOpposite();
		BlockPos footPos = context.getClickedPos();
		BlockPos headPos = footPos.relative(facing);
		DyeColor itemColor = context.getItemInHand().getOrDefault(DataComponents.BASE_COLOR, DyeColor.GREEN);
		BlockState state = defaultBlockState().setValue(FACING, facing).setValue(COLOR, itemColor);
		return canPlacePair(context.getLevel(), footPos, headPos, context) && state.canSurvive(context.getLevel(), footPos)
			? state
			: null;
	}

	/** Used by the eight-Woven-Wool physical placement path. */
	public boolean placePair(Level level, BlockPos footPos, Direction facing, DyeColor color, BlockPlaceContext context) {
		BlockPos headPos = footPos.relative(facing);
		if (!canPlacePair(level, footPos, headPos, context)) return false;

		BlockState foot = defaultBlockState()
			.setValue(FACING, facing)
			.setValue(PART, BedPart.FOOT)
			.setValue(COLOR, color);
		if (!foot.canSurvive(level, footPos)) return false;

		level.setBlock(footPos, foot, 2);
		level.setBlock(headPos, foot.setValue(PART, BedPart.HEAD), 3);
		level.updateNeighborsAt(footPos, this);
		level.updateNeighborsAt(headPos, this);
		return true;
	}

	private static boolean canPlacePair(Level level, BlockPos footPos, BlockPos headPos, BlockPlaceContext context) {
		return level.getBlockState(footPos).canBeReplaced(context)
			&& level.getBlockState(headPos).canBeReplaced(context)
			&& level.getWorldBorder().isWithinBounds(footPos)
			&& level.getWorldBorder().isWithinBounds(headPos)
			&& hasSupport(level, footPos)
			&& hasSupport(level, headPos);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.getItem() instanceof DyeItem dye) {
			DyeColor color = dye.getDyeColor();
			if (state.getValue(COLOR) == color) return InteractionResult.SUCCESS;
			if (!level.isClientSide()) {
				setPairColor(level, pos, state, color);
				stack.consume(1, player);
				level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 0.65F, 1.0F);
			}
			return InteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return super.useWithoutItem(state, level, pos, player, hit);
	}

	private void setPairColor(Level level, BlockPos pos, BlockState state, DyeColor color) {
		BlockPos footPos = footPosition(pos, state);
		BlockPos headPos = footPos.relative(state.getValue(FACING));
		BlockState foot = level.getBlockState(footPos);
		BlockState head = level.getBlockState(headPos);
		if (foot.is(this)) level.setBlockAndUpdate(footPos, foot.setValue(COLOR, color));
		if (head.is(this)) level.setBlockAndUpdate(headPos, head.setValue(COLOR, color));
	}

	public void removePairWithSalvage(Level level, BlockPos pos, BlockState state) {
		removePairWithSalvage(level, pos, state, ItemStack.EMPTY);
	}

	public void removePairWithSalvage(Level level, BlockPos pos, BlockState state, ItemStack tool) {
		removePairWithSalvage(level, pos, state, tool, false);
	}

	/** Utility Vest users recover the intact bedroll when dismantling it by hand. */
	public void removePairWithSalvage(Level level, BlockPos pos, BlockState state, ItemStack tool, boolean recoverIntact) {
		BlockPos footPos = footPosition(pos, state);
		BlockPos headPos = footPos.relative(state.getValue(FACING));
		if (level.getBlockState(headPos).is(this)) level.setBlock(headPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		if (level.getBlockState(footPos).is(this)) level.setBlock(footPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		ItemStack drop;
		if (recoverIntact || tool.is(ModItems.ADVANCED_FLINT_MULTITOOL)) {
			drop = new ItemStack(ModItems.SLEEPING_BAG);
			drop.set(DataComponents.BASE_COLOR, state.getValue(COLOR));
		} else {
			drop = new ItemStack(ModItems.WOVEN_WOOL, GameplayTuning.SLEEPING_BAG_SALVAGE_WOOL);
		}
		Block.popResource(level, footPos, drop);
		level.updateNeighborsAt(headPos, this);
		level.updateNeighborsAt(footPos, this);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && player.preventsBlockDrops()) {
			BlockPos footPos = footPosition(pos, state);
			BlockPos headPos = footPos.relative(state.getValue(FACING));
			BlockPos otherPos = pos.equals(footPos) ? headPos : footPos;
			if (level.getBlockState(otherPos).is(this)) {
				level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	private static BlockPos footPosition(BlockPos pos, BlockState state) {
		return state.getValue(PART) == BedPart.FOOT ? pos : pos.relative(state.getValue(FACING).getOpposite());
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return hasSupport(level, pos);
	}

	private static boolean hasSupport(LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean isMoving) {
		if (!level.isClientSide() && (!state.canSurvive(level, pos) || !hasMatchingHalf(level, pos, state))) {
			removePairWithSalvage(level, pos, state);
			return;
		}
		super.neighborChanged(state, level, pos, block, orientation, isMoving);
	}

	private boolean hasMatchingHalf(LevelReader level, BlockPos pos, BlockState state) {
		Direction towardOther = state.getValue(PART) == BedPart.FOOT
			? state.getValue(FACING)
			: state.getValue(FACING).getOpposite();
		BlockState other = level.getBlockState(pos.relative(towardOther));
		return other.is(this)
			&& other.getValue(FACING) == state.getValue(FACING)
			&& other.getValue(PART) != state.getValue(PART);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(0.5D, 0.0D, 0.5D, 15.5D, 2.0D, 15.5D);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	/**
	 * BedBlockEntity exists to drive vanilla's animated bed renderer. The sleeping
	 * bag is model-rendered, so omitting that visual-only entity prevents a full
	 * vanilla white bed from being drawn over the connected bedroll models.
	 */
	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(COLOR);
	}
}
