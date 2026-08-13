package dev.projectflint.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * First crude vegetation-processing cutter. It has a visible 48-use durability
 * bar and carries its combat attributes through the standard item components.
 */
public final class CrudeHarvestingToolItem extends Item {
	public CrudeHarvestingToolItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return stack.isDamaged();
	}
}
