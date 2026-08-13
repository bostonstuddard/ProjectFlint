package dev.projectflint.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AdvancedFlintMultitoolItem extends Item {
	public AdvancedFlintMultitoolItem(Properties properties) { super(properties); }
	@Override public boolean isBarVisible(ItemStack stack) { return stack.isDamaged(); }
}
