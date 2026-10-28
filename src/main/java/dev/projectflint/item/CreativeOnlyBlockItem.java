package dev.projectflint.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

/** Block item visible in creative but intentionally not placeable in survival yet. */
public final class CreativeOnlyBlockItem extends BlockItem {
	public CreativeOnlyBlockItem(Block block, Properties properties) { super(block, properties); }

	@Override
	public InteractionResult place(BlockPlaceContext context) {
		Player player = context.getPlayer();
		if (player != null && !player.isCreative()) return InteractionResult.FAIL;
		return super.place(context);
	}
}
