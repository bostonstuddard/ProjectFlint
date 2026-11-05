package dev.projectflint.block.entity;

import dev.projectflint.block.BasinBlock;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Four individually visible inputs, FIFO stomp processing and persistent per-input progress. */
public final class BasinBlockEntity extends BlockEntity implements Container {
    public static final int CAPACITY = 4;
    private final NonNullList<ItemStack> slots = NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
    // Older Basin versions could hold a stack of 64. Preserve that legacy inventory
    // and feed it back into the four visible slots as space becomes available.
    private ItemStack legacyOverflow = ItemStack.EMPTY;
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

    public ItemStack stored() { return slots.get(0).copy(); }
    public java.util.List<ItemStack> visibleContents() {
        return java.util.List.of(slots.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toArray(ItemStack[]::new));
    }
    public int storedCount() {
        int n = 0;
        for (ItemStack stack : slots) if (!stack.isEmpty()) n++;
        return n;
    }
    public int completedStomps() { return stomps; }
    public int requiredStomps() { return stored().isEmpty() ? 0 : requiredStompsFor(stored()); }
    public static int requiredStompsFor(ItemStack stack) {
        if (stack.is(Items.STRIPPED_OAK_LOG) || stack.is(Items.STRIPPED_OAK_WOOD)
            || stack.is(Items.STRIPPED_BIRCH_LOG) || stack.is(Items.STRIPPED_BIRCH_WOOD)) return 3;
        if (stack.is(net.minecraft.tags.ItemTags.PLANKS)) return 2;
        return Math.min(3, dev.projectflint.system.BasinMaterialProcessingSystem.requiredStomps(stack));
    }
    private void compact() {
        int dest = 0;
        for (int i = 0; i < CAPACITY; i++) {
            if (!slots.get(i).isEmpty()) {
                if (dest != i) { slots.set(dest, slots.get(i)); slots.set(i, ItemStack.EMPTY); }
                dest++;
            }
        }
        while (dest < CAPACITY && !legacyOverflow.isEmpty()) {
            slots.set(dest++, legacyOverflow.split(1));
        }
        if (legacyOverflow.isEmpty()) legacyOverflow = ItemStack.EMPTY;
    }
    public boolean insert(ItemStack offered, Player player) {
        if (offered.isEmpty() || !supports(offered) || storedCount() >= CAPACITY || !legacyOverflow.isEmpty()) return false;
        for (int i = 0; i < CAPACITY; i++) {
            if (slots.get(i).isEmpty()) {
                slots.set(i, offered.copyWithCount(1));
                if (!player.isCreative()) offered.shrink(1);
                refreshVisual();
                return true;
            }
        }
        return false;
    }
    /** Empty-hand retrieval removes the latest unprocessed item first. */
    public ItemStack takeOne() {
        for (int i = CAPACITY - 1; i >= 0; i--) {
            if (!slots.get(i).isEmpty()) {
                ItemStack removed = slots.get(i);
                slots.set(i, ItemStack.EMPTY);
                if (i == 0) stomps = 0;
                compact();
                refreshVisual();
                return removed;
            }
        }
        return ItemStack.EMPTY;
    }

