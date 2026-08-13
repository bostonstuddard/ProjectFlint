package dev.projectflint.menu;

import dev.projectflint.recipe.ClayGrinderRecipe;
import dev.projectflint.recipe.WoodenGrinderRecipeRegistry;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModItems;
import dev.projectflint.registry.ModMenus;
import dev.projectflint.registry.ModRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;

import java.util.ArrayList;
import java.util.List;

/**
 * Two-input grinder upgrade. With only one input occupied it exposes every Clay
 * Grinder recipe; with both inputs occupied it unlocks Wooden Grinder recipes.
 */
public final class WoodenGrinderMenu extends AbstractContainerMenu {
	public static final int INPUT_A_SLOT = 0;
	public static final int INPUT_B_SLOT = 1;
	public static final int RESULT_SLOT = 2;
	public static final int RECIPE_PREVIEW_START = 3;
	public static final int RECIPE_PREVIEW_COUNT = 6;
	private static final int PLAYER_SLOT_START = RECIPE_PREVIEW_START + RECIPE_PREVIEW_COUNT;
	private static final int PLAYER_SLOT_END = PLAYER_SLOT_START + 36;

	private final ContainerLevelAccess access;
	private final ResultContainer result = new ResultContainer();
	private final SimpleContainer previews = new SimpleContainer(RECIPE_PREVIEW_COUNT);
	private final DataSlot selectedRecipe = DataSlot.standalone();
	private final List<RecipeHolder<ClayGrinderRecipe>> recipes = new ArrayList<>();
	private final Container inputs = new SimpleContainer(2) {
		@Override public void setChanged() {
			super.setChanged();
			if (!WoodenGrinderMenu.this.suppressInputRefresh) {
				WoodenGrinderMenu.this.slotsChanged(this);
			}
		}
	};
	private RecipeHolder<ClayGrinderRecipe> activeSingleRecipe;
	private int activeSingleInputSlot = -1;
	private DualRecipe activeDualRecipe;
	private boolean suppressInputRefresh;

	public WoodenGrinderMenu(int syncId, Inventory inventory, ContainerLevelAccess access) {
		super(ModMenus.WOODEN_GRINDER, syncId);
		this.access = access;
		selectedRecipe.set(-1);
		addDataSlot(selectedRecipe);

		addSlot(new Slot(inputs, 0, 35, 35));
		addSlot(new Slot(inputs, 1, 53, 35));
		addSlot(new Slot(result, 0, 116, 35) {
			@Override public boolean mayPlace(ItemStack stack) { return false; }
			@Override public void onTake(Player player, ItemStack stack) {
				consumeActiveRecipe();
				super.onTake(player, stack);
			}
		});

		for (int i = 0; i < RECIPE_PREVIEW_COUNT; i++) {
			addSlot(new Slot(previews, i, 35 + i * 18, 64) {
				@Override public boolean mayPlace(ItemStack stack) { return false; }
				@Override public boolean mayPickup(Player player) { return false; }
				@Override public boolean isFake() { return true; }
			});
		}

		addStandardInventorySlots(inventory, 8, 100);
	}

	@Override public boolean stillValid(Player player) { return stillValid(access, player, ModBlocks.WOODEN_GRINDER); }
	public int getSelectedRecipeIndex() { return selectedRecipe.get(); }
	public boolean hasDualRecipe() { return activeDualRecipe != null; }

	@Override public void slotsChanged(Container container) {
		super.slotsChanged(container);
		if (container == inputs && !suppressInputRefresh) setupRecipes();
	}

	@Override
	public void clicked(int slotId, int button, ClickType clickType, Player player) {
		if (slotId >= RECIPE_PREVIEW_START && slotId < RECIPE_PREVIEW_START + RECIPE_PREVIEW_COUNT) {
			int recipeIndex = slotId - RECIPE_PREVIEW_START;
			if (clickType == ClickType.PICKUP && !previews.getItem(recipeIndex).isEmpty()) {
				selectedRecipe.set(recipeIndex);
				if (!recipes.isEmpty()) setupSingleResult();
			}
			return;
		}
		super.clicked(slotId, button, clickType, player);
	}

