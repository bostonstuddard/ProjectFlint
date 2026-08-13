package dev.projectflint.menu;

import dev.projectflint.block.entity.AdvancedKilnBlockEntity;
import dev.projectflint.registry.ModItems;
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
import net.minecraft.world.item.Items;

public final class AdvancedKilnMenu extends AbstractContainerMenu {
	private static final int KILN_SLOTS = AdvancedKilnBlockEntity.SLOT_COUNT;
	private final Container container;
	private final ContainerData data;

	public AdvancedKilnMenu(int syncId, Inventory inventory) {
		this(syncId, inventory, new SimpleContainer(KILN_SLOTS), new SimpleContainerData(2));
	}

	public AdvancedKilnMenu(int syncId, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.ADVANCED_KILN, syncId);
		checkContainerSize(container, KILN_SLOTS);
		checkContainerDataCount(data, 2);
		this.container = container;
		this.data = data;
		container.startOpen(inventory.player);

		addSlot(inputSlot(container, data, AdvancedKilnBlockEntity.BRANCH_SLOT, 14, 33, 8));
		addSlot(inputSlot(container, data, AdvancedKilnBlockEntity.MOLD_SLOT, 38, 33, 1));
		addSlot(inputSlot(container, data, AdvancedKilnBlockEntity.FLINT_SLOT, 62, 33, 8));
		addSlot(inputSlot(container, data, AdvancedKilnBlockEntity.FIRED_CLAY_SLOT, 86, 33, 4));
		addSlot(new Slot(container, AdvancedKilnBlockEntity.OUTPUT_SLOT, 144, 33) {
			@Override public boolean mayPlace(ItemStack stack) { return false; }
		});

		addStandardInventorySlots(inventory, 8, 84);
		addDataSlots(data);
	}

	private static Slot inputSlot(Container container, ContainerData data, int index, int x, int y, int limit) {
		return new Slot(container, index, x, y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				if (data.get(0) > 0) return false;
				return switch (index) {
					case AdvancedKilnBlockEntity.BRANCH_SLOT -> stack.is(ModItems.BURNT_BRANCH) || stack.is(ModItems.BRANCH);
					case AdvancedKilnBlockEntity.MOLD_SLOT -> AdvancedKilnBlockEntity.isFiredMold(stack);
					case AdvancedKilnBlockEntity.FLINT_SLOT -> stack.is(Items.FLINT);
					case AdvancedKilnBlockEntity.FIRED_CLAY_SLOT -> stack.is(ModItems.FIRED_CLAY);
					default -> false;
				};
			}

			@Override
			public boolean mayPickup(Player player) {
				return data.get(0) <= 0;
			}

			@Override
			public int getMaxStackSize(ItemStack stack) {
				return Math.min(limit, super.getMaxStackSize(stack));
			}
		};
	}

	public int progressPixels(int width) {
		int remaining = data.get(0);
		int max = data.get(1);
		if (max <= 0 || remaining <= 0) return 0;
		int elapsed = max - remaining + 1;
		return Math.min(width, Math.max(0, elapsed * width / max));
	}

	public boolean isProcessing() {
		return data.get(0) > 0;
	}

	@Override
	public boolean stillValid(Player player) {
		return container.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();

		if (index < KILN_SLOTS) {
			if (!moveItemStackTo(stack, KILN_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
		} else {
			int destination = destinationSlot(stack);
			if (destination < 0 || !moveItemStackTo(stack, destination, destination + 1, false)) return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
		else slot.setChanged();
		return copy;
	}

	private static int destinationSlot(ItemStack stack) {
		if (stack.is(ModItems.BURNT_BRANCH) || stack.is(ModItems.BRANCH)) return AdvancedKilnBlockEntity.BRANCH_SLOT;
		if (AdvancedKilnBlockEntity.isFiredMold(stack)) return AdvancedKilnBlockEntity.MOLD_SLOT;
		if (stack.is(Items.FLINT)) return AdvancedKilnBlockEntity.FLINT_SLOT;
		if (stack.is(ModItems.FIRED_CLAY)) return AdvancedKilnBlockEntity.FIRED_CLAY_SLOT;
		return -1;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		container.stopOpen(player);
	}
}
