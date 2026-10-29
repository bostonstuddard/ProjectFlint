package dev.projectflint.menu;

import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModMenus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Hardwood worktable: uses the ordinary vanilla 3x3 recipe grid and its ingredient/remainder
 * semantics, but never exposes the result until 60 server ticks have elapsed with an unchanged
 * input grid. One completed craft is released per timer, including shift-click crafting.
 *
 * This table has its own menu type/screen so the client receives the same output and progress
 * state as the server. A vanilla CraftingMenu client used to show instant results while a
 * server-only mayPickup check refused them, creating ghost output and inconsistent clicking.
 */
public final class HardwoodCraftingMenu extends CraftingMenu {
    public static final int CRAFT_TICKS = 60;

    private final ContainerLevelAccess table;
    private final Player crafter;
    private final DataSlot progress = DataSlot.standalone();
    private List<ItemStack> lastInputs = List.of();
    private ItemStack pendingResult = ItemStack.EMPTY;
    private long readyAtTick = Long.MAX_VALUE;

    public HardwoodCraftingMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public HardwoodCraftingMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(id, inventory, access);
        this.table = access;
        this.crafter = inventory.player;
        addDataSlot(progress);

        // ResultSlot is left in charge of recipe achievements, container remainders and ingredient
        // consumption. It may only be taken once the server publishes the timed result.
        slots.set(0, new ResultSlot(inventory.player, craftSlots, resultSlots, 0, 124, 35) {
            @Override
            public boolean mayPickup(Player player) {
                return HardwoodCraftingMenu.this.isCraftReady() && super.mayPickup(player);
            }

            @Override
            public void onTake(Player player, ItemStack crafted) {
                super.onTake(player, crafted);
                // Re-arm after *every* output, including recipe types whose grid remains intact.
                if (!player.level().isClientSide()) {
                    HardwoodCraftingMenu.this.refreshRecipe(true);
                }
            }
        });
    }

    @Override
    public MenuType<?> getType() {
        // CraftingMenu's base constructor uses the vanilla type; the dedicated type is required
        // for a matching client menu and a visible, synchronized countdown.
        return ModMenus.HARDWOOD_CRAFTING;
    }

    public int getProgressTicks() {
        return Math.clamp(progress.get(), 0, CRAFT_TICKS);
    }

    public boolean isProcessing() {
        return getProgressTicks() > 0 && getProgressTicks() < CRAFT_TICKS;
    }

    private boolean isCraftReady() {
        if (crafter.level().isClientSide()) {
            return getProgressTicks() == CRAFT_TICKS && !resultSlots.getItem(0).isEmpty();
        }
        return !pendingResult.isEmpty()
            && crafter.level().getGameTime() >= readyAtTick
            && !resultSlots.getItem(0).isEmpty();
    }

    @Override
    public void slotsChanged(Container container) {
        // The default menu instantly sends its newly assembled result to the client. Never
        // invoke super.slotsChanged here; compute and reveal the result only on the server.
        if (crafter != null && !crafter.level().isClientSide()) {
            refreshRecipe(false);
        }
    }

    private void refreshRecipe(boolean forceRestart) {
        if (!(crafter instanceof ServerPlayer serverPlayer)
            || !(crafter.level() instanceof ServerLevel serverLevel)) return;

        List<ItemStack> current = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) current.add(craftSlots.getItem(i).copy());
        if (!forceRestart && sameGrid(lastInputs, current)) return;
        lastInputs = current;
        pendingResult = ItemStack.EMPTY;
        readyAtTick = Long.MAX_VALUE;
        progress.set(0);
        resultSlots.setItem(0, ItemStack.EMPTY);
        resultSlots.setRecipeUsed(null);

        var recipe = serverLevel.getServer().getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, craftSlots.asCraftInput(), serverLevel);
        if (recipe.isPresent()) {
            RecipeHolder<CraftingRecipe> holder = recipe.get();
            if (resultSlots.setRecipeUsed(serverPlayer, holder)) {
                ItemStack assembled = holder.value().assemble(craftSlots.asCraftInput(), serverLevel.registryAccess());
                if (!assembled.isEmpty() && assembled.isItemEnabled(serverLevel.enabledFeatures())) {
                    pendingResult = assembled.copy();
                    readyAtTick = serverLevel.getGameTime() + CRAFT_TICKS;
                }
            }
        }
        // A slot update is sent during the next menu broadcast, without any interim ghost item.
    }

    @Override
    public void finishPlacingRecipe(ServerLevel level, RecipeHolder<CraftingRecipe> recipe) {
        // The vanilla recipe-book path otherwise puts the result directly in the output slot.
        // After the batch placement, restart the server timer and hide it again.
        super.finishPlacingRecipe(level, recipe);
        refreshRecipe(true);
    }

    private static boolean sameGrid(List<ItemStack> previous, List<ItemStack> current) {
        if (previous.size() != current.size()) return false;
        for (int i = 0; i < current.size(); i++) {
            ItemStack a = previous.get(i);
            ItemStack b = current.get(i);
            if (a.getCount() != b.getCount() || !ItemStack.isSameItemSameComponents(a, b)) return false;
        }
        return true;
    }

    @Override
    public void broadcastChanges() {
        if (crafter != null && !crafter.level().isClientSide()) {
            if (pendingResult.isEmpty()) {
                progress.set(0);
            } else {
                long elapsed = Math.max(0L, crafter.level().getGameTime() - (readyAtTick - CRAFT_TICKS));
                int ticks = (int) Math.min(CRAFT_TICKS, elapsed);
                progress.set(ticks);
                if (ticks == CRAFT_TICKS && resultSlots.getItem(0).isEmpty()) {
                    resultSlots.setItem(0, pendingResult.copy());
                }
            }
        }
        super.broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Defend every click mode at the server boundary (pickup, hotbar swap, shift-click etc.).
        if (slotId == 0 && !isCraftReady()) return;
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index == 0 && !isCraftReady()) return ItemStack.EMPTY;
        return super.quickMoveStack(player, index);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(table, player, ModBlocks.HARDWOOD_CRAFTING_TABLE);
    }
}
