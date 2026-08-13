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

/** Four fired-clay pieces can be piled and hammered into one movable clay block. */
public final class FiredClayPileBlock extends SupportedGroundBlock {
	public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, GameplayTuning.FIRED_CLAY_PER_BLOCK);

	public FiredClayPileBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(COUNT, 1));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!stack.is(ModItems.FIRED_CLAY)) return InteractionResult.PASS;
		int count = state.getValue(COUNT);
		if (count >= GameplayTuning.FIRED_CLAY_PER_BLOCK) return InteractionResult.SUCCESS;
		if (!level.isClientSide()) {
			stack.consume(1, player);
			level.setBlockAndUpdate(pos, state.setValue(COUNT, count + 1));
			level.playSound(null, pos, SoundEvents.DECORATED_POT_PLACE, SoundSource.BLOCKS, 0.45F, 0.82F);
		}
		return InteractionResult.SUCCESS;
	}

	public InteractionResult takeOne(BlockState state, Level level, BlockPos pos, Player player) {
		if (!level.isClientSide()) {
			ItemStack returned = new ItemStack(ModItems.FIRED_CLAY);
			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) return InteractionResult.FAIL;
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			int count = state.getValue(COUNT);
			if (count <= 1) level.removeBlock(pos, false);
			else level.setBlockAndUpdate(pos, state.setValue(COUNT, count - 1));
		}
		return InteractionResult.SUCCESS;
	}

	public InteractionResult hitWithHammer(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		ItemStack tool = player.getItemInHand(hand);
		if (!ModItems.isHammerLike(tool) || state.getValue(COUNT) != GameplayTuning.FIRED_CLAY_PER_BLOCK) return InteractionResult.FAIL;
		if (!level.isClientSide()) {
			tool.hurtAndBreak(1, player, hand);
			level.removeBlock(pos, false);
			PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.CRUDE_CLAY_BLOCK));
			level.playSound(null, pos, SoundEvents.DECORATED_POT_BREAK, SoundSource.BLOCKS, 0.7F, 0.72F);
			if (player instanceof ServerPlayer serverPlayer) ModAdvancements.award(serverPlayer, "crude_clay_block", "hammered_clay_block");
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(2.0D, 0.0D, 2.0D, 14.0D, 0.8D + state.getValue(COUNT) * 0.55D, 14.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COUNT);
	}
}
