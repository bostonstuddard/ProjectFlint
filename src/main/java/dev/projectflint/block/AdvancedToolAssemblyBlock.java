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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class AdvancedToolAssemblyBlock extends SupportedGroundBlock {
	public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, 2);
	public static final IntegerProperty BRANCHES = IntegerProperty.create("branches", 0, 2);

	public AdvancedToolAssemblyBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(TYPE, 0).setValue(BRANCHES, 0));
	}

	public BlockState stateForPiece(ItemStack stack) {
		int type = stack.is(ModItems.FLINT_AXE_PIECES) ? 0 : stack.is(ModItems.FLINT_CUTTER_PIECE) ? 1 : 2;
		return defaultBlockState().setValue(TYPE, type);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!stack.is(ModItems.BURNT_BRANCH) || state.getValue(BRANCHES) >= 2) return InteractionResult.PASS;
		if (!level.isClientSide()) {
			stack.consume(1, player);
			int branches = state.getValue(BRANCHES) + 1;
			if (branches >= 2) {
				ItemStack result = switch (state.getValue(TYPE)) {
					case 0 -> new ItemStack(ModItems.FLINT_AXE);
					case 1 -> new ItemStack(ModItems.FLINT_PLANT_CUTTERS);
					default -> new ItemStack(ModItems.ADVANCED_FLINT_MULTITOOL);
				};
				level.removeBlock(pos, false);
				PrimitiveInteractionSystem.giveOrDrop(player, result);
				if (player instanceof ServerPlayer serverPlayer) {
					String advancement = result.is(ModItems.FLINT_AXE) ? "flint_axe" : result.is(ModItems.FLINT_PLANT_CUTTERS) ? "flint_plant_cutters" : "advanced_flint_multitool";
					ModAdvancements.awardAll(serverPlayer, advancement);
				}
			} else {
				level.setBlockAndUpdate(pos, state.setValue(BRANCHES, branches));
			}
			level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.6F, 0.8F);
		}
		return InteractionResult.SUCCESS;
	}

	public InteractionResult takeOne(BlockState state, Level level, BlockPos pos, Player player) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		ItemStack returned;
		if (state.getValue(BRANCHES) > 0) {
			returned = new ItemStack(ModItems.BURNT_BRANCH);
			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) return InteractionResult.FAIL;
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			level.setBlockAndUpdate(pos, state.setValue(BRANCHES, state.getValue(BRANCHES) - 1));
		} else {
			returned = switch (state.getValue(TYPE)) {
				case 0 -> new ItemStack(ModItems.FLINT_AXE_PIECES);
				case 1 -> new ItemStack(ModItems.FLINT_CUTTER_PIECE);
				default -> new ItemStack(ModItems.REFINED_FLINT_MULTITOOL_PIECE);
			};
			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) return InteractionResult.FAIL;
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			level.removeBlock(pos, false);
		}
		return InteractionResult.SUCCESS;
	}

	@Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) { return takeOne(state, level, pos, player); }
	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(TYPE, BRANCHES); }
}
