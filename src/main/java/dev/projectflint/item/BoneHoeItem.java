package dev.projectflint.item;

import dev.projectflint.registry.ModItems;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.system.PrimitiveInteractionSystem;
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

/** Primitive 24-use hoe. It tills supported Project Flint soils that directly border water. */
public final class BoneHoeItem extends Item {
	public BoneHoeItem(Properties properties) { super(properties); }

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		var soil = level.getBlockState(pos);
		boolean tillable = soil.is(Blocks.GRASS_BLOCK) || soil.is(ModBlocks.LOOSE_DIRT) || soil.is(ModBlocks.LOOSE_SOIL);
		if (player == null || context.getClickedFace() == Direction.DOWN || !tillable || !level.getBlockState(pos.above()).isAir()) {
			return InteractionResult.PASS;
		}
		boolean touchesWater = false;
		for (Direction direction : Direction.values()) {
			if (!level.getFluidState(pos.relative(direction)).isEmpty()) {
				if (level.getFluidState(pos.relative(direction)).is(net.minecraft.tags.FluidTags.WATER)) { touchesWater = true; break; }
			}
		}
		if (!touchesWater) return InteractionResult.FAIL;
		if (!level.isClientSide()) {
			level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
			if (level.random.nextFloat() < 0.10F) PrimitiveInteractionSystem.giveOrDrop(player, new ItemStack(ModItems.FLAX_SEEDS));
			context.getItemInHand().hurtAndBreak(1, player, context.getHand());
		}
		level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
		return InteractionResult.SUCCESS;
	}
}
