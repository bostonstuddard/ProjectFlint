package dev.projectflint.block.entity;

import dev.projectflint.menu.NestMenu;
import dev.projectflint.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

public final class NestBlockEntity extends RandomizableContainerBlockEntity {
	public static final int MAX_EGGS = 4;

	private NonNullList<ItemStack> items = NonNullList.withSize(MAX_EGGS, ItemStack.EMPTY);
	private UUID owner;

	public NestBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.NEST, pos, state);
	}

	public int eggCount() {
		int count = 0;
		for (ItemStack stack : items) {
			if (stack.is(Items.EGG)) count += stack.getCount();
		}
		return count;
	}

	public boolean hasOwner() {
		return owner != null;
	}

	public boolean isOwnedBy(UUID chickenId) {
		return owner != null && owner.equals(chickenId);
	}

	public boolean canAcceptEgg() {
		return eggCount() < MAX_EGGS;
	}

	public boolean addEgg(UUID chickenId) {
		if (!canAcceptEgg()) return false;
		if (owner != null && !owner.equals(chickenId)) return false;
		if (owner == null) owner = chickenId;

		for (int slot = 0; slot < items.size(); slot++) {
			if (!items.get(slot).isEmpty()) continue;
			items.set(slot, new ItemStack(Items.EGG));
			setChanged();
			return true;
		}
		return false;
	}

	public void removeEgg() {
		for (int slot = items.size() - 1; slot >= 0; slot--) {
			if (!items.get(slot).is(Items.EGG)) continue;
			items.set(slot, ItemStack.EMPTY);
			setChanged();
			return;
		}
	}

	public void clearEggs() {
		boolean changed = false;
		for (int slot = 0; slot < items.size(); slot++) {
			if (items.get(slot).isEmpty()) continue;
			items.set(slot, ItemStack.EMPTY);
			changed = true;
		}
		if (changed) setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!trySaveLootTable(output)) ContainerHelper.saveAllItems(output, items);
		if (owner != null) output.putString("Owner", owner.toString());
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
		if (!tryLoadLootTable(input)) ContainerHelper.loadAllItems(input, items);
		for (int slot = 0; slot < items.size(); slot++) {
			ItemStack stack = items.get(slot);
			if (!stack.is(Items.EGG)) {
				items.set(slot, ItemStack.EMPTY);
			} else if (stack.getCount() > 1) {
				stack.setCount(1);
			}
		}
		owner = input.getString("Owner").flatMap(value -> {
			try {
				return java.util.Optional.of(UUID.fromString(value));
			} catch (IllegalArgumentException ignored) {
				return java.util.Optional.empty();
			}
		}).orElse(null);
	}

	@Override
	public int getContainerSize() {
		return MAX_EGGS;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return stack.is(Items.EGG);
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.projectflint.nest");
	}

	@Override
	protected AbstractContainerMenu createMenu(int syncId, Inventory inventory) {
		return new NestMenu(syncId, inventory, this);
	}
}
