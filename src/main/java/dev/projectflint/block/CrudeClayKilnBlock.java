package dev.projectflint.block;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import dev.projectflint.system.PrimitiveInventorySystem;
import dev.projectflint.system.ToolCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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

/** Movable green-clay kiln body before its first 30-second bake. */
public final class CrudeClayKilnBlock extends Block {
	public static final IntegerProperty BRANCHES = IntegerProperty.create("branches", 0, GameplayTuning.KILN_BRANCH_CAPACITY);
	public static final BooleanProperty LIT = BooleanProperty.create("lit");
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	public CrudeClayKilnBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
			.setValue(BRANCHES, 0)
			.setValue(LIT, false)
			.setValue(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
		return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
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
		if (state.getValue(LIT)) return InteractionResult.FAIL;
		if (stack.is(ModItems.BRANCH) && state.getValue(BRANCHES) < GameplayTuning.KILN_BRANCH_CAPACITY) {
			if (!level.isClientSide()) {
				stack.consume(1, player);
				level.setBlockAndUpdate(pos, state.setValue(BRANCHES, state.getValue(BRANCHES) + 1));
				level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.5F, 0.75F);
			}
			return InteractionResult.SUCCESS;
		}
		if (ToolCapabilities.has(stack, ToolCapabilities.Capability.FIRESTARTER) && state.getValue(BRANCHES) == GameplayTuning.KILN_BRANCH_CAPACITY) {
			if (!level.isClientSide()) {
				stack.hurtAndBreak(1, player, hand);
				level.setBlockAndUpdate(pos, state.setValue(BRANCHES, 0).setValue(LIT, true));
				level.scheduleTick(pos, this, GameplayTuning.KILN_BAKE_TICKS);
				level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.9F, 0.82F);
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
		if (!state.is(this) || !state.getValue(LIT)) return;
		level.setBlockAndUpdate(pos, ModBlocks.BAKED_CLAY_KILN.defaultBlockState().setValue(BakedClayKilnBlock.FACING, state.getValue(FACING)));
		level.playSound(null, pos, SoundEvents.DECORATED_POT_BREAK, SoundSource.BLOCKS, 0.7F, 0.62F);
		for (ServerPlayer serverPlayer : level.players()) {
			if (serverPlayer.blockPosition().closerThan(pos, 8.0D)) {
				ModAdvancements.award(serverPlayer, "baked_clay_kiln", "baked_kiln_base");
			}
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) return;
		level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 0, 0.015D, 0);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BRANCHES, LIT, FACING);
	}
}
