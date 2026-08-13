package dev.projectflint.block;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import dev.projectflint.system.PrimitiveInteractionSystem;
import dev.projectflint.system.PrimitiveInventorySystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 12 Burnt Twigs + 32 Dirt Piles physical assembly for the first bowl form. */
public final class WovenBowlAssemblyBlock extends SupportedGroundBlock {
	public static final IntegerProperty BURNT_TWIGS = IntegerProperty.create("burnt_twigs", 0, GameplayTuning.WOVEN_BOWL_BURNT_TWIGS);
	public static final IntegerProperty DIRT_PILES = IntegerProperty.create("dirt_piles", 0, GameplayTuning.WOVEN_BOWL_DIRT_PILES);

	public WovenBowlAssemblyBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(BURNT_TWIGS, 0).setValue(DIRT_PILES, 0));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		BlockState next = state;
		if (stack.is(ModItems.BURNT_TWIG) && state.getValue(BURNT_TWIGS) < GameplayTuning.WOVEN_BOWL_BURNT_TWIGS) {
			next = state.setValue(BURNT_TWIGS, state.getValue(BURNT_TWIGS) + 1);
		} else if (stack.is(ModItems.DIRT_PILE) && state.getValue(DIRT_PILES) < GameplayTuning.WOVEN_BOWL_DIRT_PILES) {
			next = state.setValue(DIRT_PILES, state.getValue(DIRT_PILES) + 1);
		} else {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			stack.consume(1, player);
			if (isComplete(next)) {
				level.removeBlock(pos, false);
				PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.CRUDELY_WOVEN_BOWL));
				level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8F, 0.72F);
				if (player instanceof ServerPlayer serverPlayer) {
					ModAdvancements.award(serverPlayer, "crudely_woven_bowl", "assembled_woven_bowl");
				}
			} else {
				level.setBlockAndUpdate(pos, next);
				level.playSound(null, pos, stack.is(ModItems.BURNT_TWIG) ? SoundEvents.WOOD_PLACE : SoundEvents.ROOTED_DIRT_PLACE, SoundSource.BLOCKS, 0.42F, 0.88F);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return takeOneComponent(state, level, pos, player);
	}

	public InteractionResult takeOneComponent(BlockState state, Level level, BlockPos pos, Player player) {
		if (!level.isClientSide()) {
			ItemStack returned;
			BlockState next;
			if (state.getValue(BURNT_TWIGS) > 0) {
				returned = new ItemStack(ModItems.BURNT_TWIG);
				next = state.setValue(BURNT_TWIGS, state.getValue(BURNT_TWIGS) - 1);
			} else if (state.getValue(DIRT_PILES) > 0) {
				returned = new ItemStack(ModItems.DIRT_PILE);
				next = state.setValue(DIRT_PILES, state.getValue(DIRT_PILES) - 1);
			} else {
				return InteractionResult.FAIL;
			}
			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) return InteractionResult.FAIL;
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			if (next.getValue(BURNT_TWIGS) == 0 && next.getValue(DIRT_PILES) == 0) level.removeBlock(pos, false);
			else level.setBlockAndUpdate(pos, next);
		}
		return InteractionResult.SUCCESS;
	}

	public BlockState stateForFirstDirtPile() {
		return defaultBlockState().setValue(DIRT_PILES, 1);
	}

	private static boolean isComplete(BlockState state) {
		return state.getValue(BURNT_TWIGS) == GameplayTuning.WOVEN_BOWL_BURNT_TWIGS
			&& state.getValue(DIRT_PILES) == GameplayTuning.WOVEN_BOWL_DIRT_PILES;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 3.0D, 15.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BURNT_TWIGS, DIRT_PILES);
	}
}
