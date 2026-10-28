package dev.projectflint.block.entity;

import dev.projectflint.block.PrimitiveStoveBlock;
import dev.projectflint.config.GameplayTuning;
import dev.projectflint.item.RecipeParchmentItem;
import dev.projectflint.menu.PrimitiveStoveMenu;
import dev.projectflint.recipe.PrimitiveStoveRecipeRegistry;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public final class PrimitiveStoveBlockEntity extends BaseContainerBlockEntity {
    public static final int FUEL_SLOT = 0;
    public static final int PARCHMENT_SLOT = 1;
    public static final int INPUT_START = 2;
    public static final int INPUT_END = 6;
    public static final int OUTPUT_SLOT = 6;
    public static final int SLOT_COUNT = 7;
    public static final int FUEL_LOGS_PER_IGNITION = 4;
    public static final int BURN_TICKS = 20 * 60 * 30;
	/** Half-tick units keep ContainerData below Minecraft's signed short packet limit. */
	public static final int BURN_SYNC_DIVISOR = 2;
    private static final float SPEED_MULTIPLIER = GameplayTuning.PRIMITIVE_STOVE_COOK_SPEED;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int burnTicks;
    private int progress;
    private int progressMax;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) { case 0 -> (burnTicks + BURN_SYNC_DIVISOR - 1) / BURN_SYNC_DIVISOR; case 1 -> progress; case 2 -> progressMax; default -> 0; };
        }
        @Override public void set(int index, int value) {
            switch (index) { case 0 -> burnTicks = Math.max(0, value) * BURN_SYNC_DIVISOR; case 1 -> progress = value; case 2 -> progressMax = value; default -> {} }
        }
        @Override public int getCount() { return 3; }
    };

    public PrimitiveStoveBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.PRIMITIVE_STOVE, pos, state); }

    @Override protected Component getDefaultName() { return Component.translatable("container.projectflint.primitive_stove"); }
    @Override protected AbstractContainerMenu createMenu(int syncId, Inventory inventory) { return new PrimitiveStoveMenu(syncId, inventory, this, data); }
    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == OUTPUT_SLOT) return false;
        if (slot == FUEL_SLOT) return isStrippedLog(stack);
        if (slot == PARCHMENT_SLOT) return stack.is(ModItems.RECIPE_PARCHMENT);
        return slot >= INPUT_START && slot < INPUT_END;
    }

    public boolean ignite() {
        if (burnTicks > 0) return false;
        ItemStack fuel = items.get(FUEL_SLOT);
        if (!isStrippedLog(fuel) || fuel.getCount() < FUEL_LOGS_PER_IGNITION) return false;
        fuel.shrink(FUEL_LOGS_PER_IGNITION);
        if (fuel.isEmpty()) items.set(FUEL_SLOT, ItemStack.EMPTY);
        burnTicks = BURN_TICKS;
        setLit(true);
        setChanged();
        return true;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean changed = false;
        if (burnTicks > 0) {
            burnTicks--;
            changed = true;
            if (burnTicks == 0) {
                progress = 0;
                progressMax = 0;
                setLit(false);
            }
        }

        Optional<PrimitiveStoveRecipeRegistry.Definition> recipe = burnTicks > 0 ? activeRecipe() : Optional.empty();
        if (recipe.isEmpty() || !canAccept(recipe.get().result())) {
            if (progress != 0 || progressMax != 0) { progress = 0; progressMax = 0; changed = true; }
        } else {
            int target = Math.max(1, (int)Math.ceil(recipe.get().processingTicks() / SPEED_MULTIPLIER));
            if (progressMax != target) { progressMax = target; progress = Math.min(progress, target); }
            progress++;
            changed = true;
            if (progress >= progressMax) {
                consumeRequirements(recipe.get());
                insertOutput(recipe.get().result());
                progress = 0;
                progressMax = 0;
            }
        }

        if (changed) setChanged();
    }

    private Optional<PrimitiveStoveRecipeRegistry.Definition> activeRecipe() {
        ItemStack parchment = items.get(PARCHMENT_SLOT);
        Optional<String> recipeId = RecipeParchmentItem.recipeId(parchment);
        if (recipeId.isPresent()) {
            Optional<PrimitiveStoveRecipeRegistry.Definition> bound = PrimitiveStoveRecipeRegistry.findBound(recipeId.get());
            if (bound.isPresent() && matchesRequirements(bound.get())) return bound;
            return Optional.empty();
        }

        int occupied = -1;
        for (int i = INPUT_START; i < INPUT_END; i++) {
            if (!items.get(i).isEmpty()) {
                if (occupied != -1) return Optional.empty();
                occupied = i;
            }
        }
        if (occupied == -1) return Optional.empty();
        return PrimitiveStoveRecipeRegistry.findDirectFood(items.get(occupied));
    }

    private boolean matchesRequirements(PrimitiveStoveRecipeRegistry.Definition recipe) {
        for (PrimitiveStoveRecipeRegistry.Requirement requirement : recipe.requirements()) {
            int found = 0;
            for (int i = INPUT_START; i < INPUT_END; i++) {
                ItemStack stack = items.get(i);
                if (requirement.ingredient().test(stack)) found += stack.getCount();
            }
            if (found < requirement.count()) return false;
        }
        return true;
    }

    private void consumeRequirements(PrimitiveStoveRecipeRegistry.Definition recipe) {
        for (PrimitiveStoveRecipeRegistry.Requirement requirement : recipe.requirements()) {
            int remaining = requirement.count();
            for (int i = INPUT_START; i < INPUT_END && remaining > 0; i++) {
                ItemStack stack = items.get(i);
                if (!requirement.ingredient().test(stack)) continue;
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (stack.isEmpty()) items.set(i, ItemStack.EMPTY);
            }
        }
    }

    private boolean canAccept(ItemStack result) {
        ItemStack output = items.get(OUTPUT_SLOT);
        return output.isEmpty() || (ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize());
    }

    private void insertOutput(ItemStack result) {
        ItemStack output = items.get(OUTPUT_SLOT);
        if (output.isEmpty()) items.set(OUTPUT_SLOT, result.copy());
        else output.grow(result.getCount());
    }

    private void setLit(boolean lit) {
        Level level = getLevel();
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.hasProperty(PrimitiveStoveBlock.LIT) && state.getValue(PrimitiveStoveBlock.LIT) != lit) {
            level.setBlock(getBlockPos(), state.setValue(PrimitiveStoveBlock.LIT, lit), 3);
        }
    }

    /** Accept every stripped log/wood form as one Stove fuel unit. */
    public static boolean isStrippedLog(ItemStack stack) {
        return stack.is(Blocks.STRIPPED_OAK_LOG.asItem()) || stack.is(Blocks.STRIPPED_OAK_WOOD.asItem())
            || stack.is(Blocks.STRIPPED_SPRUCE_LOG.asItem()) || stack.is(Blocks.STRIPPED_SPRUCE_WOOD.asItem())
            || stack.is(Blocks.STRIPPED_BIRCH_LOG.asItem()) || stack.is(Blocks.STRIPPED_BIRCH_WOOD.asItem())
            || stack.is(Blocks.STRIPPED_JUNGLE_LOG.asItem()) || stack.is(Blocks.STRIPPED_JUNGLE_WOOD.asItem())
            || stack.is(Blocks.STRIPPED_ACACIA_LOG.asItem()) || stack.is(Blocks.STRIPPED_ACACIA_WOOD.asItem())
            || stack.is(Blocks.STRIPPED_DARK_OAK_LOG.asItem()) || stack.is(Blocks.STRIPPED_DARK_OAK_WOOD.asItem())
            || stack.is(Blocks.STRIPPED_MANGROVE_LOG.asItem()) || stack.is(Blocks.STRIPPED_MANGROVE_WOOD.asItem())
            || stack.is(Blocks.STRIPPED_CHERRY_LOG.asItem()) || stack.is(Blocks.STRIPPED_CHERRY_WOOD.asItem())
            || stack.is(Blocks.STRIPPED_PALE_OAK_LOG.asItem()) || stack.is(Blocks.STRIPPED_PALE_OAK_WOOD.asItem())
            || stack.is(Blocks.STRIPPED_CRIMSON_STEM.asItem()) || stack.is(Blocks.STRIPPED_CRIMSON_HYPHAE.asItem())
            || stack.is(Blocks.STRIPPED_WARPED_STEM.asItem()) || stack.is(Blocks.STRIPPED_WARPED_HYPHAE.asItem());
    }

    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("BurnTicks", burnTicks);
        output.putInt("Progress", progress);
        output.putInt("ProgressMax", progressMax);
    }

    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        burnTicks = Math.max(0, input.getIntOr("BurnTicks", 0));
        progress = Math.max(0, input.getIntOr("Progress", 0));
        progressMax = Math.max(0, input.getIntOr("ProgressMax", 0));
    }
}
