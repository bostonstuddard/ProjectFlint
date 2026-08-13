package dev.projectflint.block.entity;

import dev.projectflint.block.AdvancedKilnControllerBlock;
import dev.projectflint.block.AdvancedKilnMultiblock;
import dev.projectflint.menu.AdvancedKilnMenu;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.ModAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Five-slot GUI-backed processor for the Advanced Kiln multiblock. */
public final class AdvancedKilnBlockEntity extends BaseContainerBlockEntity {
	public static final int BRANCH_SLOT = 0;
	/** Compatibility alias retained for older menu code and save assumptions. */
	public static final int BURNT_BRANCH_SLOT = BRANCH_SLOT;
	public static final int MOLD_SLOT = 1;
	public static final int FLINT_SLOT = 2;
	public static final int FIRED_CLAY_SLOT = 3;
	public static final int OUTPUT_SLOT = 4;
	public static final int SLOT_COUNT = 5;
	public static final int DEFAULT_PROCESS_TICKS = 20 * 60;
	public static final int BURNT_BRANCH_BLOCK_PROCESS_TICKS = 20 * 15;

	private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	private int progress;
	private int processTotal = DEFAULT_PROCESS_TICKS;

	private final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> progress;
				case 1 -> processTotal;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			if (index == 0) progress = value;
			if (index == 1) processTotal = value;
		}

		@Override
		public int getCount() {
			return 2;
		}
	};

	public AdvancedKilnBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ADVANCED_KILN, pos, state);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.projectflint.advanced_kiln");
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
	public int getContainerSize() {
		return SLOT_COUNT;
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (progress > 0 && slot != OUTPUT_SLOT) return false;
		return switch (slot) {
			case BRANCH_SLOT -> stack.is(ModItems.BURNT_BRANCH) || stack.is(ModItems.BRANCH);
			case MOLD_SLOT -> isFiredMold(stack);
			case FLINT_SLOT -> stack.is(Items.FLINT);
			case FIRED_CLAY_SLOT -> stack.is(ModItems.FIRED_CLAY);
			default -> false;
		};
	}

	@Override
	protected AbstractContainerMenu createMenu(int syncId, Inventory inventory) {
		return new AdvancedKilnMenu(syncId, inventory, this, data);
	}

	public void serverTick(Level level, BlockPos pos, BlockState state) {
		boolean changed = false;

		if (progress > 0) {
			progress--;
			changed = true;
			if (progress == 0) finishProcess(level, pos);
		} else {
			ProcessDefinition process = currentProcess();
			if (process != null && canStartProcess(process)) {
				consumeForProcess(process);
				processTotal = process.durationTicks();
				progress = processTotal;
				changed = true;
			}
		}

		boolean active = progress > 0;
		if (state.hasProperty(AdvancedKilnControllerBlock.ACTIVE)
			&& state.getValue(AdvancedKilnControllerBlock.ACTIVE) != active) {
			BlockState updated = state.setValue(AdvancedKilnControllerBlock.ACTIVE, active);
			level.setBlockAndUpdate(pos, updated);
			AdvancedKilnMultiblock.setActive(level, pos, updated, active);
			changed = true;
		}

		if (changed) setChanged();
	}

	private ProcessDefinition currentProcess() {
		ItemStack mold = items.get(MOLD_SLOT);
		if (mold.is(ModItems.FIRED_CLAY_BLOCK_MOLD)) {
			return new ProcessDefinition(new ItemStack(ModItems.BURNT_BRANCH_BLOCK), 4, 4, 0, BURNT_BRANCH_BLOCK_PROCESS_TICKS, false, false);
		}
		ItemStack result = outputFor(mold);
		if (result.isEmpty()) return null;
		return new ProcessDefinition(result, 8, 8, 4, DEFAULT_PROCESS_TICKS, true, mold.is(ModItems.FIRED_CLAY_POT_MOLD));
	}

	private boolean canStartProcess(ProcessDefinition process) {
		ItemStack branchInput = items.get(BRANCH_SLOT);
		if (process.usesBurntBranches()) {
			if (!branchInput.is(ModItems.BURNT_BRANCH) || branchInput.getCount() < process.branchCount()) return false;
		} else if (!branchInput.is(ModItems.BRANCH) || branchInput.getCount() < process.branchCount()) {
			return false;
		}
		if (items.get(FLINT_SLOT).getCount() < process.flintCount()) return false;
		if (items.get(FIRED_CLAY_SLOT).getCount() < process.firedClayCount()) return false;
		if (!isFiredMold(items.get(MOLD_SLOT))) return false;

		ItemStack output = items.get(OUTPUT_SLOT);
		if (output.isEmpty()) return true;
		return ItemStack.isSameItemSameComponents(output, process.result())
			&& output.getCount() + process.result().getCount() <= output.getMaxStackSize();
	}

	private void consumeForProcess(ProcessDefinition process) {
		items.get(BRANCH_SLOT).shrink(process.branchCount());
		items.get(FLINT_SLOT).shrink(process.flintCount());
		if (process.firedClayCount() > 0) items.get(FIRED_CLAY_SLOT).shrink(process.firedClayCount());
	}

	private void finishProcess(Level level, BlockPos pos) {
		ProcessDefinition process = currentProcess();
		if (process == null) return;
		ItemStack output = items.get(OUTPUT_SLOT);
		if (output.isEmpty()) items.set(OUTPUT_SLOT, process.result().copy());
		else if (ItemStack.isSameItemSameComponents(output, process.result())) output.grow(process.result().getCount());

		if (process.consumeMold()) items.get(MOLD_SLOT).shrink(1);

		if (level instanceof ServerLevel serverLevel) {
			for (ServerPlayer player : serverLevel.players()) {
				if (player.blockPosition().closerThan(pos, 8.0D)) ModAdvancements.awardAll(player, "advanced_kiln_process");
			}
		}
	}

	public static boolean isFiredMold(ItemStack stack) {
		return stack.is(ModItems.FIRED_CLAY_AXE_MOLD)
			|| stack.is(ModItems.FIRED_CLAY_BRICK_MOLD)
			|| stack.is(ModItems.FIRED_CLAY_CUTTERS_MOLD)
			|| stack.is(ModItems.FIRED_CLAY_MULTITOOL_MOLD)
			|| stack.is(ModItems.FIRED_CLAY_POT_MOLD)
			|| stack.is(ModItems.FIRED_CLAY_BLOCK_MOLD);
	}

	public static ItemStack outputFor(ItemStack mold) {
		if (mold.is(ModItems.FIRED_CLAY_AXE_MOLD)) return new ItemStack(ModItems.FLINT_AXE_PIECES);
		if (mold.is(ModItems.FIRED_CLAY_BRICK_MOLD)) return new ItemStack(ModItems.MORTAR_BRICKS, 6);
		if (mold.is(ModItems.FIRED_CLAY_CUTTERS_MOLD)) return new ItemStack(ModItems.FLINT_CUTTER_PIECE);
		if (mold.is(ModItems.FIRED_CLAY_MULTITOOL_MOLD)) return new ItemStack(ModItems.REFINED_FLINT_MULTITOOL_PIECE);
		if (mold.is(ModItems.FIRED_CLAY_POT_MOLD)) return new ItemStack(ModItems.CLAY_POT);
		if (mold.is(ModItems.FIRED_CLAY_BLOCK_MOLD)) return new ItemStack(ModItems.BURNT_BRANCH_BLOCK);
		return ItemStack.EMPTY;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, items);
		output.putInt("Progress", progress);
		output.putInt("ProcessTotal", processTotal);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, items);
		progress = input.getIntOr("Progress", 0);
		processTotal = input.getIntOr("ProcessTotal", DEFAULT_PROCESS_TICKS);
	}

	private record ProcessDefinition(ItemStack result, int branchCount, int flintCount, int firedClayCount,
		int durationTicks, boolean usesBurntBranches, boolean consumeMold) {}
}