	private void setupRecipes() {
		var preferredRecipeId = activeSingleRecipe == null ? null : activeSingleRecipe.id();

		recipes.clear();
		previews.clearContent();
		activeSingleRecipe = null;
		activeSingleInputSlot = -1;
		activeDualRecipe = null;
		result.setItem(0, ItemStack.EMPTY);

		ItemStack a = inputs.getItem(0);
		ItemStack b = inputs.getItem(1);
		if (!a.isEmpty() && !b.isEmpty()) {
			activeDualRecipe = findDualRecipe(a, b);
			if (activeDualRecipe != null) {
				ItemStack output = activeDualRecipe.result().copy();
				result.setItem(0, output.copy());
				previews.setItem(0, output);
				selectedRecipe.set(0);
			}
			broadcastChanges();
			return;
		}

		int sourceSlot = !a.isEmpty() ? 0 : !b.isEmpty() ? 1 : -1;
		if (sourceSlot < 0) {
			selectedRecipe.set(-1);
			broadcastChanges();
			return;
		}
		activeSingleInputSlot = sourceSlot;
		ItemStack source = inputs.getItem(sourceSlot);
		access.execute((level, pos) -> {
			if (!(level instanceof ServerLevel serverLevel)) return;
			SingleRecipeInput recipeInput = new SingleRecipeInput(source);
			for (RecipeHolder<?> holder : serverLevel.getServer().getRecipeManager().getRecipes()) {
				if (!(holder.value() instanceof ClayGrinderRecipe recipe)) continue;
				if (recipe.getType() != ModRecipes.CLAY_GRINDING || !recipe.matches(recipeInput, serverLevel)) continue;
				recipes.add(new RecipeHolder<>(holder.id(), recipe));
				if (recipes.size() >= RECIPE_PREVIEW_COUNT) break;
			}
			for (int i = 0; i < recipes.size(); i++) previews.setItem(i, recipes.get(i).value().assemble(recipeInput, serverLevel.registryAccess()));
			if (!recipes.isEmpty()) {
				int nextSelection = 0;
				if (preferredRecipeId != null) {
					for (int i = 0; i < recipes.size(); i++) {
						if (recipes.get(i).id().equals(preferredRecipeId)) {
							nextSelection = i;
							break;
						}
					}
				}
				selectedRecipe.set(nextSelection);
				setupSingleResult();
			} else broadcastChanges();
		});
	}

	private void setupSingleResult() {
		int selected = selectedRecipe.get();
		if (selected < 0 || selected >= recipes.size() || activeSingleInputSlot < 0) {
			activeSingleRecipe = null;
			result.setItem(0, ItemStack.EMPTY);
			broadcastChanges();
			return;
		}
		access.execute((level, pos) -> {
			if (!(level instanceof ServerLevel serverLevel)) return;
			activeSingleRecipe = recipes.get(selected);
			result.setItem(0, activeSingleRecipe.value().assemble(new SingleRecipeInput(inputs.getItem(activeSingleInputSlot)), serverLevel.registryAccess()));
			broadcastChanges();
		});
	}

	private void consumeActiveRecipe() {
		DualRecipe dual = activeDualRecipe;
		RecipeHolder<ClayGrinderRecipe> single = activeSingleRecipe;
		int singleSlot = activeSingleInputSlot;

		suppressInputRefresh = true;
		try {
			if (dual != null) {
				// Capture both counts before mutating either slot. SimpleContainer#removeItem
				// calls setChanged(), so allowing a mid-craft refresh here used to clear or
				// replace activeDualRecipe before the second ingredient was consumed.
				int countA = dual.countA();
				int countB = dual.countB();
				inputs.removeItem(INPUT_A_SLOT, countA);
				inputs.removeItem(INPUT_B_SLOT, countB);
			} else if (single != null && singleSlot >= 0) {
				result.setRecipeUsed(single);
				inputs.removeItem(singleSlot, single.value().inputCount());
			}
		} finally {
			suppressInputRefresh = false;
		}

		setupRecipes();
	}

	private static DualRecipe findDualRecipe(ItemStack slot0, ItemStack slot1) {
		return WoodenGrinderRecipeRegistry.find(slot0, slot1)
			.map(match -> new DualRecipe(match.countA(), match.countB(), match.result()))
			.orElse(null);
	}


	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		if (index >= RECIPE_PREVIEW_START && index < PLAYER_SLOT_START) return ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (!slot.hasItem()) return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();
		if (index == RESULT_SLOT) {
			if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, true)) return ItemStack.EMPTY;
			slot.onQuickCraft(stack, copy);
		} else if (index == INPUT_A_SLOT || index == INPUT_B_SLOT) {
			if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, false)) return ItemStack.EMPTY;
		} else if (!moveItemStackTo(stack, INPUT_A_SLOT, INPUT_B_SLOT + 1, false)) return ItemStack.EMPTY;
		if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
		if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
		slot.onTake(player, stack);
		return copy;
	}

	@Override public void removed(Player player) {
		super.removed(player);
		access.execute((level, pos) -> clearContainer(player, inputs));
	}

	private record DualRecipe(int countA, int countB, ItemStack result) {}
}
