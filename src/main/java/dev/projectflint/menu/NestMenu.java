package dev.projectflint.menu;

import dev.projectflint.block.entity.NestBlockEntity;
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

public final class NestMenu extends AbstractContainerMenu {
	public static final int NEST_SLOTS = NestBlockEntity.MAX_EGGS;
	private final Container container;
	private final ContainerData data;

	public NestMenu(int syncId, Inventory inventory) {
		this(syncId, inventory, new SimpleContainer(NEST_SLOTS), new SimpleContainerData(1));
	}

	public NestMenu(int syncId, Inventory inventory, NestBlockEntity nest) {
		this(syncId, inventory, nest, new ContainerData() {
			@Override
			public int get(int index) {
				return index == 0 && nest.hasOwner() ? 1 : 0;
			}

			@Override
			public void set(int index, int value) {
				// Occupancy is server-authoritative and follows the assigned chicken owner.
			}

			@Override
			public int getCount() {
				return 1;
			}
		});
	}

	private NestMenu(int syncId, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.NEST, syncId);
		checkContainerSize(container, NEST_SLOTS);
		checkContainerDataCount(data, 1);
		this.container = container;
		this.data = data;
		container.startOpen(inventory.player);

		for (int slot = 0; slot < NEST_SLOTS; slot++) {
			addSlot(new Slot(container, slot, 52 + slot * 18, 32) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return stack.is(Items.EGG);
				}

				@Override
				public int getMaxStackSize() {
					return 1;
				}
			});
		}

		addStandardInventorySlots(inventory, 8, 77);
		addDataSlots(data);
	}

	public boolean hasOccupant() {
		return data.get(0) != 0;
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
		if (index < NEST_SLOTS) {
			if (!moveItemStackTo(stack, NEST_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
		} else {
			if (!stack.is(Items.EGG)) return ItemStack.EMPTY;
			if (!moveItemStackTo(stack, 0, NEST_SLOTS, false)) return ItemStack.EMPTY;
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
