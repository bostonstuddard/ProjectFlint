package dev.projectflint.block.entity;

import dev.projectflint.block.BrickBlastFurnaceBlock;
import dev.projectflint.block.BrickFurnaceBlock;
import dev.projectflint.config.GameplayTuning;
import dev.projectflint.menu.BrickFurnaceMenu;
import dev.projectflint.recipe.BrickFurnaceRecipeRegistry;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModBlocks;
import net.minecraft.world.item.Items;
import dev.projectflint.system.PrimitiveProcessingRecipe;
import dev.projectflint.system.PrimitiveProcessingRegistry;
import dev.projectflint.system.PrimitiveProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public final class BrickFurnaceBlockEntity extends BaseContainerBlockEntity {
	public static final int FUEL_SLOT = 0;
	public static final int INPUT_SLOT = 1;
	public static final int OUTPUT_START = 2;
	public static final int OUTPUT_END = 6;
	public static final int SLOT_COUNT = 6;
	public static final int FUEL_CHARCOAL_PER_IGNITION = 1;
	public static final int BURN_TICKS = 10 * 60 * 20;

	private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	private int burnTicks;
	private int progress;
	private int progressMax;

	private final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> burnTicks;
				case 1 -> progress;
				case 2 -> progressMax;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0 -> burnTicks = value;
				case 1 -> progress = value;
				case 2 -> progressMax = value;
				default -> { }
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
	};

	public BrickFurnaceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BRICK_FURNACE, pos, state);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable(isBlastFurnace()
			? "container.projectflint.brick_blast_furnace"
			: "container.projectflint.brick_furnace");
	}

	@Override
	protected AbstractContainerMenu createMenu(int syncId, Inventory inventory) {
		return new BrickFurnaceMenu(syncId, inventory, this, data, isBlastFurnace());
	}

	@Override
	public int getContainerSize() {
		return SLOT_COUNT;
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
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (slot >= OUTPUT_START) return false;
		if (slot == FUEL_SLOT) return stack.is(Items.CHARCOAL);
		return slot == INPUT_SLOT && (PrimitiveProcessingRegistry.isProcessable(PrimitiveProcessor.KILN, stack) || (!isBlastFurnace() && (stack.is(Items.LEATHER) || stack.is(Items.SAND))));
	}

	public boolean ignite() {
		if (burnTicks > 0) return false;
		ItemStack fuel = items.get(FUEL_SLOT);
		if (!fuel.is(Items.CHARCOAL) || fuel.getCount() < FUEL_CHARCOAL_PER_IGNITION) return false;
		fuel.shrink(FUEL_CHARCOAL_PER_IGNITION);
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

		Optional<PrimitiveProcessingRecipe> recipe = burnTicks > 0
			? findRecipe(items.get(INPUT_SLOT))
			: Optional.empty();

		if (recipe.isEmpty() || !canAccept(recipe.get().resultCopy())) {
			if (progress != 0 || progressMax != 0) {
				progress = 0;
				progressMax = 0;
				changed = true;
			}
		} else {
			int target = Math.max(1, (int)Math.ceil(recipe.get().processingTicks() / speedMultiplier()));
			if (progressMax != target) {
				progressMax = target;
				progress = Math.min(progress, target);
			}
			progress++;
			changed = true;
			if (progress >= progressMax) {
				ItemStack input = items.get(INPUT_SLOT);
				input.shrink(1);
				if (input.isEmpty()) items.set(INPUT_SLOT, ItemStack.EMPTY);
				insertOutput(recipe.get().resultCopy());
				progress = 0;
				progressMax = 0;
			}
		}

		if (changed) setChanged();
	}

	private Optional<PrimitiveProcessingRecipe> findRecipe(ItemStack input) {
		if (!isBlastFurnace() && input.is(Items.LEATHER)) return Optional.of(BrickFurnaceRecipeRegistry.TANNED_LEATHER);
		if (!isBlastFurnace() && input.is(Items.SAND)) return Optional.of(BrickFurnaceRecipeRegistry.GLASS);
		return PrimitiveProcessingRegistry.find(PrimitiveProcessor.KILN, input);
	}

	private float speedMultiplier() {
		return isBlastFurnace()
			? GameplayTuning.BRICK_BLAST_FURNACE_COOK_SPEED
			: GameplayTuning.BRICK_FURNACE_COOK_SPEED;
	}

	private boolean canAccept(ItemStack result) {
		int remaining = result.getCount();
		for (int slot = OUTPUT_START; slot < OUTPUT_END; slot++) {
			ItemStack output = items.get(slot);
			if (!output.isEmpty() && ItemStack.isSameItemSameComponents(output, result)) {
				remaining -= Math.max(0, output.getMaxStackSize() - output.getCount());
				if (remaining <= 0) return true;
			}
		}
		for (int slot = OUTPUT_START; slot < OUTPUT_END; slot++) {
			if (items.get(slot).isEmpty()) {
				remaining -= result.getMaxStackSize();
				if (remaining <= 0) return true;
			}
		}
		return false;
	}

	private void insertOutput(ItemStack result) {
		int remaining = result.getCount();
		for (int slot = OUTPUT_START; slot < OUTPUT_END && remaining > 0; slot++) {
			ItemStack output = items.get(slot);
			if (output.isEmpty() || !ItemStack.isSameItemSameComponents(output, result)) continue;
			int add = Math.min(remaining, output.getMaxStackSize() - output.getCount());
			if (add > 0) {
				output.grow(add);
				remaining -= add;
			}
		}
		for (int slot = OUTPUT_START; slot < OUTPUT_END && remaining > 0; slot++) {
			if (!items.get(slot).isEmpty()) continue;
			ItemStack placed = result.copy();
			int put = Math.min(remaining, placed.getMaxStackSize());
			placed.setCount(put);
			items.set(slot, placed);
			remaining -= put;
		}
	}

	private boolean isBlastFurnace() {
		return getBlockState().is(ModBlocks.BRICK_BLAST_FURNACE);
	}

	private void setLit(boolean lit) {
		Level level = getLevel();
		if (level == null) return;
		BlockState state = getBlockState();
		if (state.is(ModBlocks.BRICK_BLAST_FURNACE)) {
			if (state.getValue(BrickBlastFurnaceBlock.LIT) != lit) {
				level.setBlock(getBlockPos(), state.setValue(BrickBlastFurnaceBlock.LIT, lit), 3);
			}
		} else if (state.is(ModBlocks.BRICK_FURNACE) && state.getValue(BrickFurnaceBlock.LIT) != lit) {
			level.setBlock(getBlockPos(), state.setValue(BrickFurnaceBlock.LIT, lit), 3);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, items);
		output.putInt("BurnTicks", burnTicks);
		output.putInt("Progress", progress);
		output.putInt("ProgressMax", progressMax);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, items);
		burnTicks = Math.max(0, Math.min(BURN_TICKS, input.getIntOr("BurnTicks", 0)));
		progress = Math.max(0, input.getIntOr("Progress", 0));
		progressMax = Math.max(0, input.getIntOr("ProgressMax", 0));
	}
}
