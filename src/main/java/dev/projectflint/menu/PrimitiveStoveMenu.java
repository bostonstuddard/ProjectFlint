package dev.projectflint.menu;

import dev.projectflint.block.entity.PrimitiveStoveBlockEntity;
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

public final class PrimitiveStoveMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public PrimitiveStoveMenu(int syncId, Inventory inventory) {
        this(syncId, inventory, new SimpleContainer(PrimitiveStoveBlockEntity.SLOT_COUNT), new SimpleContainerData(3));
    }

    public PrimitiveStoveMenu(int syncId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.PRIMITIVE_STOVE, syncId);
        checkContainerSize(container, PrimitiveStoveBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;
        container.startOpen(inventory.player);

        addSlot(new Slot(container, PrimitiveStoveBlockEntity.FUEL_SLOT, 14, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return PrimitiveStoveBlockEntity.isStrippedLog(stack); }
        });
        addSlot(new Slot(container, PrimitiveStoveBlockEntity.PARCHMENT_SLOT, 38, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(ModItems.RECIPE_PARCHMENT); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(container, 2, 70, 26));
        addSlot(new Slot(container, 3, 88, 26));
        addSlot(new Slot(container, 4, 70, 44));
        addSlot(new Slot(container, 5, 88, 44));
        addSlot(new Slot(container, PrimitiveStoveBlockEntity.OUTPUT_SLOT, 144, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public void onTake(Player player, ItemStack stack) {
                if (player instanceof ServerPlayer serverPlayer) FlintQuestsBridge.onCraft(serverPlayer, "primitive_oven", stack);
                super.onTake(player, stack);
            }
        });
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    public int progressPixels(int width) {
        int progress = data.get(1), max = data.get(2);
        return max <= 0 ? 0 : Math.min(width, progress * width / max);
    }

    public int burnPixels(int height) {
        int burn = data.get(0);
        return Math.min(height, Math.max(0, burn * height / (PrimitiveStoveBlockEntity.BURN_TICKS / PrimitiveStoveBlockEntity.BURN_SYNC_DIVISOR)));
    }

	public int remainingFuelSeconds() {
		return Math.max(0, (data.get(0) * PrimitiveStoveBlockEntity.BURN_SYNC_DIVISOR + 19) / 20);
	}

    @Override public boolean stillValid(Player player) { return container.stillValid(player); }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int machineSlots = PrimitiveStoveBlockEntity.SLOT_COUNT;
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (PrimitiveStoveBlockEntity.isStrippedLog(stack)) {
            if (!moveItemStackTo(stack, PrimitiveStoveBlockEntity.FUEL_SLOT, PrimitiveStoveBlockEntity.FUEL_SLOT + 1, false)) return ItemStack.EMPTY;
        } else if (stack.is(ModItems.RECIPE_PARCHMENT)) {
            if (!moveItemStackTo(stack, PrimitiveStoveBlockEntity.PARCHMENT_SLOT, PrimitiveStoveBlockEntity.PARCHMENT_SLOT + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, PrimitiveStoveBlockEntity.INPUT_START, PrimitiveStoveBlockEntity.INPUT_END, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
