package dev.projectflint.block.entity;

import dev.projectflint.block.FireclayForgeControllerBlock;
import dev.projectflint.menu.FireclayForgeMenu;
import dev.projectflint.registry.ModItems;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.system.FireclayForgeMultiblock;
import dev.projectflint.system.ToolCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Fuel and recipe inputs are independent. This prototype has no smelting
 * recipe yet: burning four logs/wood as fuel lasts 60 seconds and produces one
 * charcoal byproduct every 15 seconds (four total); charcoal fuel lasts five minutes and
 * has no byproduct. One firestarter use is charged per unit of fuel ignited.
 * All timers/inventory are saved. A full byproduct slot pauses a wood fire
 * before the next charcoal would be lost. A broken structure pauses the fire.
 */
public final class FireclayForgeBlockEntity extends BaseContainerBlockEntity {
	public static final int INPUT_SLOT = 0;
	public static final int FUEL_SLOT = 1;
	public static final int IGNITER_SLOT = 2;
	public static final int OUTPUT_SLOT = 3;
	/** Migration-only, never player-insertable: old prepaid charcoal-batch wood. */
	public static final int LEGACY_FUEL_SLOT = 4;
	/** Independent finished-recipe output. Charcoal fuel byproduct always uses OUTPUT_SLOT. */
	public static final int PRIMARY_OUTPUT_SLOT = 5;
	public static final int SLOT_COUNT = 6;
	public static final int WOOD_FUEL_COST = 4;
	public static final int WOOD_BURN_TICKS = 60 * 20;
	public static final int CHARCOAL_BURN_TICKS = 5 * 60 * 20;
	public static final int WOOD_BYPRODUCT_TICKS = 15 * 20;

	private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	private int burnTicks;
	private int burnTotal;
	private int woodByproductTicks;
	private boolean woodFuel;
	private boolean formed;
	private int structureCheckCooldown;
	private int alloyTicks;
	private int processInputKind; // 0 none, 1 Hardwood, 2 MBSSIG Dust

	private final ContainerData data = new ContainerData() {
		@Override public int get(int index) {
			return switch (index) {
				case 0 -> burnTicks;
				case 1 -> burnTotal;
				case 2 -> formed ? 1 : 0;
				case 3 -> formed && burnTicks > 0 ? 1 : 0;
				case 4 -> woodFuel ? woodByproductTicks : 0;
				default -> 0;
			};
		}
		@Override public void set(int index, int value) {
			switch (index) {
				case 0 -> burnTicks = value;
				case 1 -> burnTotal = value;
				case 2 -> formed = value != 0;
				case 4 -> woodByproductTicks = value;
				default -> { }
			}
		}
		@Override public int getCount() { return 5; }
	};

