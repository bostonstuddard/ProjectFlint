package dev.projectflint.menu;

import dev.projectflint.block.entity.FireclayForgeBlockEntity;
import dev.projectflint.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Recipe input, fuel, firestarter, independent recipe output and charcoal byproduct; hidden old-save fuel refund. */
public final class FireclayForgeMenu extends AbstractContainerMenu {
	private static final int VISIBLE_SLOTS = 5;
	private final Container container;
	private final ContainerData data;

	public FireclayForgeMenu(int id, Inventory inventory) {
		this(id, inventory, new SimpleContainer(FireclayForgeBlockEntity.SLOT_COUNT), new SimpleContainerData(5));
	}
	public FireclayForgeMenu(int id, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.FIRECLAY_FORGE, id);
		checkContainerSize(container, FireclayForgeBlockEntity.SLOT_COUNT);
		checkContainerDataCount(data, 5);
		this.container = container;
		this.data = data;
		container.startOpen(inventory.player);
		addSlot(new Slot(container, FireclayForgeBlockEntity.INPUT_SLOT, 17, 34));
		addSlot(new Slot(container, FireclayForgeBlockEntity.FUEL_SLOT, 44, 34) {
			@Override public boolean mayPlace(ItemStack stack) { return FireclayForgeBlockEntity.isFuel(stack); }
		});
		addSlot(new Slot(container, FireclayForgeBlockEntity.IGNITER_SLOT, 71, 34) {
			@Override public boolean mayPlace(ItemStack stack) { return FireclayForgeBlockEntity.isFirestarter(stack); }
		});
		addSlot(new Slot(container, FireclayForgeBlockEntity.PRIMARY_OUTPUT_SLOT, 130, 34) {
			@Override public boolean mayPlace(ItemStack stack) { return false; }
		});
		addSlot(new Slot(container, FireclayForgeBlockEntity.OUTPUT_SLOT, 158, 34) {
			@Override public boolean mayPlace(ItemStack stack) { return false; }
		});
		addStandardInventorySlots(inventory, 14, 97);
		addDataSlots(data);
	}
	@Override public boolean clickMenuButton(Player player, int id) {
		if (id != 0 || !(container instanceof FireclayForgeBlockEntity forge)) return false;
		return forge.ignite();
	}
	public boolean formed() { return data.get(2) != 0; }
	public boolean lit() { return data.get(3) != 0; }
	public int remainingSeconds() { return Math.max(0, (data.get(0) + 19) / 20); }
	public int burnPixels(int width) { return Math.min(width, Math.max(0, data.get(0)) * width / Math.max(1, data.get(1))); }
	@Override public boolean stillValid(Player player) { return container.stillValid(player); }
	@Override public ItemStack quickMoveStack(Player player, int index) {
		if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (!slot.hasItem()) return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		if (index < VISIBLE_SLOTS) {
			if (!moveItemStackTo(stack, VISIBLE_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
		} else if (FireclayForgeBlockEntity.isFirestarter(stack)) {
			if (!moveItemStackTo(stack, FireclayForgeBlockEntity.IGNITER_SLOT, FireclayForgeBlockEntity.IGNITER_SLOT + 1, false)) return ItemStack.EMPTY;
		} else if (FireclayForgeBlockEntity.isFuel(stack)) {
			if (!moveItemStackTo(stack, FireclayForgeBlockEntity.FUEL_SLOT, FireclayForgeBlockEntity.FUEL_SLOT + 1, false)) return ItemStack.EMPTY;
		} else if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
		if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
		else slot.setChanged();
		return original;
	}
	@Override public void removed(Player player) {
		super.removed(player);
		container.stopOpen(player);
	}
}
