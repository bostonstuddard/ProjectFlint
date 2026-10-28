package dev.projectflint.menu;

import dev.projectflint.block.entity.WoodenCabinetBlockEntity;
import dev.projectflint.registry.ModItems;
import dev.projectflint.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class WoodenCabinetMenu extends AbstractContainerMenu {
    public static final int STORAGE_SLOTS = WoodenCabinetBlockEntity.SLOT_COUNT;
    private final Container container;

    public WoodenCabinetMenu(int syncId, Inventory inventory) {
        this(syncId, inventory, new SimpleContainer(STORAGE_SLOTS));
    }

    public WoodenCabinetMenu(int syncId, Inventory inventory, Container container) {
        super(ModMenus.WOODEN_CABINET, syncId);
        checkContainerSize(container, STORAGE_SLOTS);
        this.container = container;
        container.startOpen(inventory.player);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(container, row * 9 + col, 7 + col * 18, 18 + row * 18) {
                    @Override public boolean mayPlace(ItemStack stack) { return stack.is(ModItems.RECIPE_PARCHMENT); }
                });
            }
        }
        addStandardInventorySlots(inventory, 7, 91);
    }

    @Override public boolean stillValid(Player player) { return container.stillValid(player); }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < STORAGE_SLOTS) {
            if (!moveItemStackTo(stack, STORAGE_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!stack.is(ModItems.RECIPE_PARCHMENT) || !moveItemStackTo(stack, 0, STORAGE_SLOTS, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