	public FireclayForgeBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.FIRECLAY_FORGE, pos, state);
	}
	@Override protected Component getDefaultName() { return Component.translatable("container.projectflint.fireclay_forge"); }
	@Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return new FireclayForgeMenu(id, inventory, this, data);
	}
	@Override public int getContainerSize() { return SLOT_COUNT; }
	@Override protected NonNullList<ItemStack> getItems() { return items; }
	@Override protected void setItems(NonNullList<ItemStack> stacks) { items = stacks; }
	@Override public boolean canPlaceItem(int slot, ItemStack stack) {
		return switch (slot) {
			case INPUT_SLOT -> stack.is(ModItems.HARDWOOD) || stack.is(ModItems.MBSSIG_DUST);
			case FUEL_SLOT -> isFuel(stack);
			case IGNITER_SLOT -> isFirestarter(stack);
			default -> false;
		};
	}

	public static boolean isCharcoalWood(ItemStack stack) { return stack.is(ItemTags.LOGS_THAT_BURN); }
	public static boolean isFuel(ItemStack stack) { return stack.is(Items.CHARCOAL) || isCharcoalWood(stack); }
	public static boolean isFirestarter(ItemStack stack) {
		return ToolCapabilities.has(stack, ToolCapabilities.Capability.FIRESTARTER)
			&& stack.isDamageableItem() && stack.getDamageValue() < stack.getMaxDamage();
	}
	private boolean canOutputCharcoal() {
		ItemStack result = items.get(OUTPUT_SLOT);
		return result.isEmpty() || (result.is(Items.CHARCOAL) && result.getCount() < result.getMaxStackSize());
	}
	private void outputCharcoal() {
		ItemStack result = items.get(OUTPUT_SLOT);
		if (result.isEmpty()) items.set(OUTPUT_SLOT, new ItemStack(Items.CHARCOAL));
		else result.grow(1);
	}
	private int processingKind() {
        if (items.get(INPUT_SLOT).is(ModItems.HARDWOOD)) return 1;
        if (items.get(INPUT_SLOT).is(ModItems.MBSSIG_DUST)) return 2;
        return 0;
    }
    private ItemStack processingOutput(int kind) {
        return switch (kind) {
            case 1 -> new ItemStack(ModItems.HARDWOOD_ALLOY);
            case 2 -> new ItemStack(ModItems.PURE_MICA_INSULATED_GLASS);
            default -> ItemStack.EMPTY;
        };
    }
    private boolean canOutputProcessed(int kind) {
        ItemStack wanted = processingOutput(kind);
        ItemStack output = items.get(PRIMARY_OUTPUT_SLOT);
        return !wanted.isEmpty() && (output.isEmpty()
            || (ItemStack.isSameItemSameComponents(output, wanted) && output.getCount() < output.getMaxStackSize()));
    }
	private void replenishLegacyFuel() {
		if (items.get(FUEL_SLOT).isEmpty() && !items.get(LEGACY_FUEL_SLOT).isEmpty()) {
			items.set(FUEL_SLOT, items.get(LEGACY_FUEL_SLOT));
			items.set(LEGACY_FUEL_SLOT, ItemStack.EMPTY);
		}
	}

	/** Manual ignition and guarded automatic relights use the same preflight. */
	public boolean ignite() {
		if (level == null || level.isClientSide()) return false;
		Direction facing = getBlockState().getValue(HorizontalDirectionalBlock.FACING);
		formed = FireclayForgeMultiblock.validate(level, getBlockPos(), facing);
		if (!formed || burnTicks > 0 || !isFirestarter(items.get(IGNITER_SLOT))) return false;
		replenishLegacyFuel();
		ItemStack fuel = items.get(FUEL_SLOT);
		if (!isFuel(fuel)) return false;
		boolean nextWood = isCharcoalWood(fuel);
		if (nextWood && fuel.getCount() < WOOD_FUEL_COST) return false;
		if (nextWood && !canOutputCharcoal()) return false;
        // Never waste charcoal on an empty/blocked recipe, even during automatic relight.
        if (!nextWood && (processingKind() == 0 || !canOutputProcessed(processingKind()))) return false;
		ItemStack starter = items.get(IGNITER_SLOT);
		int nextDamage = starter.getDamageValue() + 1;
		if (nextDamage >= starter.getMaxDamage()) items.set(IGNITER_SLOT, ItemStack.EMPTY);
		else starter.setDamageValue(nextDamage);
		fuel.shrink(nextWood ? WOOD_FUEL_COST : 1);
		woodFuel = nextWood;
		burnTotal = woodFuel ? WOOD_BURN_TICKS : CHARCOAL_BURN_TICKS;
		burnTicks = burnTotal;
		woodByproductTicks = 0;
		alloyTicks = 0; // A full 5-minute charcoal fire is required, not partial fuel reuse.
		processInputKind = processingKind();
		refreshBlockState();
		level.playSound(null, getBlockPos(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.35F, 0.9F);
		setChanged();
		return true;
	}

	public void serverTick(Level level, BlockPos pos, BlockState state) {
		if (structureCheckCooldown-- <= 0 || !formed) {
			structureCheckCooldown = 10;
			boolean next = FireclayForgeMultiblock.validate(level, pos, state.getValue(HorizontalDirectionalBlock.FACING));
			if (next != formed) {
				formed = next;
				refreshBlockState();
				setChanged();
			}
		}
        if (!formed) { alloyTicks = 0; return; }
        if (burnTicks <= 0) {
            alloyTicks = 0;
            // Resume a valid charcoal-fueled operation automatically, but never
            // spend fuel on empty inputs, full outputs, or an absent firestarter.
            // Log-to-charcoal batches continue to require deliberate ignition.
            int nextKind = processingKind();
            if (items.get(FUEL_SLOT).is(Items.CHARCOAL) && nextKind != 0
                && canOutputProcessed(nextKind) && isFirestarter(items.get(IGNITER_SLOT))) {
                ignite();
            }
            return;
        }
        // Both Hardwood Alloy and Pure Mica Insulated Glass require 6000 uninterrupted ticks
        // of a charcoal fire. Switching or removing input cancels progress, never preserves it.
        int currentKind = processingKind();
        if (currentKind != processInputKind) {
            alloyTicks = 0;
            processInputKind = currentKind;
        }
        if (!woodFuel && currentKind != 0 && canOutputProcessed(currentKind)) {
            alloyTicks++;
            if (alloyTicks >= CHARCOAL_BURN_TICKS) {
                items.get(INPUT_SLOT).shrink(1);
                if (items.get(INPUT_SLOT).isEmpty()) items.set(INPUT_SLOT, ItemStack.EMPTY);
                ItemStack output = items.get(PRIMARY_OUTPUT_SLOT);
                ItemStack product = processingOutput(currentKind);
                if (output.isEmpty()) items.set(PRIMARY_OUTPUT_SLOT, product);
                else output.grow(1);
                alloyTicks = 0;
                processInputKind = processingKind();
                setChanged();
            }
        } else alloyTicks = 0;
		// Stop *before* using fuel if this tick would generate a charcoal that cannot fit.
		if (woodFuel && woodByproductTicks + 1 >= WOOD_BYPRODUCT_TICKS && !canOutputCharcoal()) return;
		burnTicks--;
		if (woodFuel && ++woodByproductTicks >= WOOD_BYPRODUCT_TICKS) {
			woodByproductTicks = 0;
			outputCharcoal();
			setChanged();
		}
		if (level.getGameTime() % 80L == 0L)
			level.playSound(null, pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.22F, 0.92F);
		if (burnTicks == 0) {
			woodFuel = false;
			woodByproductTicks = 0;
			refreshBlockState();
			setChanged();
            // Next tick may ignite another charge only for a valid, unblocked recipe.
		} else if (burnTicks % 20 == 0) setChanged();
	}

	private void refreshBlockState() {
		if (level == null) return;
		BlockState state = level.getBlockState(getBlockPos());
		if (!state.hasProperty(FireclayForgeControllerBlock.FORMED)) return;
		boolean lit = formed && burnTicks > 0;
		if (state.getValue(FireclayForgeControllerBlock.FORMED) != formed || state.getValue(FireclayForgeControllerBlock.LIT) != lit)
			level.setBlock(getBlockPos(), state.setValue(FireclayForgeControllerBlock.FORMED, formed)
				.setValue(FireclayForgeControllerBlock.LIT, lit), 3);
	}

	@Override protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, items);
		output.putInt("ForgeLayoutVersion", 3);
		output.putInt("ForgeBurnTicks", burnTicks);
		output.putInt("ForgeAlloyTicks", alloyTicks);
		output.putInt("ForgeProcessInputKind", processInputKind);
		output.putInt("ForgeBurnTotal", burnTotal);
		output.putInt("ForgeByproductTicks", woodByproductTicks);
		output.putBoolean("ForgeWoodFuel", woodFuel);
	}
	@Override protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, items);
		if (input.getIntOr("ForgeLayoutVersion", 0) < 2) {
			// v0.31.34-b: [logs, firestarter, charcoal, prepaid logs].
			// The new recipe output lives at index 5 and is intentionally empty on migration.
			// Preserve ALL four stacks; old prepaid logs remain usable as later fuel.
			NonNullList<ItemStack> old = items;
			items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
			items.set(FUEL_SLOT, old.get(0));
			items.set(IGNITER_SLOT, old.get(1));
			items.set(OUTPUT_SLOT, old.get(2));
			items.set(LEGACY_FUEL_SLOT, old.get(3));
			burnTicks = 0;
			burnTotal = 0;
			woodByproductTicks = 0;
			woodFuel = false;
		} else {
			// v2 slots 0-4 retain their indices; v3 adds the empty primary output at 5.
			burnTotal = Math.max(0, Math.min(CHARCOAL_BURN_TICKS, input.getIntOr("ForgeBurnTotal", 0)));
			burnTicks = Math.max(0, Math.min(burnTotal, input.getIntOr("ForgeBurnTicks", 0)));
			woodByproductTicks = Math.max(0, Math.min(WOOD_BYPRODUCT_TICKS - 1, input.getIntOr("ForgeByproductTicks", 0)));
			woodFuel = input.getBooleanOr("ForgeWoodFuel", false);
		}
		alloyTicks = Math.max(0, Math.min(CHARCOAL_BURN_TICKS - 1, input.getIntOr("ForgeAlloyTicks", 0)));
		processInputKind = input.getIntOr("ForgeProcessInputKind", processingKind());
		formed = false;
		structureCheckCooldown = 0;
	}
}
