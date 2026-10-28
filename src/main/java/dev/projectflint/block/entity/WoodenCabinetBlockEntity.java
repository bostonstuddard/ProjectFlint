package dev.projectflint.block.entity;

import dev.projectflint.block.WoodenCabinetBlock;
import dev.projectflint.menu.WoodenCabinetMenu;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class WoodenCabinetBlockEntity extends RandomizableContainerBlockEntity {
    public static final int SLOT_COUNT = 27;
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int openers;

    public WoodenCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WOODEN_CABINET, pos, state);
    }

    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!trySaveLootTable(output)) ContainerHelper.saveAllItems(output, items);
    }

    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(input)) ContainerHelper.loadAllItems(input, items);
    }

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override protected Component getDefaultName() { return Component.translatable("container.projectflint.wooden_cabinet"); }
    @Override protected AbstractContainerMenu createMenu(int syncId, Inventory inventory) { return new WoodenCabinetMenu(syncId, inventory, this); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return stack.is(ModItems.RECIPE_PARCHMENT); }

    @Override public void startOpen(ContainerUser user) {
        if (remove || user.getLivingEntity().isSpectator()) return;
        openers++;
        if (openers == 1) setOpen(true);
    }

    @Override public void stopOpen(ContainerUser user) {
        if (remove || user.getLivingEntity().isSpectator()) return;
        openers = Math.max(0, openers - 1);
        if (openers == 0) setOpen(false);
    }

    private void setOpen(boolean open) {
        Level level = getLevel();
        if (level == null) return;
        BlockState state = getBlockState();
        if (!state.hasProperty(WoodenCabinetBlock.OPEN) || state.getValue(WoodenCabinetBlock.OPEN) == open) return;
        level.setBlock(getBlockPos(), state.setValue(WoodenCabinetBlock.OPEN, open), 3);
        if (!level.isClientSide()) {
            level.playSound(null, getBlockPos(), open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.45F, 0.92F);
        }
    }
}
