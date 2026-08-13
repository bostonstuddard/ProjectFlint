package dev.projectflint.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Second primitive multitool tier, retaining the full crude multitool action set. */
public final class FlintMultitoolItem extends Item {
	public FlintMultitoolItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return stack.isDamaged();
	}
}
