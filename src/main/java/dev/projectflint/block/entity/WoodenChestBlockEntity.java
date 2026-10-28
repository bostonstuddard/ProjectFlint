package dev.projectflint.block.entity;

import dev.projectflint.menu.WoodenChestMenu;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class WoodenChestBlockEntity extends ChestBlockEntity {
	public static final int SLOT_COUNT = 9;
	private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

	public WoodenChestBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.WOODEN_CHEST, pos, state); }
	@Override public int getContainerSize() { return SLOT_COUNT; }
	@Override protected NonNullList<ItemStack> getItems() { return items; }
	@Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
	@Override protected Component getDefaultName() { return Component.translatable("container.projectflint.wooden_chest"); }
	@Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); if (!trySaveLootTable(output)) ContainerHelper.saveAllItems(output, items); }
	@Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input); items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY); if (!tryLoadLootTable(input)) ContainerHelper.loadAllItems(input, items); }
	@Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new WoodenChestMenu(ModMenus.WOODEN_CHEST, id, inventory, this, 1); }
}
