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

/**
 * Physical assembly for the first dedicated vegetation-harvesting tool.
 *
 * The assembly begins when a Plant Tool Binding is placed, then accepts:
 *  - 2 Very Refined Stone
 *  - 2 Plant Tool Bindings total
 *  - 1 Lightly Burnt Twig
 *
 * A completed assembly is worked with the Crude Stone Hammer.
 */
public final class HarvestingToolAssemblyBlock extends SupportedGroundBlock {
	public static final IntegerProperty STONES = IntegerProperty.create("stones", 0, 2);
	public static final IntegerProperty BINDINGS = IntegerProperty.create("bindings", 1, 2);
	public static final BooleanProperty HAS_TWIG = BooleanProperty.create("has_twig");

	public HarvestingToolAssemblyBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
			.setValue(STONES, 0)
			.setValue(BINDINGS, 1)
			.setValue(HAS_TWIG, false));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.is(ModItems.VERY_REFINED_STONE)) {
			int stones = state.getValue(STONES);
			if (stones >= 2) {
				return InteractionResult.SUCCESS;
			}
			if (!level.isClientSide()) {
				stack.consume(1, player);
				level.setBlockAndUpdate(pos, state.setValue(STONES, stones + 1));
				level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.55F, 1.18F);
			}
			return InteractionResult.SUCCESS;
		}

		if (stack.is(ModItems.PLANT_TOOL_BINDING)) {
			int bindings = state.getValue(BINDINGS);
			if (bindings >= 2) {
				return InteractionResult.SUCCESS;
			}
			if (!level.isClientSide()) {
				stack.consume(1, player);
				level.setBlockAndUpdate(pos, state.setValue(BINDINGS, bindings + 1));
				level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.45F, 1.12F);
			}
			return InteractionResult.SUCCESS;
		}

		if (stack.is(ModItems.LIGHTLY_BURNT_TWIG)) {
			if (state.getValue(HAS_TWIG)) {
				return InteractionResult.SUCCESS;
			}
			if (!level.isClientSide()) {
				stack.consume(1, player);
				level.setBlockAndUpdate(pos, state.setValue(HAS_TWIG, true));
				level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.5F, 0.76F);
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
			BlockState nextState = state;
			boolean removeBlock = false;

			if (state.getValue(HAS_TWIG)) {
				returned = new ItemStack(ModItems.LIGHTLY_BURNT_TWIG);
				nextState = state.setValue(HAS_TWIG, false);
			} else if (state.getValue(STONES) > 0) {
				returned = new ItemStack(ModItems.VERY_REFINED_STONE);
				nextState = state.setValue(STONES, state.getValue(STONES) - 1);
			} else {
				returned = new ItemStack(ModItems.PLANT_TOOL_BINDING);
				int bindings = state.getValue(BINDINGS);
				if (bindings <= 1) {
					removeBlock = true;
				} else {
					nextState = state.setValue(BINDINGS, bindings - 1);
				}
			}

			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) {
				return InteractionResult.FAIL;
			}

			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			if (removeBlock) {
				level.removeBlock(pos, false);
			} else {
				level.setBlockAndUpdate(pos, nextState);
			}
		}
		return InteractionResult.SUCCESS;
	}

	public InteractionResult hitWithHammer(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		ItemStack hammer = player.getItemInHand(hand);
		if (!ModItems.isHammerLike(hammer)
			|| state.getValue(STONES) != 2
			|| state.getValue(BINDINGS) != 2
			|| !state.getValue(HAS_TWIG)) {
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			hammer.hurtAndBreak(1, player, hand);
			level.removeBlock(pos, false);
			PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.CRUDE_HARVESTING_TOOL));
			level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.8F, 0.86F);
			if (player instanceof ServerPlayer serverPlayer) {
				ModAdvancements.award(serverPlayer, "crude_harvesting_tool", "made_crude_harvesting_tool");
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		double height = 1.0D;
		height += state.getValue(STONES) * 0.45D;
		height += (state.getValue(BINDINGS) - 1) * 0.3D;
		height += state.getValue(HAS_TWIG) ? 0.45D : 0.0D;
		return Block.box(1.0D, 0.0D, 1.0D, 15.0D, Math.min(height, 3.0D), 15.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STONES, BINDINGS, HAS_TWIG);
	}
}
