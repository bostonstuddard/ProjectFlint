package dev.projectflint.block.entity;

import dev.projectflint.block.BasinBlock;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.chat.Component;
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

/** One saved input stack and stomp counter, synchronized to the true-item client renderer. */
public final class BasinBlockEntity extends BlockEntity {
	private ItemStack input = ItemStack.EMPTY;
	private int stomps;

	public BasinBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BASIN, pos, state);
	}

	public static boolean supports(ItemStack stack) {
		return stack.is(Items.STRIPPED_OAK_LOG) || stack.is(Items.STRIPPED_OAK_WOOD)
			|| stack.is(Items.STRIPPED_BIRCH_LOG) || stack.is(Items.STRIPPED_BIRCH_WOOD)
			|| stack.is(net.minecraft.tags.ItemTags.PLANKS)
            || dev.projectflint.system.BasinMaterialProcessingSystem.supports(stack);
	}

	public ItemStack stored() { return input.copy(); }
    public int completedStomps() { return stomps; }
    public int requiredStomps() { return input.isEmpty() ? 0 : requiredStompsFor(input); }
    public static int requiredStompsFor(ItemStack stack) {
        if (stack.is(Items.STRIPPED_OAK_LOG) || stack.is(Items.STRIPPED_OAK_WOOD)
            || stack.is(Items.STRIPPED_BIRCH_LOG) || stack.is(Items.STRIPPED_BIRCH_WOOD)) return 3;
        if (stack.is(net.minecraft.tags.ItemTags.PLANKS)) return 2;
        return Math.min(3, dev.projectflint.system.BasinMaterialProcessingSystem.requiredStomps(stack));
    }

	public boolean insert(ItemStack offered, Player player) {
		if (!supports(offered) || (input.isEmpty() ? false : !ItemStack.isSameItemSameComponents(input, offered))
			|| input.getCount() >= 64) return false;
		if (input.isEmpty()) { input = offered.copyWithCount(1); stomps = 0; }
		else input.grow(1);
		if (!player.isCreative()) offered.shrink(1);
		refreshVisual();
		return true;
	}

	public ItemStack takeOne() {
		if (input.isEmpty()) return ItemStack.EMPTY;
		ItemStack removed = input.split(1);
        stomps = 0;
		if (input.isEmpty()) input = ItemStack.EMPTY;
		refreshVisual();
		return removed;
	}

    /** Progress belongs to one item. Each valid landing costs one food point.
     * A player who cannot pay cannot advance the counter or complete processing.
     */
    public boolean jumpProcess(Level level, Player player) {
        if (input.isEmpty() || !supports(input)) return false;
        int required = requiredStomps();
        if (required <= 0) return false;
        if (!player.isCreative() && player.getFoodData().getFoodLevel() < 1) {
            player.displayClientMessage(Component.literal("You are too hungry to stomp the Basin."), true);
            return false;
        }
        if (!player.isCreative()) player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 1);
        if (stomps + 1 < required) {
            stomps++;
            refreshVisual();
            level.playSound(null, worldPosition, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.55F, 0.8F);
            player.displayClientMessage(Component.literal("Basin: " + stomps + "/" + required + " stomps"), true);
            return true;
        }
        ItemStack processed = input.copyWithCount(1);
        boolean inputWasPlank = processed.is(net.minecraft.tags.ItemTags.PLANKS);
        boolean isWood = inputWasPlank || processed.is(Items.STRIPPED_OAK_LOG) || processed.is(Items.STRIPPED_OAK_WOOD)
            || processed.is(Items.STRIPPED_BIRCH_LOG) || processed.is(Items.STRIPPED_BIRCH_WOOD);
        input.shrink(1);
        if (input.isEmpty()) input = ItemStack.EMPTY;
        stomps = 0;
        refreshVisual();
        if (isWood) {
            Block.popResource(level, worldPosition.above(), inputWasPlank ? new ItemStack(ModItems.WOOD_DUST) : new ItemStack(ModItems.TANNIN, 2));
        } else {
            for (ItemStack drop : dev.projectflint.system.BasinMaterialProcessingSystem.process(processed, level.getRandom()))
                Block.popResource(level, worldPosition.above(), drop);
        }
        level.playSound(null, worldPosition, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.7F, 0.75F);
        return true;
    }

	public void dropContents(Level level) {
		if (!input.isEmpty()) Block.popResource(level, worldPosition, input.copy());
		input = ItemStack.EMPTY;
        stomps = 0;
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
			: input.is(Items.STRIPPED_BIRCH_WOOD) ? BasinBlock.Contents.BIRCH_WOOD : input.is(net.minecraft.tags.ItemTags.PLANKS) ? BasinBlock.Contents.PLANKS : BasinBlock.Contents.MATERIALS;
		if (state.getValue(BasinBlock.CONTENTS) != contents)
            level.setBlock(worldPosition, state.setValue(BasinBlock.CONTENTS, contents), Block.UPDATE_ALL);
        if (!level.isClientSide())
            level.sendBlockUpdated(worldPosition, state, level.getBlockState(worldPosition), Block.UPDATE_CLIENTS);
	}

	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!input.isEmpty()) output.store("BasinInput", ItemStack.CODEC, input);
        output.putInt("Stomps", stomps);
	}

	@Override protected void loadAdditional(ValueInput inputData) {
		super.loadAdditional(inputData);
		input = inputData.read("BasinInput", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		if (!supports(input)) input = ItemStack.EMPTY;
        stomps = input.isEmpty() ? 0 : Math.clamp(inputData.getIntOr("Stomps", 0), 0, Math.max(0, requiredStomps() - 1));
	}
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
