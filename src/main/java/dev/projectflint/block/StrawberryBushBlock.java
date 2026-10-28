package dev.projectflint.block;

import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Four-stage strawberry plant with Sweet Berry Bush-style growth/harvesting.
 * Strawberries are not thorny, so entering the plant does not slow or damage entities.
 */
public final class StrawberryBushBlock extends SweetBerryBushBlock {
	public StrawberryBushBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(ModItems.STRAWBERRY_SEEDS);
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean moving) {
		// Strawberry plants reuse berry-bush growth, but intentionally have no thorn slowdown/damage.
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		int age = state.getValue(AGE);
		if (!player.isCreative() || age <= 1) return InteractionResult.FAIL;

		if (!level.isClientSide()) {
			int strawberries = 1 + level.getRandom().nextInt(2);
			level.setBlockAndUpdate(pos, state.setValue(AGE, 1));
			Block.popResource(level, pos, new ItemStack(ModItems.STRAWBERRY, strawberries));
			level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 0.8F, 1.05F);
		}
		return InteractionResult.SUCCESS;
	}
}
