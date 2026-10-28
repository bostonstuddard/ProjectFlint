package dev.projectflint.block;

import dev.projectflint.compat.flintquests.FlintQuestsBridge;
import dev.projectflint.block.entity.PrimitiveCampfireBlockEntity;
import dev.projectflint.config.GameplayTuning;
import dev.projectflint.item.CrudePlantTorchItem;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import dev.projectflint.system.PrimitiveProcessingRegistry;
import dev.projectflint.system.PrimitiveProcessor;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Permanent primitive campfire frame assembled from 12 Campfire Stones and
 * 12 Lightly Burnt Twigs. Ignition is temporary, but the frame itself remains and can
 * be relit indefinitely.
 */
public final class PrimitiveCampfireBlock extends Block implements EntityBlock {
	public static final IntegerProperty STONES = IntegerProperty.create("stones", 1, GameplayTuning.CAMPFIRE_STONE_CAPACITY);
	public static final IntegerProperty BURNT_TWIGS = IntegerProperty.create("burnt_twigs", 0, GameplayTuning.CAMPFIRE_BURNT_TWIG_CAPACITY);
	public static final BooleanProperty LIT = BooleanProperty.create("lit");

	public PrimitiveCampfireBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
			.setValue(STONES, 1)
			.setValue(BURNT_TWIGS, 0)
			.setValue(LIT, false));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (state.getValue(LIT) && stack.is(ModItems.LIGHTLY_BURNT_TWIG)) {
			if (level.isClientSide()) return InteractionResult.SUCCESS;
			return CrudePlantTorchItem.lightFromCampfire((ServerLevel)level, pos, player, stack);
		}

		if (state.getValue(LIT) && PrimitiveProcessingRegistry.isProcessable(PrimitiveProcessor.CAMPFIRE, stack)) {
			PrimitiveCampfireBlockEntity campfire = level.getBlockEntity(pos) instanceof PrimitiveCampfireBlockEntity be ? be : null;
			if (campfire != null && campfire.isBusy()) {
				return InteractionResult.FAIL;
			}
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}
			if (campfire == null || !campfire.tryInsert(stack)) {
				return InteractionResult.FAIL;
			}
			if (player instanceof ServerPlayer serverPlayer) FlintQuestsBridge.onWorkstationUse(serverPlayer, "primitive_campfire");
			if (!player.isCreative()) {
				stack.shrink(1);
			}
			level.playSound(null, pos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.35F, 1.06F);
			return InteractionResult.SUCCESS;
		}

		if (stack.is(ModItems.CAMPFIRE_STONE)) {
			int stones = state.getValue(STONES);
			if (stones >= GameplayTuning.CAMPFIRE_STONE_CAPACITY) {
				return InteractionResult.SUCCESS;
			}
			if (!level.isClientSide()) {
				stack.consume(1, player);
				level.setBlockAndUpdate(pos, state.setValue(STONES, stones + 1));
				level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.5F, 0.9F);
			}
			return InteractionResult.SUCCESS;
		}

		if (stack.is(ModItems.LIGHTLY_BURNT_TWIG)) {
			int twigs = state.getValue(BURNT_TWIGS);
			if (twigs >= GameplayTuning.CAMPFIRE_BURNT_TWIG_CAPACITY) {
				return InteractionResult.SUCCESS;
			}
			if (!level.isClientSide()) {
				stack.consume(1, player);
				BlockState next = state.setValue(BURNT_TWIGS, twigs + 1);
				level.setBlockAndUpdate(pos, next);
				level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.45F, 0.72F);
				if (next.getValue(STONES) == GameplayTuning.CAMPFIRE_STONE_CAPACITY
					&& next.getValue(BURNT_TWIGS) == GameplayTuning.CAMPFIRE_BURNT_TWIG_CAPACITY
					&& player instanceof ServerPlayer serverPlayer) {
					ModAdvancements.award(serverPlayer, "primitive_campfire", "assembled_campfire");
				}
			}
			return InteractionResult.SUCCESS;
		}

		if (!state.getValue(LIT)
			&& isComplete(state)
			&& hand == InteractionHand.MAIN_HAND
			&& ToolCapabilities.has(stack, ToolCapabilities.Capability.FIRESTARTER)) {
			if (level.isRainingAt(pos)) return InteractionResult.FAIL;
			if (!level.isClientSide()) {
				stack.hurtAndBreak(1, player, hand);
				level.setBlockAndUpdate(pos, state.setValue(LIT, true));
				level.scheduleTick(pos, this, GameplayTuning.CAMPFIRE_FIRE_DURATION_TICKS);
				level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.9F, 0.98F);
				if (player instanceof ServerPlayer serverPlayer) {
					ModAdvancements.award(serverPlayer, "flint_firestarter", "lit_campfire_with_firestarter");
				}
			}
			return InteractionResult.SUCCESS;
		}

		if (!state.getValue(LIT)
			&& isComplete(state)
			&& hand == InteractionHand.MAIN_HAND
			&& stack.is(ModItems.FLOWER_PETALS)
			&& stack.getCount() == GameplayTuning.CAMPFIRE_IGNITION_PETALS
			&& player.getOffhandItem().is(ModItems.TWIG)
			&& player.getOffhandItem().getCount() >= GameplayTuning.CAMPFIRE_IGNITION_OFFHAND_TWIGS) {
			if (level.isRainingAt(pos)) return InteractionResult.FAIL;
			if (!level.isClientSide()) {
				if (!player.isCreative()) {
					stack.shrink(GameplayTuning.CAMPFIRE_IGNITION_PETALS);
				}
				level.setBlockAndUpdate(pos, state.setValue(LIT, true));
				level.scheduleTick(pos, this, GameplayTuning.CAMPFIRE_FIRE_DURATION_TICKS);
				level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.85F, 0.92F);
				if (player instanceof ServerPlayer serverPlayer) {
					ModAdvancements.award(serverPlayer, "campfire_fire", "lit_campfire");
				}
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (state.getValue(LIT)) {
			return InteractionResult.PASS;
		}
		return takeOneComponent(state, level, pos, player);
	}

	public InteractionResult takeOneComponent(BlockState state, Level level, BlockPos pos, Player player) {
		if (!level.isClientSide()) {
			ItemStack returned;
			BlockState next = state;
			boolean remove = false;

			if (state.getValue(BURNT_TWIGS) > 0) {
				returned = new ItemStack(ModItems.LIGHTLY_BURNT_TWIG);
				next = state.setValue(BURNT_TWIGS, state.getValue(BURNT_TWIGS) - 1);
			} else {
				returned = new ItemStack(ModItems.CAMPFIRE_STONE);
				int stones = state.getValue(STONES);
				if (stones <= 1) {
					remove = true;
				} else {
					next = state.setValue(STONES, stones - 1);
				}
			}

			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) {
				return InteractionResult.FAIL;
			}
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			if (remove) {
				level.removeBlock(pos, false);
			} else {
				level.setBlockAndUpdate(pos, next);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.is(this) && state.getValue(LIT)) {
			level.setBlockAndUpdate(pos, state.setValue(LIT, false));
			level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.45F, 0.8F);
		}
	}

	@Override
	public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
		if (precipitation == Biome.Precipitation.RAIN && state.getValue(LIT)) {
			level.setBlockAndUpdate(pos, state.setValue(LIT, false));
			level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.65F, 0.9F);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) {
			return;
		}
		double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.25D;
		double y = pos.getY() + 0.38D;
		double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.25D;
		level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.012D, 0.0D);
		if (random.nextInt(3) == 0) {
			level.addParticle(ParticleTypes.SMOKE, x, y + 0.10D, z, 0.0D, 0.02D, 0.0D);
		}
		if (random.nextInt(18) == 0) {
			level.playLocalSound(pos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.42F, 0.95F, false);
		}
	}

	private static boolean isComplete(BlockState state) {
		return state.getValue(STONES) == GameplayTuning.CAMPFIRE_STONE_CAPACITY
			&& state.getValue(BURNT_TWIGS) == GameplayTuning.CAMPFIRE_BURNT_TWIG_CAPACITY;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 3.5D, 15.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STONES, BURNT_TWIGS, LIT);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PrimitiveCampfireBlockEntity(pos, state);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : (lvl, pos, st, be) -> {
			if (st.getValue(LIT) && lvl.isRainingAt(pos)) {
				lvl.setBlockAndUpdate(pos, st.setValue(LIT, false));
				lvl.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.65F, 0.9F);
				return;
			}
			if (be instanceof PrimitiveCampfireBlockEntity campfire) {
				campfire.serverTick(lvl, pos, st);
			}
		};
	}
}
