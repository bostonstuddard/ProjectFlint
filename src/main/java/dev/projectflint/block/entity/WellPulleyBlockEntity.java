package dev.projectflint.block.entity;

import dev.projectflint.menu.WellPulleyMenu;
import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.AquiferSystem;
import dev.projectflint.system.WellMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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

import java.util.Optional;

public final class WellPulleyBlockEntity extends BaseContainerBlockEntity {
	public static final int ROPE_SLOT = 0;
	public static final int OUTPUT_START = 1;
	public static final int OUTPUT_END = 9;
	public static final int SLOT_COUNT = 9;
	public static final int OUTPUT_SLOTS = 8;
	private static final int BASE_CYCLE_TICKS = 20 * 60;
	private static final int CYCLE_WORK = BASE_CYCLE_TICKS * 100;
	private static final int GUI_PROGRESS_MAX = 10_000;

	private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	private int progress;
	private int progressMax;
	private int qualityPoints;
	private int yieldOrdinal = -1;
	private int structureValid;
	private int validationCooldown;
	private AquiferSystem.AquiferHit cachedAquifer;
	private int pendingClay = -1;
	private int pendingFlintUnits = -1;

	private final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> progressMax <= 0 ? 0 : Math.min(GUI_PROGRESS_MAX, Math.round(progress * GUI_PROGRESS_MAX / (float) progressMax));
				case 1 -> progressMax <= 0 ? 0 : GUI_PROGRESS_MAX;
				case 2 -> qualityPoints;
				case 3 -> yieldOrdinal;
				case 4 -> structureValid;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0 -> progress = value;
				case 1 -> progressMax = value;
				case 2 -> qualityPoints = value;
				case 3 -> yieldOrdinal = value;
				case 4 -> structureValid = value;
				default -> { }
			}
		}

		@Override
		public int getCount() {
			return 5;
		}
	};

	public WellPulleyBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WELL_PULLEY, pos, state);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.projectflint.well_pulley");
	}

	@Override
	protected AbstractContainerMenu createMenu(int syncId, Inventory inventory) {
		return new WellPulleyMenu(syncId, inventory, this, data);
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
		return slot == ROPE_SLOT && stack.is(ModItems.ROPE);
	}

	public boolean structureReady(ServerLevel level) {
		return WellMultiblock.validate(level, getBlockPos()).isPresent();
	}

	/** Update the menu immediately on open, even if the structure is unfinished. */
	public void refreshStructure(ServerLevel level) {
		Optional<AquiferSystem.AquiferHit> aquifer = WellMultiblock.validate(level, getBlockPos());
		validationCooldown = 20;
		cachedAquifer = aquifer.orElse(null);
		structureValid = cachedAquifer == null ? 0 : 1;
		if (cachedAquifer == null) {
			if (progress != 0 || progressMax != 0 || qualityPoints != 0 || yieldOrdinal != -1) {
				progress = 0;
				progressMax = 0;
				qualityPoints = 0;
				yieldOrdinal = -1;
				setChanged();
			}
		} else {
			yieldOrdinal = cachedAquifer.yield().ordinal();
			qualityPoints = Math.max(1, Math.round(cachedAquifer.yield().rateMultiplier() * 100.0F) + ropeQualityBonus(ropeCount()));
		}
	}

	public void serverTick(Level level, BlockPos pos, BlockState state) {
		if (!(level instanceof ServerLevel serverLevel)) return;
		if (validationCooldown-- <= 0 || cachedAquifer == null) refreshStructure(serverLevel);
		if (cachedAquifer == null) return;

		AquiferSystem.AquiferHit hit = cachedAquifer;
		yieldOrdinal = hit.yield().ordinal();
		qualityPoints = Math.max(1, Math.round(hit.yield().rateMultiplier() * 100.0F) + ropeQualityBonus(ropeCount()));
		if (progressMax > 0 && progressMax != CYCLE_WORK) {
			progress = Math.min(CYCLE_WORK, Math.round((float) progress * CYCLE_WORK / progressMax));
		}
		progressMax = CYCLE_WORK;

		if (pendingClay >= 0 || pendingFlintUnits >= 0) {
			if (commitPendingOutput()) {
				pendingClay = -1;
				pendingFlintUnits = -1;
				progress = 0;
			}
			setChanged();
			return;
		}

		// Only an operating, unblocked Well makes a soft rolling pulley sound.
		if (serverLevel.getGameTime() % 70L == 0L) {
			serverLevel.playSound(null, pos, SoundEvents.MINECART_RIDING, SoundSource.BLOCKS, 0.12F, 1.18F);
		}
		progress = Math.min(progressMax, progress + qualityPoints);
		if (progress < progressMax) {
			if ((progress & 31) == 0) setChanged();
			return;
		}

		WellMultiblock.refillWater(serverLevel, pos);
		rollPendingOutput(serverLevel.getRandom());
		if (commitPendingOutput()) {
			pendingClay = -1;
			pendingFlintUnits = -1;
			progress = 0;
		} else {
			progress = progressMax;
		}
		setChanged();
	}

	private int ropeCount() {
		ItemStack rope = items.get(ROPE_SLOT);
		return rope.is(ModItems.ROPE) ? Math.min(16, rope.getCount()) : 0;
	}

	/** A full rope stack concentrates the benefit in one Well; scattering single ropes is inefficient. */
	private static int ropeQualityBonus(int count) {
		int rope = Math.max(0, Math.min(16, count));
		return rope * 3 + rope * rope / 4;
	}

	private void rollPendingOutput(RandomSource random) {
		int cappedQuality = Math.min(100, qualityPoints);
		pendingClay = 0;
		if (random.nextInt(100) < cappedQuality) pendingClay++;
		if (random.nextInt(100) < cappedQuality) pendingClay++;

		pendingFlintUnits = 2;
		if (random.nextInt(100) < cappedQuality) pendingFlintUnits++;
		if (random.nextInt(100) < Math.max(0, Math.min(100, qualityPoints - 25))) pendingFlintUnits++;
	}

	private boolean commitPendingOutput() {
		NonNullList<ItemStack> simulated = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
		for (int i = 0; i < SLOT_COUNT; i++) simulated.set(i, items.get(i).copy());

		if (pendingClay > 0 && !insertIntoBuffer(simulated, new ItemStack(Items.CLAY_BALL, pendingClay))) return false;
		if (pendingFlintUnits >= 4) {
			if (!insertIntoBuffer(simulated, new ItemStack(Items.FLINT))) return false;
		} else if (pendingFlintUnits > 0) {
			if (!insertIntoBuffer(simulated, new ItemStack(ModItems.FLINT_CHIP, pendingFlintUnits))) return false;
		}

		for (int i = OUTPUT_START; i < OUTPUT_END; i++) items.set(i, simulated.get(i));
		return true;
	}

	private static boolean insertIntoBuffer(NonNullList<ItemStack> target, ItemStack incoming) {
		int remaining = incoming.getCount();
		for (int i = OUTPUT_START; i < OUTPUT_END && remaining > 0; i++) {
			ItemStack existing = target.get(i);
			if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, incoming)) continue;
			int room = existing.getMaxStackSize() - existing.getCount();
			int move = Math.min(room, remaining);
			existing.grow(move);
			remaining -= move;
		}
		for (int i = OUTPUT_START; i < OUTPUT_END && remaining > 0; i++) {
			if (!target.get(i).isEmpty()) continue;
			ItemStack placed = incoming.copy();
			int move = Math.min(placed.getMaxStackSize(), remaining);
			placed.setCount(move);
			target.set(i, placed);
			remaining -= move;
		}
		return remaining == 0;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, items);
		output.putInt("Progress", progress);
		output.putInt("ProgressMax", progressMax);
		output.putInt("QualityPoints", qualityPoints);
		output.putInt("YieldOrdinal", yieldOrdinal);
		output.putInt("PendingClay", pendingClay);
		output.putInt("PendingFlintUnits", pendingFlintUnits);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, items);
		progress = Math.max(0, input.getIntOr("Progress", 0));
		progressMax = Math.max(0, input.getIntOr("ProgressMax", 0));
		qualityPoints = Math.max(0, input.getIntOr("QualityPoints", 0));
		yieldOrdinal = input.getIntOr("YieldOrdinal", -1);
		pendingClay = input.getIntOr("PendingClay", -1);
		pendingFlintUnits = input.getIntOr("PendingFlintUnits", -1);
		structureValid = 0;
		validationCooldown = 0;
		cachedAquifer = null;
	}
}
