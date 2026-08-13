package dev.projectflint.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class FlintAxeItem extends Item {
	public FlintAxeItem(Properties properties) { super(properties); }
	@Override public boolean isBarVisible(ItemStack stack) { return stack.isDamaged(); }
}
