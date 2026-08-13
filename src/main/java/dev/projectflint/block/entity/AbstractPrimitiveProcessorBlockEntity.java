package dev.projectflint.block.entity;

import dev.projectflint.system.PrimitiveProcessingRecipe;
import dev.projectflint.system.PrimitiveProcessingRegistry;
import dev.projectflint.system.PrimitiveProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Shared one-input -> timer -> one-output processor state for primitive heat stations. */
public abstract class AbstractPrimitiveProcessorBlockEntity extends BlockEntity {
	private ItemStack input = ItemStack.EMPTY;
	private ItemStack output = ItemStack.EMPTY;
	private int remainingTicks;
	private int totalTicks;

	protected AbstractPrimitiveProcessorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	protected abstract PrimitiveProcessor processor();
	protected abstract boolean isActive(BlockState state);
	protected abstract void ejectFinishedOutput(Level level, BlockPos pos, BlockState state, ItemStack output);

	protected float processingSpeedMultiplier(BlockState state) {
		return 1.0F;
	}

	public final boolean isBusy() {
		return !input.isEmpty();
	}

	/** Removes and returns the in-flight input when a recoverable workstation is dismantled. */
	public final ItemStack takeStoredInput() {
		ItemStack stored = input.copy();
		input = ItemStack.EMPTY;
		output = ItemStack.EMPTY;
		remainingTicks = 0;
		totalTicks = 0;
		setChanged();
		return stored;
	}

	public final boolean tryInsert(ItemStack stack) {
		if (stack.isEmpty() || isBusy()) return false;
		PrimitiveProcessingRecipe recipe = PrimitiveProcessingRegistry.find(processor(), stack).orElse(null);
		if (recipe == null) return false;
		input = stack.copyWithCount(1);
		output = recipe.resultCopy();
		int recipeTicks = recipe.processingTicks();
		float speed = Math.max(0.01F, processingSpeedMultiplier(getBlockState()));
		totalTicks = recipeTicks <= 0 ? 0 : Math.max(1, Math.round(recipeTicks / speed));
		remainingTicks = totalTicks;
		setChanged();
		return true;
	}

	public final void serverTick(Level level, BlockPos pos, BlockState state) {
		if (input.isEmpty() || !isActive(state)) return;
		if (remainingTicks > 0) {
			remainingTicks--;
			setChanged();
		}
		if (remainingTicks > 0) return;

		ItemStack finished = output.copy();
		input = ItemStack.EMPTY;
		output = ItemStack.EMPTY;
		totalTicks = 0;
		remainingTicks = 0;
		ejectFinishedOutput(level, pos, state, finished);
		setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput outputView) {
		super.saveAdditional(outputView);
		if (!input.isEmpty()) outputView.store("Input", ItemStack.CODEC, input);
		if (!output.isEmpty()) outputView.store("Output", ItemStack.CODEC, output);
		outputView.putInt("RemainingTicks", remainingTicks);
		outputView.putInt("TotalTicks", totalTicks);
	}

	@Override
	protected void loadAdditional(ValueInput inputView) {
		super.loadAdditional(inputView);
		input = inputView.read("Input", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		output = inputView.read("Output", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		remainingTicks = inputView.getIntOr("RemainingTicks", 0);
		totalTicks = inputView.getIntOr("TotalTicks", 0);
	}
}
