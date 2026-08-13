package dev.projectflint.block.entity;

import dev.projectflint.block.MortarChestBlock;
import dev.projectflint.menu.MortarChestMenu;
import dev.projectflint.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class MortarChestBlockEntity extends RandomizableContainerBlockEntity {
	public static final int SLOT_COUNT = 3;
	private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	private int openers = 0;

	public MortarChestBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.MORTAR_CHEST, pos, state);
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
	@Override protected Component getDefaultName() { return Component.translatable("container.projectflint.mortar_chest"); }
	@Override protected AbstractContainerMenu createMenu(int syncId, Inventory inventory) { return new MortarChestMenu(syncId, inventory, this); }

	@Override
	public void startOpen(ContainerUser user) {
		if (remove || user.getLivingEntity().isSpectator()) return;
		openers++;
		if (openers == 1) setChestOpen(true);
	}

	@Override
	public void stopOpen(ContainerUser user) {
		if (remove || user.getLivingEntity().isSpectator()) return;
		openers = Math.max(0, openers - 1);
		if (openers == 0) setChestOpen(false);
	}

	private void setChestOpen(boolean open) {
		Level level = getLevel();
		if (level == null) return;
		BlockState state = getBlockState();
		if (!state.hasProperty(MortarChestBlock.OPEN) || state.getValue(MortarChestBlock.OPEN) == open) return;
		level.setBlock(getBlockPos(), state.setValue(MortarChestBlock.OPEN, open), 3);
		if (!level.isClientSide()) {
			level.playSound(null, getBlockPos(), open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, 0.9F + level.random.nextFloat() * 0.1F);
		}
	}
}
