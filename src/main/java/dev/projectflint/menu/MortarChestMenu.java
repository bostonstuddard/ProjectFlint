package dev.projectflint.menu;

import dev.projectflint.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MortarChestMenu extends AbstractContainerMenu {
	public static final int STORAGE_SLOTS = 3;
	private final Container container;

	public MortarChestMenu(int syncId, Inventory inventory) {
		this(syncId, inventory, new SimpleContainer(STORAGE_SLOTS));
	}

	public MortarChestMenu(int syncId, Inventory inventory, Container container) {
		super(ModMenus.MORTAR_CHEST, syncId);
		checkContainerSize(container, STORAGE_SLOTS);
		this.container = container;
		container.startOpen(inventory.player);

		for (int slot = 0; slot < STORAGE_SLOTS; slot++) {
			addSlot(new Slot(container, slot, 61 + slot * 18, 25));
		}

		addStandardInventorySlots(inventory, 8, 65);
	}

	@Override
	public boolean stillValid(Player player) {
		return container.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack empty = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (!slot.hasItem()) return empty;

		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();
		if (index < STORAGE_SLOTS) {
			if (!moveItemStackTo(stack, STORAGE_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
		} else if (!moveItemStackTo(stack, 0, STORAGE_SLOTS, false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
		else slot.setChanged();
		return copy;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		container.stopOpen(player);
	}
}
