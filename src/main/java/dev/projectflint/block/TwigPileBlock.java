package dev.projectflint.block;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.item.CrudePlantTorchItem;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import dev.projectflint.system.PrimitiveInteractionSystem;
import dev.projectflint.system.PrimitiveInventorySystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TwigPileBlock extends Block {
	public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, GameplayTuning.TWIG_PILE_CAPACITY);
	public static final BooleanProperty LIT = BooleanProperty.create("lit");

	public TwigPileBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
			.setValue(COUNT, 1)
			.setValue(LIT, false));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (state.getValue(LIT) && stack.is(ModItems.LIGHTLY_BURNT_TWIG)) {
			if (level.isClientSide()) return InteractionResult.SUCCESS;
			return CrudePlantTorchItem.lightFromCampfire((ServerLevel)level, pos, player, stack);
		}

		if (!stack.is(ModItems.TWIG)) {
			if (!state.getValue(LIT)) {
				return takeOneTwig(state, level, pos, player);
			}
			return InteractionResult.PASS;
		}

		if (state.getValue(LIT)) {
			if (!level.isClientSide()) {
				if (!player.isCreative()) {
					stack.shrink(1);
				}
				PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.LIGHTLY_BURNT_TWIG));
				if (player instanceof ServerPlayer serverPlayer) {
					ModAdvancements.award(serverPlayer, "charred_twig", "lightly_burnt_twig");
				}
				level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25F, 1.65F);
			}
			return InteractionResult.SUCCESS;
		}

		int count = state.getValue(COUNT);
		if (count < GameplayTuning.TWIG_PILE_CAPACITY) {
			if (!level.isClientSide()) {
				if (!player.isCreative()) {
					stack.shrink(1);
				}
				level.setBlockAndUpdate(pos, state.setValue(COUNT, count + 1));
				level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.55F, 1.35F);
			}
			return InteractionResult.SUCCESS;
		}

		ItemStack otherHand = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
		if (stack.getCount() != GameplayTuning.TWIG_IGNITION_STACK_SIZE || !otherHand.is(ModItems.REFINED_ROCK)) {
			return InteractionResult.SUCCESS;
		}

		if (level.isRainingAt(pos)) {
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			boolean success = level.getRandom().nextFloat() < GameplayTuning.TWIG_IGNITION_SUCCESS_CHANCE;
			if (!success) {
				if (!player.isCreative()) {
					stack.shrink(GameplayTuning.TWIG_IGNITION_STACK_SIZE);
				}
				level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.45F, 0.55F);
				return InteractionResult.SUCCESS;
			}

			if (!player.isCreative()) {
				stack.shrink(GameplayTuning.TWIG_IGNITION_STACK_SIZE);
			}
			level.setBlockAndUpdate(pos, state.setValue(LIT, true));
			level.scheduleTick(pos, this, GameplayTuning.TWIG_FIRE_DURATION_TICKS);
			if (player instanceof ServerPlayer serverPlayer) {
				ModAdvancements.award(serverPlayer, "first_fire", "lit_twig_pile");
			}
			level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.8F, 1.05F);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide() && placer instanceof ServerPlayer serverPlayer) {
			ModAdvancements.award(serverPlayer, "kindling", "placed_twig_pile");
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (state.getValue(LIT)) {
			return InteractionResult.PASS;
		}
		return takeOneTwig(state, level, pos, player);
	}

	/**
	 * Returns one Twig from an unlit pile. The pile is changed only after the
	 * Twig can be accepted by the primitive inventory, so a full inventory never
	 * turns this interaction into another drop/carry exploit.
	 */
	public InteractionResult takeOneTwig(BlockState state, Level level, BlockPos pos, Player player) {
		if (state.getValue(LIT)) {
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			ItemStack twig = new ItemStack(ModItems.TWIG);
			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, twig)) {
				return InteractionResult.FAIL;
			}

			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, twig);
			int count = state.getValue(COUNT);
			if (count <= 1) {
				level.removeBlock(pos, false);
			} else {
				level.setBlockAndUpdate(pos, state.setValue(COUNT, count - 1));
			}
			level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.45F, 0.85F);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.is(this) && state.getValue(LIT)) {
			level.removeBlock(pos, false);
		}
	}

	@Override
	public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
		if (precipitation == Biome.Precipitation.RAIN && state.getValue(LIT)) {
			level.setBlockAndUpdate(pos, state.setValue(LIT, false));
			level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.55F, 1.0F);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) {
			return;
		}

		double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D;
		double y = pos.getY() + 0.28D;
		double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D;
		level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.015D, 0.0D);
		if (random.nextInt(3) == 0) {
			level.addParticle(ParticleTypes.SMOKE, x, y + 0.08D, z, 0.0D, 0.025D, 0.0D);
		}
		if (random.nextInt(18) == 0) {
			level.playLocalSound(pos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.35F, 1.25F, false);
		}
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		// The visual pile is intentionally low and spread out rather than becoming a
		// staircase of chunky sticks as more Twigs are added.
		double height = 1.0D + ((state.getValue(COUNT) - 1) / 4) * 0.6D;
		return Block.box(2.0D, 0.0D, 2.0D, 14.0D, height, 14.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COUNT, LIT);
	}
}
