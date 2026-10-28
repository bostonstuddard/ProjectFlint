package dev.projectflint.block.entity;

import dev.projectflint.block.BasinBlock;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** One real inventory stack; blockstate mirrors its wood type to render it inside the basin. */
public final class BasinBlockEntity extends BlockEntity {
	private ItemStack input = ItemStack.EMPTY;

	public BasinBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BASIN, pos, state);
	}

	public static boolean supports(ItemStack stack) {
		return stack.is(Items.STRIPPED_OAK_LOG) || stack.is(Items.STRIPPED_OAK_WOOD)
			|| stack.is(Items.STRIPPED_BIRCH_LOG) || stack.is(Items.STRIPPED_BIRCH_WOOD)
			|| stack.is(net.minecraft.tags.ItemTags.PLANKS);
	}

	public ItemStack stored() { return input.copy(); }

	public boolean insert(ItemStack offered, Player player) {
		if (!supports(offered) || (input.isEmpty() ? false : !ItemStack.isSameItemSameComponents(input, offered))
			|| input.getCount() >= 64) return false;
		if (input.isEmpty()) input = offered.copyWithCount(1);
		else input.grow(1);
		if (!player.isCreative()) offered.shrink(1);
		refreshVisual();
		return true;
	}

	public ItemStack takeOne() {
		if (input.isEmpty()) return ItemStack.EMPTY;
		ItemStack removed = input.split(1);
		if (input.isEmpty()) input = ItemStack.EMPTY;
		refreshVisual();
		return removed;
	}

	/** A jump landing on the basin consumes one wood and half a hunger icon. */
	public boolean jumpProcess(Level level, Player player) {
		if (input.isEmpty() || !supports(input)) return false;
		if (!player.isCreative() && player.getFoodData().getFoodLevel() < 1) return false;
		if (!player.isCreative()) player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 1);
		boolean inputWasPlank = input.is(net.minecraft.tags.ItemTags.PLANKS);
		input.shrink(1);
		if (input.isEmpty()) input = ItemStack.EMPTY;
		refreshVisual();
		Block.popResource(level, worldPosition.above(), inputWasPlank ? new ItemStack(ModItems.WOOD_DUST) : new ItemStack(ModItems.TANNIN, 2));
		level.playSound(null, worldPosition, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.5F, 0.75F);
		return true;
	}

	public void dropContents(Level level) {
		if (!input.isEmpty()) Block.popResource(level, worldPosition, input.copy());
		input = ItemStack.EMPTY;
		setChanged();
	}

	private void refreshVisual() {
		setChanged();
		if (level == null) return;
		BlockState state = level.getBlockState(worldPosition);
		if (!(state.getBlock() instanceof BasinBlock)) return;
		BasinBlock.Contents contents = input.isEmpty() ? BasinBlock.Contents.EMPTY
			: input.is(Items.STRIPPED_OAK_LOG) ? BasinBlock.Contents.OAK_LOG
			: input.is(Items.STRIPPED_OAK_WOOD) ? BasinBlock.Contents.OAK_WOOD
			: input.is(Items.STRIPPED_BIRCH_LOG) ? BasinBlock.Contents.BIRCH_LOG
			: input.is(Items.STRIPPED_BIRCH_WOOD) ? BasinBlock.Contents.BIRCH_WOOD : BasinBlock.Contents.PLANKS;
		if (state.getValue(BasinBlock.CONTENTS) != contents)
			level.setBlock(worldPosition, state.setValue(BasinBlock.CONTENTS, contents), 3);
	}

	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!input.isEmpty()) output.store("BasinInput", ItemStack.CODEC, input);
	}

	@Override protected void loadAdditional(ValueInput inputData) {
		super.loadAdditional(inputData);
		input = inputData.read("BasinInput", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		if (!supports(input)) input = ItemStack.EMPTY;
	}
}
