package dev.projectflint.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Project Flint seed item for deliberately establishing a vanilla Sweet Berry Bush. */
public final class SweetBerrySeedsItem extends Item {
	public SweetBerrySeedsItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos supportPos = context.getClickedPos();
		if (context.getClickedFace() != Direction.UP || !level.getBlockState(supportPos).is(Blocks.GRASS_BLOCK)) {
			return InteractionResult.PASS;
		}

		BlockPos plantPos = supportPos.above();
		BlockState bush = Blocks.SWEET_BERRY_BUSH.defaultBlockState();
		if (!level.getBlockState(plantPos).canBeReplaced() || !bush.canSurvive(level, plantPos)) {
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			level.setBlockAndUpdate(plantPos, bush);
			Player player = context.getPlayer();
			ItemStack seeds = context.getItemInHand();
			if (player == null || !player.isCreative()) seeds.shrink(1);
			level.playSound(null, plantPos, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
		}
		return InteractionResult.SUCCESS;
	}
}
