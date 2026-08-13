package dev.projectflint.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Primitive processing tool. Project Flint operations consume its 24 points of
 * durability explicitly. The durability bar is always visible so the player can
 * read remaining tool life before committing to a process.
 */
public final class CrudeStoneHammerItem extends Item {
	public CrudeStoneHammerItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return stack.isDamaged();
	}
}
