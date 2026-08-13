package dev.projectflint.block;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import dev.projectflint.system.PrimitiveInteractionSystem;
import dev.projectflint.system.PrimitiveInventorySystem;
import dev.projectflint.system.ToolCapabilities;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Placeable unfired clay ball that can be ringed with four twigs and fired. */
public final class ClayFiringPileBlock extends SupportedGroundBlock {
	public static final IntegerProperty TWIGS = IntegerProperty.create("twigs", 0, GameplayTuning.CLAY_BALL_FIRING_TWIGS);
	public static final BooleanProperty LIT = BooleanProperty.create("lit");

	public ClayFiringPileBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(TWIGS, 0).setValue(LIT, false));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (state.getValue(LIT)) return InteractionResult.FAIL;
		if (stack.is(ModItems.TWIG) && state.getValue(TWIGS) < GameplayTuning.CLAY_BALL_FIRING_TWIGS) {
			if (!level.isClientSide()) {
				stack.consume(1, player);
				level.setBlockAndUpdate(pos, state.setValue(TWIGS, state.getValue(TWIGS) + 1));
				level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.45F, 0.86F);
			}
			return InteractionResult.SUCCESS;
		}
		if (ToolCapabilities.has(stack, ToolCapabilities.Capability.FIRESTARTER) && state.getValue(TWIGS) == GameplayTuning.CLAY_BALL_FIRING_TWIGS) {
			if (!level.isClientSide()) {
				stack.hurtAndBreak(1, player, hand);
				level.setBlockAndUpdate(pos, state.setValue(LIT, true));
				level.scheduleTick(pos, this, GameplayTuning.CLAY_BALL_FIRING_TICKS);
				level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.88F, 0.92F);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public InteractionResult takeOneComponent(BlockState state, Level level, BlockPos pos, Player player) {
		if (state.getValue(LIT)) return InteractionResult.FAIL;
		if (!level.isClientSide()) {
			ItemStack returned;
			if (state.getValue(TWIGS) > 0) {
				returned = new ItemStack(ModItems.TWIG);
				if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) return InteractionResult.FAIL;
				PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
				level.setBlockAndUpdate(pos, state.setValue(TWIGS, state.getValue(TWIGS) - 1));
			} else {
				returned = new ItemStack(ModItems.UNFIRED_CLAY_BALL);
				if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) return InteractionResult.FAIL;
				PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
				level.removeBlock(pos, false);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.is(this) || !state.getValue(LIT)) return;
		level.removeBlock(pos, false);
		Block.popResource(level, pos, new ItemStack(ModItems.FIRED_CLAY, 2));
		level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.58F, 0.78F);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) return;
		level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.22D, pos.getZ() + 0.5D, 0, 0.01D, 0);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(3.0D, 0.0D, 3.0D, 13.0D, 3.2D, 13.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TWIGS, LIT);
	}
}
