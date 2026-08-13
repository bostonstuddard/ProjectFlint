package dev.projectflint.block;

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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class HammerAssemblyBlock extends SupportedGroundBlock {
	public static final IntegerProperty TWIGS = IntegerProperty.create("twigs", 1, 2);
	public static final BooleanProperty HAS_ROCK = BooleanProperty.create("has_rock");

	public HammerAssemblyBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(TWIGS, 1).setValue(HAS_ROCK, false));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.is(ModItems.LIGHTLY_BURNT_TWIG)) {
			int twigs = state.getValue(TWIGS);
			if (twigs >= 2) {
				return InteractionResult.SUCCESS;
			}
			if (!level.isClientSide()) {
				stack.consume(1, player);
				level.setBlockAndUpdate(pos, state.setValue(TWIGS, twigs + 1));
				level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.5F, 0.78F);
			}
			return InteractionResult.SUCCESS;
		}

		if (stack.is(ModItems.ROCK)) {
			if (state.getValue(HAS_ROCK)) {
				return InteractionResult.SUCCESS;
			}
			if (!level.isClientSide()) {
				stack.consume(1, player);
				level.setBlockAndUpdate(pos, state.setValue(HAS_ROCK, true));
				level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.55F, 0.9F);
			}
			return InteractionResult.SUCCESS;
		}

		return takeOneComponent(state, level, pos, player);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return takeOneComponent(state, level, pos, player);
	}

	public InteractionResult takeOneComponent(BlockState state, Level level, BlockPos pos, Player player) {
		if (!level.isClientSide()) {
			ItemStack returned;
			BlockState nextState;
			if (state.getValue(HAS_ROCK)) {
				returned = new ItemStack(ModItems.ROCK);
				nextState = state.setValue(HAS_ROCK, false);
			} else {
				returned = new ItemStack(ModItems.LIGHTLY_BURNT_TWIG);
				int twigs = state.getValue(TWIGS);
				if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) {
					return InteractionResult.FAIL;
				}
				PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
				if (twigs <= 1) {
					level.removeBlock(pos, false);
					return InteractionResult.SUCCESS;
				}
				level.setBlockAndUpdate(pos, state.setValue(TWIGS, twigs - 1));
				return InteractionResult.SUCCESS;
			}

			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) {
				return InteractionResult.FAIL;
			}
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			level.setBlockAndUpdate(pos, nextState);
		}
		return InteractionResult.SUCCESS;
	}

	public InteractionResult hitWithRefinedRock(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		if (!player.getItemInHand(hand).is(ModItems.REFINED_ROCK)
			|| state.getValue(TWIGS) != 2
			|| !state.getValue(HAS_ROCK)) {
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			player.getItemInHand(hand).consume(1, player);
			level.removeBlock(pos, false);
			PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.CRUDE_STONE_HAMMER));
			level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.8F, 0.72F);
			if (player instanceof ServerPlayer serverPlayer) {
				ModAdvancements.award(serverPlayer, "crude_hammer", "made_crude_hammer");
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(2.0D, 0.0D, 2.0D, 14.0D, state.getValue(HAS_ROCK) ? 3.0D : 1.5D, 14.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TWIGS, HAS_ROCK);
	}
}
