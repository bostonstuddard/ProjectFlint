package dev.projectflint.menu;

import dev.projectflint.registry.ModBlocks;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

/** Vanilla 3x3 crafting menu; output is gated on the SERVER, including shift-click. */
public final class HardwoodCraftingMenu extends CraftingMenu {
    private final ContainerLevelAccess table;
    private final Player crafter;
    private long readyAt;
    public HardwoodCraftingMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(id, inventory, access);
        this.table = access;
        this.crafter = inventory.player;
        this.readyAt = inventory.player.level().getGameTime() + 60L;
        // Replace only the result slot; all vanilla 3x3 recipe logic remains authoritative.
        slots.set(0, new ResultSlot(inventory.player, craftSlots, resultSlots, 0, 124, 35) {
            @Override public boolean mayPickup(Player player) {
                return super.mayPickup(player) && (player.isCreative() || player.level().getGameTime() >= readyAt);
            }
        });
    }
    @Override public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (crafter != null) readyAt = crafter.level().getGameTime() + 60L;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index == 0 && !getResultSlot().mayPickup(player)) return ItemStack.EMPTY;
        return super.quickMoveStack(player, index);
    }
    @Override public boolean stillValid(Player player) {
        return stillValid(table, player, ModBlocks.HARDWOOD_CRAFTING_TABLE);
    }
}
