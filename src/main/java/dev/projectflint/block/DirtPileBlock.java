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

public final class DirtPileBlock extends SupportedGroundBlock {
	public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, GameplayTuning.DIRT_PILE_COMPACTION_COUNT);

	public DirtPileBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(COUNT, 1));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!stack.is(ModItems.DIRT_PILE)) {
			return InteractionResult.PASS;
		}
		int count = state.getValue(COUNT);
		if (count >= GameplayTuning.DIRT_PILE_COMPACTION_COUNT) {
			return InteractionResult.SUCCESS;
		}
		if (!level.isClientSide()) {
			stack.consume(1, player);
			level.setBlockAndUpdate(pos, state.setValue(COUNT, count + 1));
			level.playSound(null, pos, SoundEvents.ROOTED_DIRT_PLACE, SoundSource.BLOCKS, 0.4F, 0.9F);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return takeOne(state, level, pos, player);
	}

	public InteractionResult takeOne(BlockState state, Level level, BlockPos pos, Player player) {
		if (!level.isClientSide()) {
			ItemStack returned = new ItemStack(ModItems.DIRT_PILE);
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

	public InteractionResult hitWithHammer(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
		ItemStack hammer = player.getItemInHand(hand);
		if (!ModItems.isHammerLike(hammer) || state.getValue(COUNT) != GameplayTuning.DIRT_PILE_COMPACTION_COUNT) {
			return InteractionResult.FAIL;
		}
		if (!level.isClientSide()) {
			hammer.hurtAndBreak(1, player, hand);
			level.removeBlock(pos, false);
			PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.LOOSE_DIRT));
			level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.75F, 0.72F);
			if (player instanceof ServerPlayer serverPlayer) {
				ModAdvancements.award(serverPlayer, "loose_dirt", "compacted_loose_dirt");
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		double height = Math.min(8.0D, state.getValue(COUNT) * 2.0D);
		return Block.box(2.0D, 0.0D, 2.0D, 14.0D, height, 14.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COUNT);
	}
}
