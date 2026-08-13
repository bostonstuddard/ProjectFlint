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

/** Ground assembly for 2 Burnt Twigs + 4 Plant Tool Bindings + 4 Very Refined Stone. */
public final class MultitoolAssemblyBlock extends SupportedGroundBlock {
	public static final IntegerProperty BURNT_TWIGS = IntegerProperty.create("burnt_twigs", 0, 2);
	public static final IntegerProperty BINDINGS = IntegerProperty.create("bindings", 0, 4);
	public static final IntegerProperty STONES = IntegerProperty.create("stones", 0, 4);

	public MultitoolAssemblyBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
			.setValue(BURNT_TWIGS, 0)
			.setValue(BINDINGS, 0)
			.setValue(STONES, 0));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		BlockState next = state;
		SoundEventsHolder sound = null;
		if (stack.is(ModItems.BURNT_TWIG) && state.getValue(BURNT_TWIGS) < 2) {
			next = state.setValue(BURNT_TWIGS, state.getValue(BURNT_TWIGS) + 1);
			sound = SoundEventsHolder.WOOD;
		} else if (stack.is(ModItems.PLANT_TOOL_BINDING) && state.getValue(BINDINGS) < 4) {
			next = state.setValue(BINDINGS, state.getValue(BINDINGS) + 1);
			sound = SoundEventsHolder.BINDING;
		} else if (stack.is(ModItems.VERY_REFINED_STONE) && state.getValue(STONES) < 4) {
			next = state.setValue(STONES, state.getValue(STONES) + 1);
			sound = SoundEventsHolder.STONE;
		} else {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			stack.consume(1, player);
			if (isComplete(next)) {
				level.removeBlock(pos, false);
				PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.CRUDE_MULTITOOL));
				level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 0.72F);
				if (player instanceof ServerPlayer serverPlayer) {
					ModAdvancements.award(serverPlayer, "crude_multitool", "assembled_multitool");
				}
			} else {
				level.setBlockAndUpdate(pos, next);
				level.playSound(null, pos, sound.event, SoundSource.BLOCKS, 0.45F, sound.pitch);
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
			BlockState next = state;
			if (state.getValue(STONES) > 0) {
				returned = new ItemStack(ModItems.VERY_REFINED_STONE);
				next = state.setValue(STONES, state.getValue(STONES) - 1);
			} else if (state.getValue(BINDINGS) > 0) {
				returned = new ItemStack(ModItems.PLANT_TOOL_BINDING);
				next = state.setValue(BINDINGS, state.getValue(BINDINGS) - 1);
			} else if (state.getValue(BURNT_TWIGS) > 0) {
				returned = new ItemStack(ModItems.BURNT_TWIG);
				next = state.setValue(BURNT_TWIGS, state.getValue(BURNT_TWIGS) - 1);
			} else {
				return InteractionResult.FAIL;
			}

			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) {
				return InteractionResult.FAIL;
			}
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			if (next.getValue(STONES) == 0 && next.getValue(BINDINGS) == 0 && next.getValue(BURNT_TWIGS) == 0) {
				level.removeBlock(pos, false);
			} else {
				level.setBlockAndUpdate(pos, next);
			}
		}
		return InteractionResult.SUCCESS;
	}

	public BlockState stateForFirstIngredient(ItemStack stack) {
		BlockState state = defaultBlockState();
		if (stack.is(ModItems.BURNT_TWIG)) {
			return state.setValue(BURNT_TWIGS, 1);
		}
		if (stack.is(ModItems.PLANT_TOOL_BINDING)) {
			return state.setValue(BINDINGS, 1);
		}
		if (stack.is(ModItems.VERY_REFINED_STONE)) {
			return state.setValue(STONES, 1);
		}
		return state;
	}

	private static boolean isComplete(BlockState state) {
		return state.getValue(BURNT_TWIGS) == 2
			&& state.getValue(BINDINGS) == 4
			&& state.getValue(STONES) == 4;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 3.2D, 15.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BURNT_TWIGS, BINDINGS, STONES);
	}

	private enum SoundEventsHolder {
		WOOD(SoundEvents.WOOD_PLACE, 0.78F),
		BINDING(SoundEvents.WOOL_PLACE, 1.08F),
		STONE(SoundEvents.STONE_PLACE, 1.15F);

		private final net.minecraft.sounds.SoundEvent event;
		private final float pitch;

		SoundEventsHolder(net.minecraft.sounds.SoundEvent event, float pitch) {
			this.event = event;
			this.pitch = pitch;
		}
	}
}
