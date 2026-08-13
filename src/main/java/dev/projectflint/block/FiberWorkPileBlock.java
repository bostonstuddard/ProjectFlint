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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FiberWorkPileBlock extends SupportedGroundBlock {
	public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, 2);

	public FiberWorkPileBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(COUNT, 1));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!stack.is(ModItems.PLANT_FIBER)) {
			return takeOneComponent(state, level, pos, player);
		}

		int count = state.getValue(COUNT);
		if (count >= 2) {
			return InteractionResult.SUCCESS;
		}

		if (!level.isClientSide()) {
			stack.consume(1, player);
			level.setBlockAndUpdate(pos, state.setValue(COUNT, count + 1));
			level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.45F, 1.25F);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return takeOneComponent(state, level, pos, player);
	}

	public InteractionResult takeOneComponent(BlockState state, Level level, BlockPos pos, Player player) {
		if (!level.isClientSide()) {
			ItemStack returned = new ItemStack(ModItems.PLANT_FIBER);
			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) {
				return InteractionResult.FAIL;
			}
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			int count = state.getValue(COUNT);
			if (count <= 1) {
				level.removeBlock(pos, false);
			} else {
				level.setBlockAndUpdate(pos, state.setValue(COUNT, count - 1));
			}
		}
		return InteractionResult.SUCCESS;
	}

	public InteractionResult hitWithRefinedRock(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		if (!player.getItemInHand(hand).is(ModItems.REFINED_ROCK) || state.getValue(COUNT) != 2) {
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			player.getItemInHand(hand).consume(1, player);
			level.removeBlock(pos, false);
			PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.CRUDE_PLANT_STRING));
			level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.7F, 0.82F);
			if (player instanceof ServerPlayer serverPlayer) {
				ModAdvancements.award(serverPlayer, "crude_string", "made_crude_string");
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(2.0D, 0.0D, 2.0D, 14.0D, state.getValue(COUNT) == 1 ? 1.0D : 1.6D, 14.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COUNT);
	}
}
