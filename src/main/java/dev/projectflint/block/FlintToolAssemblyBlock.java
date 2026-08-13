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
import net.minecraft.world.item.Items;
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

/** Shared ground assembly for the Flint Firestarter and Flint Multitool recipes. */
public final class FlintToolAssemblyBlock extends SupportedGroundBlock {
	public static final IntegerProperty BRANCHES = IntegerProperty.create("branches", 0, 8);
	public static final IntegerProperty BURNT_BRANCHES = IntegerProperty.create("burnt_branches", 0, 2);
	public static final IntegerProperty FLINT = IntegerProperty.create("flint", 0, 2);

	public FlintToolAssemblyBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(BRANCHES, 0).setValue(BURNT_BRANCHES, 0).setValue(FLINT, 0));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		BlockState next = state;
		if (stack.is(ModItems.BRANCH) && state.getValue(BRANCHES) < 8) {
			next = state.setValue(BRANCHES, state.getValue(BRANCHES) + 1);
		} else if (stack.is(ModItems.BURNT_BRANCH) && state.getValue(BURNT_BRANCHES) < 2) {
			next = state.setValue(BURNT_BRANCHES, state.getValue(BURNT_BRANCHES) + 1);
		} else if (stack.is(Items.FLINT) && state.getValue(FLINT) < 2) {
			next = state.setValue(FLINT, state.getValue(FLINT) + 1);
		} else {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			stack.consume(1, player);
			ItemStack result = resultFor(next);
			if (!result.isEmpty()) {
				level.removeBlock(pos, false);
				PrimitiveInteractionSystem.giveOrDrop(player, result);
				level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 0.82F);
				if (player instanceof ServerPlayer serverPlayer) {
					if (result.is(ModItems.FLINT_FIRESTARTER)) {
						ModAdvancements.award(serverPlayer, "flint_firestarter", "assembled_firestarter");
					} else {
						ModAdvancements.award(serverPlayer, "flint_multitool", "assembled_flint_multitool");
					}
				}
			} else {
				level.setBlockAndUpdate(pos, next);
				level.playSound(null, pos, stack.is(Items.FLINT) ? SoundEvents.STONE_PLACE : SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.48F, 0.94F);
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
			if (state.getValue(FLINT) > 0) {
				returned = new ItemStack(Items.FLINT);
				next = state.setValue(FLINT, state.getValue(FLINT) - 1);
			} else if (state.getValue(BURNT_BRANCHES) > 0) {
				returned = new ItemStack(ModItems.BURNT_BRANCH);
				next = state.setValue(BURNT_BRANCHES, state.getValue(BURNT_BRANCHES) - 1);
			} else if (state.getValue(BRANCHES) > 0) {
				returned = new ItemStack(ModItems.BRANCH);
				next = state.setValue(BRANCHES, state.getValue(BRANCHES) - 1);
			} else {
				return InteractionResult.FAIL;
			}

			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) {
				return InteractionResult.FAIL;
			}
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			if (isEmpty(next)) {
				level.removeBlock(pos, false);
			} else {
				level.setBlockAndUpdate(pos, next);
			}
		}
		return InteractionResult.SUCCESS;
	}

	public BlockState stateForFirstIngredient(ItemStack stack) {
		BlockState state = defaultBlockState();
		if (stack.is(ModItems.BRANCH)) return state.setValue(BRANCHES, 1);
		if (stack.is(ModItems.BURNT_BRANCH)) return state.setValue(BURNT_BRANCHES, 1);
		if (stack.is(Items.FLINT)) return state.setValue(FLINT, 1);
		return state;
	}

	private static ItemStack resultFor(BlockState state) {
		if (state.getValue(BRANCHES) == 8 && state.getValue(BURNT_BRANCHES) == 0 && state.getValue(FLINT) == 2) {
			return new ItemStack(ModItems.FLINT_FIRESTARTER);
		}
		if (state.getValue(BRANCHES) == 0 && state.getValue(BURNT_BRANCHES) == 2 && state.getValue(FLINT) == 2) {
			return new ItemStack(ModItems.FLINT_MULTITOOL);
		}
		return ItemStack.EMPTY;
	}

	private static boolean isEmpty(BlockState state) {
		return state.getValue(BRANCHES) == 0 && state.getValue(BURNT_BRANCHES) == 0 && state.getValue(FLINT) == 0;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 3.0D, 15.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BRANCHES, BURNT_BRANCHES, FLINT);
	}
}
