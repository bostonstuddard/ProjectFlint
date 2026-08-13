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

/** Physical assembly shared by the Reed Backpack and Bone Brush. */
public final class WovenGearAssemblyBlock extends SupportedGroundBlock {
	public static final IntegerProperty FANS = IntegerProperty.create("fans", 0, 8);
	public static final IntegerProperty BINDINGS = IntegerProperty.create("bindings", 0, 2);
	public static final IntegerProperty WOOL = IntegerProperty.create("wool", 0, 2);
	public static final IntegerProperty FILED_BONES = IntegerProperty.create("filed_bones", 0, 4);

	public WovenGearAssemblyBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FANS, 0).setValue(BINDINGS, 0).setValue(WOOL, 0).setValue(FILED_BONES, 0));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		BlockState next = state;
		if (stack.is(ModItems.WOVEN_FAN) && state.getValue(FANS) < 8) {
			next = state.setValue(FANS, state.getValue(FANS) + 1);
		} else if (stack.is(ModItems.PLANT_TOOL_BINDING) && state.getValue(BINDINGS) < 2) {
			next = state.setValue(BINDINGS, state.getValue(BINDINGS) + 1);
		} else if (stack.is(ModItems.WOVEN_WOOL) && state.getValue(WOOL) < 2) {
			next = state.setValue(WOOL, state.getValue(WOOL) + 1);
		} else if (stack.is(ModItems.FILED_BONE) && state.getValue(FILED_BONES) < 4) {
			next = state.setValue(FILED_BONES, state.getValue(FILED_BONES) + 1);
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
					String advancement = result.is(ModItems.REED_BACKPACK) ? "reed_backpack" : "bone_brush";
					ModAdvancements.award(serverPlayer, advancement, "assembled_" + advancement);
				}
			} else {
				level.setBlockAndUpdate(pos, next);
				level.playSound(null, pos, stack.is(ModItems.FILED_BONE) ? SoundEvents.BONE_BLOCK_PLACE : SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.48F, 0.94F);
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
			if (state.getValue(FILED_BONES) > 0) {
				returned = new ItemStack(ModItems.FILED_BONE);
				next = state.setValue(FILED_BONES, state.getValue(FILED_BONES) - 1);
			} else if (state.getValue(WOOL) > 0) {
				returned = new ItemStack(ModItems.WOVEN_WOOL);
				next = state.setValue(WOOL, state.getValue(WOOL) - 1);
			} else if (state.getValue(BINDINGS) > 0) {
				returned = new ItemStack(ModItems.PLANT_TOOL_BINDING);
				next = state.setValue(BINDINGS, state.getValue(BINDINGS) - 1);
			} else if (state.getValue(FANS) > 0) {
				returned = new ItemStack(ModItems.WOVEN_FAN);
				next = state.setValue(FANS, state.getValue(FANS) - 1);
			} else {
				return InteractionResult.FAIL;
			}

			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) return InteractionResult.FAIL;
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			if (isEmpty(next)) level.removeBlock(pos, false);
			else level.setBlockAndUpdate(pos, next);
		}
		return InteractionResult.SUCCESS;
	}

	public BlockState stateForFirstIngredient(ItemStack stack) {
		BlockState state = defaultBlockState();
		if (stack.is(ModItems.WOVEN_FAN)) return state.setValue(FANS, 1);
		if (stack.is(ModItems.PLANT_TOOL_BINDING)) return state.setValue(BINDINGS, 1);
		if (stack.is(ModItems.WOVEN_WOOL)) return state.setValue(WOOL, 1);
		if (stack.is(ModItems.FILED_BONE)) return state.setValue(FILED_BONES, 1);
		return state;
	}

	private static ItemStack resultFor(BlockState state) {
		int fans = state.getValue(FANS);
		int bindings = state.getValue(BINDINGS);
		int wool = state.getValue(WOOL);
		int bones = state.getValue(FILED_BONES);
		if (fans == 8 && bindings == 2 && wool == 2 && bones == 0) return new ItemStack(ModItems.REED_BACKPACK);
		if (fans == 4 && bindings == 0 && wool == 0 && bones == 4) return new ItemStack(ModItems.BONE_BRUSH);
		return ItemStack.EMPTY;
	}

	private static boolean isEmpty(BlockState state) {
		return state.getValue(FANS) == 0 && state.getValue(BINDINGS) == 0 && state.getValue(WOOL) == 0 && state.getValue(FILED_BONES) == 0;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 3.0D, 15.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FANS, BINDINGS, WOOL, FILED_BONES);
	}
}
