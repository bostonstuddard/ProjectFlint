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

public final class BindingAssemblyBlock extends SupportedGroundBlock {
	public static final IntegerProperty REEDS = IntegerProperty.create("reeds", 1, 4);
	public static final IntegerProperty STRINGS = IntegerProperty.create("strings", 0, 2);

	public BindingAssemblyBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(REEDS, 1).setValue(STRINGS, 0));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.is(ModItems.CRUDE_REED)) {
			int reeds = state.getValue(REEDS);
			if (reeds >= 4) {
				return InteractionResult.SUCCESS;
			}
			if (!level.isClientSide()) {
				stack.consume(1, player);
				level.setBlockAndUpdate(pos, state.setValue(REEDS, reeds + 1));
				level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.45F, 1.05F);
			}
			return InteractionResult.SUCCESS;
		}

		if (stack.is(ModItems.CRUDE_PLANT_STRING)) {
			int strings = state.getValue(STRINGS);
			if (strings >= 2) {
				return InteractionResult.SUCCESS;
			}
			if (!level.isClientSide()) {
				stack.consume(1, player);
				level.setBlockAndUpdate(pos, state.setValue(STRINGS, strings + 1));
				level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.45F, 1.25F);
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
			if (state.getValue(STRINGS) > 0) {
				ItemStack returned = new ItemStack(ModItems.CRUDE_PLANT_STRING);
				if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) {
					return InteractionResult.FAIL;
				}
				PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
				level.setBlockAndUpdate(pos, state.setValue(STRINGS, state.getValue(STRINGS) - 1));
				return InteractionResult.SUCCESS;
			}

			ItemStack returned = new ItemStack(ModItems.CRUDE_REED);
			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) {
				return InteractionResult.FAIL;
			}
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			int reeds = state.getValue(REEDS);
			if (reeds <= 1) {
				level.removeBlock(pos, false);
			} else {
				level.setBlockAndUpdate(pos, state.setValue(REEDS, reeds - 1));
			}
		}
		return InteractionResult.SUCCESS;
	}

	public InteractionResult hitWithHammer(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		ItemStack hammer = player.getItemInHand(hand);
		if (!ModItems.isHammerLike(hammer)
			|| state.getValue(REEDS) != 4
			|| state.getValue(STRINGS) != 2) {
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			hammer.hurtAndBreak(1, player, hand);
			level.removeBlock(pos, false);
			PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.PLANT_TOOL_BINDING));
			level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.75F, 0.66F);
			if (player instanceof ServerPlayer serverPlayer) {
				ModAdvancements.award(serverPlayer, "plant_tool_binding", "made_plant_tool_binding");
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		double height = 1.0D + state.getValue(STRINGS) * 0.4D;
		return Block.box(1.0D, 0.0D, 2.0D, 15.0D, height, 14.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(REEDS, STRINGS);
	}
}
