package dev.projectflint.item;

import dev.projectflint.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;

/** Water-filled clay bucket. Placement uses vanilla water-bucket behavior and returns the clay vessel. */
public final class ClayWaterBucketItem extends BucketItem {
	public ClayWaterBucketItem(Properties properties) {
		super(Fluids.WATER, properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		InteractionResult result = super.use(level, player, hand);
		if (player.hasInfiniteMaterials()) return result;
		if (!(result instanceof InteractionResult.Success success)) return result;

		ItemStack transformed = success.heldItemTransformedTo();
		if (transformed == null || transformed.isEmpty()) return result;
		return success.heldItemTransformedTo(new ItemStack(ModItems.CLAY_BUCKET));
	}
}
