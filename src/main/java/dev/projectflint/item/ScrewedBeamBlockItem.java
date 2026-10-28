package dev.projectflint.item;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

/** Survival beam placement consumes one Wooden Screw from the off hand. */
public final class ScrewedBeamBlockItem extends BlockItem {
	public ScrewedBeamBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public InteractionResult place(BlockPlaceContext context) {
		Player player = context.getPlayer();
		if (player == null || player.isCreative()) return super.place(context);
		if (context.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.FAIL;

		ItemStack screw = player.getOffhandItem();
		if (!screw.is(ModItems.WOODEN_SCREW)) return InteractionResult.FAIL;

		InteractionResult result = super.place(context);
		if (result.consumesAction()) screw.shrink(1);
		return result;
	}
}
