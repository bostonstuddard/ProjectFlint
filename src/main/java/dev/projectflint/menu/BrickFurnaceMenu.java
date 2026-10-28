package dev.projectflint.menu;

import dev.projectflint.block.entity.BrickFurnaceBlockEntity;
import dev.projectflint.block.entity.PrimitiveStoveBlockEntity;
import dev.projectflint.compat.flintquests.FlintQuestsBridge;
import dev.projectflint.registry.ModMenus;
import dev.projectflint.system.PrimitiveProcessingRegistry;
import dev.projectflint.system.PrimitiveProcessor;
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
import net.minecraft.world.item.Items;

public final class BrickFurnaceMenu extends AbstractContainerMenu {
	private final Container container;
	private final ContainerData data;
	private final boolean blastFurnace;

	public static BrickFurnaceMenu furnace(int syncId, Inventory inventory) {
		return new BrickFurnaceMenu(syncId, inventory, new SimpleContainer(BrickFurnaceBlockEntity.SLOT_COUNT), new SimpleContainerData(3), false);
	}

	public static BrickFurnaceMenu blastFurnace(int syncId, Inventory inventory) {
		return new BrickFurnaceMenu(syncId, inventory, new SimpleContainer(BrickFurnaceBlockEntity.SLOT_COUNT), new SimpleContainerData(3), true);
	}

	public BrickFurnaceMenu(int syncId, Inventory inventory, Container container, ContainerData data, boolean blastFurnace) {
		super(blastFurnace ? ModMenus.BRICK_BLAST_FURNACE : ModMenus.BRICK_FURNACE, syncId);
		checkContainerSize(container, BrickFurnaceBlockEntity.SLOT_COUNT);
		checkContainerDataCount(data, 3);
		this.container = container;
		this.data = data;
		this.blastFurnace = blastFurnace;
		container.startOpen(inventory.player);

		addSlot(new Slot(container, BrickFurnaceBlockEntity.FUEL_SLOT, 20, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(Items.CHARCOAL);
			}
		});
		addSlot(new Slot(container, BrickFurnaceBlockEntity.INPUT_SLOT, 56, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return PrimitiveProcessingRegistry.isProcessable(PrimitiveProcessor.KILN, stack) || (!blastFurnace && stack.is(Items.LEATHER));
			}
		});
		addOutputSlot(BrickFurnaceBlockEntity.OUTPUT_START, 112, 26);
		addOutputSlot(BrickFurnaceBlockEntity.OUTPUT_START + 1, 130, 26);
		addOutputSlot(BrickFurnaceBlockEntity.OUTPUT_START + 2, 112, 44);
		addOutputSlot(BrickFurnaceBlockEntity.OUTPUT_START + 3, 130, 44);
		addStandardInventorySlots(inventory, 8, 84);
		addDataSlots(data);
	}

	private void addOutputSlot(int index, int x, int y) {
		addSlot(new Slot(container, index, x, y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public void onTake(Player player, ItemStack stack) {
				if (player instanceof ServerPlayer serverPlayer) {
					FlintQuestsBridge.onCraft(serverPlayer, blastFurnace ? "brick_blast_furnace" : "brick_furnace", stack);
				}
				super.onTake(player, stack);
			}
		});
	}

	public int progressPixels(int width) {
		int progress = data.get(1);
		int max = data.get(2);
		return max <= 0 ? 0 : Math.min(width, progress * width / max);
	}

	public int burnPixels(int height) {
		int burn = data.get(0);
		return Math.min(height, Math.max(0, burn * height / BrickFurnaceBlockEntity.BURN_TICKS));
	}

	public int remainingFuelSeconds() {
		return Math.max(0, (data.get(0) + 19) / 20);
	}

	public boolean isBlastFurnace() {
		return blastFurnace;
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
		int machineSlots = BrickFurnaceBlockEntity.SLOT_COUNT;
		if (index < machineSlots) {
			if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
		} else if (stack.is(Items.CHARCOAL)) {
			if (!moveItemStackTo(stack, BrickFurnaceBlockEntity.FUEL_SLOT, BrickFurnaceBlockEntity.FUEL_SLOT + 1, false)) return ItemStack.EMPTY;
		} else if (PrimitiveProcessingRegistry.isProcessable(PrimitiveProcessor.KILN, stack) || (!blastFurnace && stack.is(Items.LEATHER))) {
			if (!moveItemStackTo(stack, BrickFurnaceBlockEntity.INPUT_SLOT, BrickFurnaceBlockEntity.INPUT_SLOT + 1, false)) return ItemStack.EMPTY;
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
