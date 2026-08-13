package dev.projectflint.block;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import dev.projectflint.system.PrimitiveInventorySystem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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

public final class RockPileBlock extends SupportedGroundBlock {
	public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, GameplayTuning.ROCK_PILE_CAPACITY);

	public RockPileBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(COUNT, 1));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide() && placer instanceof ServerPlayer serverPlayer) {
			ModAdvancements.award(serverPlayer, "stone_stockpile", "placed_stone_stockpile");
		}
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.is(ModItems.ROCK)) {
			int count = state.getValue(COUNT);
			if (count >= GameplayTuning.ROCK_PILE_CAPACITY) {
				return InteractionResult.SUCCESS;
			}
			if (!level.isClientSide()) {
				if (!player.isCreative()) {
					stack.shrink(1);
				}
				level.setBlockAndUpdate(pos, state.setValue(COUNT, count + 1));
				level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.45F, 1.05F);
			}
			return InteractionResult.SUCCESS;
		}
		return takeOne(state, level, pos, player);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return takeOne(state, level, pos, player);
	}

	public InteractionResult takeOne(BlockState state, Level level, BlockPos pos, Player player) {
		if (!level.isClientSide()) {
			ItemStack returned = new ItemStack(ModItems.ROCK);
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
			level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.4F, 0.85F);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		double height = 1.0D + ((state.getValue(COUNT) - 1) / 4) * 0.8D;
		return Block.box(2.0D, 0.0D, 2.0D, 14.0D, height, 14.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COUNT);
	}
}
