package dev.projectflint.menu;

import dev.projectflint.block.entity.WellPulleyBlockEntity;
import dev.projectflint.compat.flintquests.FlintQuestsBridge;
import dev.projectflint.registry.ModItems;
import dev.projectflint.registry.ModMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class WellPulleyMenu extends AbstractContainerMenu {
	private final Container container;
	private final ContainerData data;

	public WellPulleyMenu(int syncId, Inventory inventory) {
		this(syncId, inventory, new SimpleContainer(WellPulleyBlockEntity.SLOT_COUNT), new SimpleContainerData(5));
	}

	public WellPulleyMenu(int syncId, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.WELL_PULLEY, syncId);
		checkContainerSize(container, WellPulleyBlockEntity.SLOT_COUNT);
		checkContainerDataCount(data, 5);
		this.container = container;
		this.data = data;
		container.startOpen(inventory.player);

		addSlot(new Slot(container, WellPulleyBlockEntity.ROPE_SLOT, 17, 44) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(ModItems.ROPE);
			}

			@Override
			public int getMaxStackSize() {
				return 16;
			}
		});

		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 4; col++) {
				int slot = WellPulleyBlockEntity.OUTPUT_START + row * 4 + col;
				addSlot(new Slot(container, slot, 96 + col * 18, 26 + row * 18) {
					@Override
					public boolean mayPlace(ItemStack stack) {
						return false;
					}

					@Override
					public void onTake(Player player, ItemStack stack) {
						if (player instanceof ServerPlayer serverPlayer) FlintQuestsBridge.onCraft(serverPlayer, "well", stack);
						super.onTake(player, stack);
					}
				});
			}
		}

		addStandardInventorySlots(inventory, 8, 84);
		addDataSlots(data);
	}

	public int progressPixels(int width) {
		int progress = data.get(0);
		int max = data.get(1);
		return max <= 0 ? 0 : Math.min(width, progress * width / max);
	}

	public int qualityPoints() {
		return data.get(2);
	}

	public String yieldLabel() {
		return switch (data.get(3)) {
			case 0 -> "Scarce";
			case 1 -> "Normal";
			case 2 -> "Good";
			case 3 -> "Abundant";
			default -> "Inactive";
		};
	}

	public boolean structureValid() {
		return data.get(4) != 0;
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
		int machineSlots = WellPulleyBlockEntity.SLOT_COUNT;

		if (index < machineSlots) {
			if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
		} else if (stack.is(ModItems.ROPE)) {
			if (!moveItemStackTo(stack, WellPulleyBlockEntity.ROPE_SLOT, WellPulleyBlockEntity.ROPE_SLOT + 1, false)) return ItemStack.EMPTY;
		} else {
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
