package dev.projectflint.menu;

import dev.projectflint.block.entity.SandingStationBlockEntity;
import dev.projectflint.compat.flintquests.FlintQuestsBridge;
import net.minecraft.server.level.ServerPlayer;
import dev.projectflint.recipe.SandingStationRecipeRegistry;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SandingStationMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT = 0;
    public static final int RESULT_SLOT = 1;
    private static final int PLAYER_SLOT_START = 2;
    private static final int PLAYER_SLOT_END = PLAYER_SLOT_START + 36;

    private final ContainerLevelAccess access;
    private final ResultContainer result = new ResultContainer();
    private final Container input = new SimpleContainer(1) {
        @Override public void setChanged() {
            super.setChanged();
            if (!suppressRefresh) SandingStationMenu.this.slotsChanged(this);
        }
    };
    private final DataSlot durability;
    private SandingStationRecipeRegistry.Definition activeRecipe;
    private boolean suppressRefresh;

    public SandingStationMenu(int syncId, Inventory inventory) {
        this(syncId, inventory, ContainerLevelAccess.NULL, null);
    }

    public SandingStationMenu(int syncId, Inventory inventory, ContainerLevelAccess access, SandingStationBlockEntity station) {
        super(ModMenus.SANDING_STATION, syncId);
        this.access = access;
        this.durability = station == null ? DataSlot.standalone() : new DataSlot() {
            @Override public int get() { return station.durability(); }
            @Override public void set(int value) { /* server-owned */ }
        };
        addDataSlot(durability);

        addSlot(new Slot(input, 0, 44, 35));
        addSlot(new Slot(result, 0, 116, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return activeRecipe != null && durability.get() > 0; }
            @Override public void onTake(Player player, ItemStack stack) {
                SandingStationRecipeRegistry.Definition recipe = activeRecipe;
                if (recipe == null) return;
                boolean paid = station != null && station.consumeDurability();
                if (!paid) {
                    setupResult();
                    return;
                }
                suppressRefresh = true;
                try {
                    input.removeItem(0, recipe.count());
                } finally {
                    suppressRefresh = false;
                }
                if (player instanceof ServerPlayer serverPlayer) FlintQuestsBridge.onCraft(serverPlayer, "sanding_station", stack);
                setupResult();
                super.onTake(player, stack);
            }
        });
        addStandardInventorySlots(inventory, 8, 100);
        setupResult();
    }

    public int getDurability() { return durability.get(); }

    @Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.SANDING_STATION); }

    @Override public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == input && !suppressRefresh) setupResult();
    }

    private void setupResult() {
        activeRecipe = SandingStationRecipeRegistry.find(input.getItem(0)).orElse(null);
        if (activeRecipe == null || durability.get() <= 0) result.setItem(0, ItemStack.EMPTY);
        else result.setItem(0, activeRecipe.result().copy());
        broadcastChanges();
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == RESULT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, copy);
        } else if (index == INPUT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, input));
    }
}
