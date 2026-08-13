package dev.projectflint.block;

import dev.projectflint.block.entity.BakedClayKilnBlockEntity;
import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.PrimitiveInventorySystem;
import dev.projectflint.system.PrimitiveProcessingRegistry;
import dev.projectflint.system.PrimitiveProcessor;
import dev.projectflint.system.ToolCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Upgraded one-block kiln with the Clay Kiln processing rules and a 15-minute burn. */
public final class MortarKilnBlock extends Block implements EntityBlock {
	public static final IntegerProperty BRANCHES = BakedClayKilnBlock.BRANCHES;
	public static final BooleanProperty LIT = BakedClayKilnBlock.LIT;
	public static final EnumProperty<Direction> FACING = BakedClayKilnBlock.FACING;
	public static final int BURN_TICKS = 20 * 60 * 15;

	public MortarKilnBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(BRANCHES, 0).setValue(LIT, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean isMoving) {
		if (!state.canSurvive(level, pos)) {
			level.destroyBlock(pos, true);
			return;
		}
		super.neighborChanged(state, level, pos, block, orientation, isMoving);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (state.getValue(LIT)) {
			BakedClayKilnBlockEntity kiln = level.getBlockEntity(pos) instanceof BakedClayKilnBlockEntity be ? be : null;
			if (!PrimitiveProcessingRegistry.isProcessable(PrimitiveProcessor.KILN, stack)) return InteractionResult.FAIL;
			if (kiln != null && kiln.isBusy()) return InteractionResult.FAIL;
			if (level.isClientSide()) return InteractionResult.SUCCESS;
			if (kiln == null || !kiln.tryInsert(stack)) return InteractionResult.FAIL;
			if (!player.isCreative()) stack.shrink(1);
			level.playSound(null, pos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.35F, 1.08F);
			return InteractionResult.SUCCESS;
		}

		if (stack.is(ModItems.BRANCH) && state.getValue(BRANCHES) < GameplayTuning.KILN_BRANCH_CAPACITY) {
			if (!level.isClientSide()) {
				stack.consume(1, player);
				level.setBlockAndUpdate(pos, state.setValue(BRANCHES, state.getValue(BRANCHES) + 1));
				level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.5F, 0.72F);
			}
			return InteractionResult.SUCCESS;
		}

		if (ToolCapabilities.has(stack, ToolCapabilities.Capability.FIRESTARTER)
			&& state.getValue(BRANCHES) == GameplayTuning.KILN_BRANCH_CAPACITY) {
			if (!level.isClientSide()) {
				stack.hurtAndBreak(1, player, hand);
				level.setBlockAndUpdate(pos, state.setValue(BRANCHES, 0).setValue(LIT, true));
				level.scheduleTick(pos, this, BURN_TICKS);
				level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.9F, 0.78F);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public InteractionResult takeOneBranch(BlockState state, Level level, BlockPos pos, Player player) {
		if (state.getValue(LIT) || state.getValue(BRANCHES) <= 0) return InteractionResult.FAIL;
		if (!level.isClientSide()) {
			ItemStack returned = new ItemStack(ModItems.BRANCH);
			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) return InteractionResult.FAIL;
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			level.setBlockAndUpdate(pos, state.setValue(BRANCHES, state.getValue(BRANCHES) - 1));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.is(this) && state.getValue(LIT)) {
			level.setBlockAndUpdate(pos, state.setValue(LIT, false));
			level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.55F, 0.74F);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) return;
		level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 1.02D, pos.getZ() + 0.5D, 0, 0.025D, 0);
		if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.45D, pos.getZ() + 0.5D, 0, 0.01D, 0);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BRANCHES, LIT, FACING);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BakedClayKilnBlockEntity(pos, state);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : (lvl, pos, st, be) -> {
			if (be instanceof BakedClayKilnBlockEntity kiln) kiln.serverTick(lvl, pos, st);
		};
	}
}
