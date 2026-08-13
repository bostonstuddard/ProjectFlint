package dev.projectflint.block;

import dev.projectflint.config.GameplayTuning;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import dev.projectflint.system.PrimitiveInventorySystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ClayGrindingBaseBlock extends Block {
	public static final IntegerProperty STONES = IntegerProperty.create("stones", 0, GameplayTuning.CLAY_GRINDER_STONE_REQUIREMENT);

	public ClayGrindingBaseBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(STONES, 0));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!stack.is(ModItems.VERY_REFINED_STONE)) return InteractionResult.PASS;
		int stones = state.getValue(STONES);
		if (stones >= GameplayTuning.CLAY_GRINDER_STONE_REQUIREMENT) return InteractionResult.SUCCESS;
		if (!level.isClientSide()) {
			stack.consume(1, player);
			stones++;
			if (stones >= GameplayTuning.CLAY_GRINDER_STONE_REQUIREMENT) {
				level.setBlockAndUpdate(pos, ModBlocks.CLAY_GRINDER.defaultBlockState()
					.setValue(HorizontalDirectionalBlock.FACING, player.getDirection().getOpposite()));
				level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.7F, 0.88F);
				if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
					ModAdvancements.award(serverPlayer, "clay_grinder", "assembled_clay_grinder");
				}
			} else {
				level.setBlockAndUpdate(pos, state.setValue(STONES, stones));
				level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.45F, 1.02F);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		int stones = state.getValue(STONES);
		if (stones <= 0) return InteractionResult.PASS;
		if (!level.isClientSide()) {
			ItemStack returned = new ItemStack(ModItems.VERY_REFINED_STONE);
			if (!PrimitiveInventorySystem.canFullyInsertIntoPrimitiveInventory(player, returned)) return InteractionResult.FAIL;
			PrimitiveInventorySystem.insertIntoPrimitiveInventory(player, returned);
			level.setBlockAndUpdate(pos, state.setValue(STONES, stones - 1));
		}
		return InteractionResult.SUCCESS;
	}


	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean isMoving) {
		if (!state.canSurvive(level, pos)) {
			level.destroyBlock(pos, true);
			return;
		}
		super.neighborChanged(state, level, pos, block, orientation, isMoving);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STONES);
	}
}
