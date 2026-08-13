package dev.projectflint.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Durable primitive ignition tool made from branches and vanilla flint. */
public final class FlintFirestarterItem extends Item {
	public FlintFirestarterItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return stack.isDamaged();
	}
}
