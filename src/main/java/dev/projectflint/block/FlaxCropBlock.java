package dev.projectflint.block;

import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Flax crop using the supplied eight-stage crop art. Mature flax is Project Flint's mob-free String source. */
public final class FlaxCropBlock extends CropBlock {
	public FlaxCropBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected ItemLike getBaseSeedId() {
		return ModItems.FLAX_SEEDS;
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
		super.playerDestroy(level, player, pos, state, blockEntity, tool);
		if (level.isClientSide()) return;

		if (isMaxAge(state)) {
			Block.popResource(level, pos, new ItemStack(Items.STRING));
			int seeds = 1 + (level.random.nextFloat() < 0.10F ? 1 : 0);
			Block.popResource(level, pos, new ItemStack(ModItems.FLAX_SEEDS, seeds));
		} else {
			// Preserve the seed when an immature crop is removed so accidental early breaks are not progression traps.
			Block.popResource(level, pos, new ItemStack(ModItems.FLAX_SEEDS));
		}
	}
}