    // Mojang Container interface also lets Jade's normal inventory providers
    // observe the Basin without adding a required client/server Jade dependency.
    @Override public int getContainerSize() { return CAPACITY; }
    @Override public int getMaxStackSize() { return 1; }
    @Override public boolean isEmpty() { return storedCount() == 0; }
    @Override public ItemStack getItem(int slot) { return slot >= 0 && slot < CAPACITY ? slots.get(slot) : ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= CAPACITY || amount < 1) return ItemStack.EMPTY;
        ItemStack taken = slots.get(slot).split(amount);
        if (slots.get(slot).isEmpty()) slots.set(slot, ItemStack.EMPTY);
        if (slot == 0) stomps = 0;
        compact(); refreshVisual(); return taken;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= CAPACITY) return ItemStack.EMPTY;
        ItemStack taken = slots.get(slot); slots.set(slot, ItemStack.EMPTY);
        if (slot == 0) stomps = 0;
        compact(); refreshVisual(); return taken;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= CAPACITY) return;
        slots.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        if (slot == 0) stomps = 0;
        compact(); refreshVisual();
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64.0;
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return false; } // player placement uses BasinBlock.insert
    @Override public void clearContent() {
        for (int i = 0; i < CAPACITY; i++) slots.set(i, ItemStack.EMPTY);
        legacyOverflow = ItemStack.EMPTY; stomps = 0; refreshVisual();
    }

    /** Progress belongs to one item. Each valid landing costs one food point.
     * A player who cannot pay cannot advance the counter or complete processing.
     */
    public boolean jumpProcess(Level level, Player player) {
        if (stored().isEmpty() || !supports(stored())) return false;
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
            level.playSound(null, worldPosition, SoundEvents.GRAVEL_HIT, SoundSource.BLOCKS, 0.5F, 0.82F + 0.1F * stomps);
            if (level instanceof ServerLevel server) server.sendParticles(ParticleTypes.POOF,
                worldPosition.getX() + .5, worldPosition.getY() + .30, worldPosition.getZ() + .5,
                2 + 3 * stomps, .20, .02, .20, .015);
            player.displayClientMessage(Component.literal("Basin: " + stomps + "/" + required + " stomps"), true);
            return true;
        }
        ItemStack processed = stored();
        boolean inputWasPlank = processed.is(net.minecraft.tags.ItemTags.PLANKS);
        boolean isWood = inputWasPlank || processed.is(Items.STRIPPED_OAK_LOG) || processed.is(Items.STRIPPED_OAK_WOOD)
            || processed.is(Items.STRIPPED_BIRCH_LOG) || processed.is(Items.STRIPPED_BIRCH_WOOD);
        slots.set(0, ItemStack.EMPTY);
        compact();
        stomps = 0;
        refreshVisual();
        if (isWood) {
            Block.popResource(level, worldPosition.above(), inputWasPlank ? new ItemStack(ModItems.WOOD_DUST) : new ItemStack(ModItems.TANNIN, 2));
        } else {
            for (ItemStack drop : dev.projectflint.system.BasinMaterialProcessingSystem.process(processed, level.getRandom()))
                Block.popResource(level, worldPosition.above(), drop);
        }
        level.playSound(null, worldPosition, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.9F, 0.78F);
        if (level instanceof ServerLevel server) server.sendParticles(ParticleTypes.POOF,
            worldPosition.getX() + .5, worldPosition.getY() + .35, worldPosition.getZ() + .5,
            14, .28, .07, .28, .035);
        return true;
    }

    public void dropContents(Level level) {
        for (ItemStack stack : slots) if (!stack.isEmpty()) Block.popResource(level, worldPosition, stack.copy());
        if (!legacyOverflow.isEmpty()) Block.popResource(level, worldPosition, legacyOverflow.copy());
        clearContent();
    }

	private void refreshVisual() {
		setChanged();
		if (level == null) return;
		BlockState state = level.getBlockState(worldPosition);
		if (!(state.getBlock() instanceof BasinBlock)) return;
		BasinBlock.Contents contents = stored().isEmpty() ? BasinBlock.Contents.EMPTY
			: stored().is(Items.STRIPPED_OAK_LOG) ? BasinBlock.Contents.OAK_LOG
			: stored().is(Items.STRIPPED_OAK_WOOD) ? BasinBlock.Contents.OAK_WOOD
			: stored().is(Items.STRIPPED_BIRCH_LOG) ? BasinBlock.Contents.BIRCH_LOG
			: stored().is(Items.STRIPPED_BIRCH_WOOD) ? BasinBlock.Contents.BIRCH_WOOD : stored().is(net.minecraft.tags.ItemTags.PLANKS) ? BasinBlock.Contents.PLANKS : BasinBlock.Contents.MATERIALS;
		if (state.getValue(BasinBlock.CONTENTS) != contents)
            level.setBlock(worldPosition, state.setValue(BasinBlock.CONTENTS, contents), Block.UPDATE_ALL);
        if (!level.isClientSide())
            level.sendBlockUpdated(worldPosition, state, level.getBlockState(worldPosition), Block.UPDATE_CLIENTS);
	}

    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        // A full list is required: distinct materials must survive save/load independently.
        ContainerHelper.saveAllItems(output, slots);
        if (!legacyOverflow.isEmpty()) output.store("LegacyBasinOverflow", ItemStack.CODEC, legacyOverflow);
        output.putInt("Stomps", stomps);
    }
    @Override protected void loadAdditional(ValueInput inputData) {
        super.loadAdditional(inputData);
        for (int i = 0; i < CAPACITY; i++) slots.set(i, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(inputData, slots);
        legacyOverflow = inputData.read("LegacyBasinOverflow", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (storedCount() == 0) {
            ItemStack old = inputData.read("BasinInput", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            for (int i = 0; i < CAPACITY && !old.isEmpty(); i++) slots.set(i, old.split(1));
            if (!old.isEmpty()) legacyOverflow = old;
        }
        compact();
        stomps = isEmpty() ? 0 : Math.clamp(inputData.getIntOr("Stomps", 0), 0, Math.max(0, requiredStomps() - 1));
    }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
