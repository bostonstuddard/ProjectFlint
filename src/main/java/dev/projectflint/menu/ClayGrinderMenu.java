package dev.projectflint.menu;

import dev.projectflint.recipe.ClayGrinderRecipe;
import dev.projectflint.registry.ModBlocks;
import dev.projectflint.registry.ModMenus;
import dev.projectflint.registry.ModRecipes;
import dev.projectflint.system.ModAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
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
 * Fully Project Flint-owned Clay Grinder container.
 *
 * Alpha.22 provides an input, selectable output previews, and an output slot but
 * intentionally ships no grinder recipes yet.
 */
public final class ClayGrinderMenu extends AbstractContainerMenu {
	public static final int INPUT_SLOT = 0;
	public static final int RESULT_SLOT = 1;
	public static final int RECIPE_PREVIEW_START = 2;
	public static final int RECIPE_PREVIEW_COUNT = 6;
	private static final int PLAYER_SLOT_START = RECIPE_PREVIEW_START + RECIPE_PREVIEW_COUNT;
	private static final int PLAYER_SLOT_END = PLAYER_SLOT_START + 36;

	private final ContainerLevelAccess access;
	private final ResultContainer result = new ResultContainer();
	private final SimpleContainer previews = new SimpleContainer(RECIPE_PREVIEW_COUNT);
	private final DataSlot selectedRecipe = DataSlot.standalone();
	private final List<RecipeHolder<ClayGrinderRecipe>> recipes = new ArrayList<>();
	private final Container input = new SimpleContainer(1) {
		@Override
		public void setChanged() {
			super.setChanged();
			ClayGrinderMenu.this.slotsChanged(this);
		}
	};
	private RecipeHolder<ClayGrinderRecipe> activeRecipe;
	private boolean suppressInputRefresh;

	public ClayGrinderMenu(int syncId, Inventory inventory, ContainerLevelAccess access) {
		super(ModMenus.CLAY_GRINDER, syncId);
		this.access = access;
		selectedRecipe.set(-1);
		addDataSlot(selectedRecipe);

		addSlot(new Slot(input, 0, 44, 35));
		addSlot(new Slot(result, 0, 116, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public void onTake(Player player, ItemStack stack) {
				RecipeHolder<ClayGrinderRecipe> completedRecipe = activeRecipe;
				if (completedRecipe != null) result.setRecipeUsed(completedRecipe);

				suppressInputRefresh = true;
				try {
					input.removeItem(0, completedRecipe != null ? completedRecipe.value().inputCount() : 1);
				} finally {
					suppressInputRefresh = false;
				}

				if (player instanceof ServerPlayer serverPlayer && stack.is(ItemTags.WOODEN_SLABS)) {
					ModAdvancements.awardAll(serverPlayer, "flint_axe_slabs");
				}
				setupRecipes();
				super.onTake(player, stack);
			}
		});

		for (int i = 0; i < RECIPE_PREVIEW_COUNT; i++) {
			addSlot(new Slot(previews, i, 35 + i * 18, 64) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return false;
				}

				@Override
				public boolean mayPickup(Player player) {
					return false;
				}

				@Override
				public boolean isFake() {
					return true;
				}
			});
		}

		addStandardInventorySlots(inventory, 8, 100);
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, ModBlocks.CLAY_GRINDER);
	}

	public int getSelectedRecipeIndex() {
		return selectedRecipe.get();
	}

	@Override
	public void slotsChanged(Container container) {
		super.slotsChanged(container);
		if (container == input && !suppressInputRefresh) setupRecipes();
	}

	@Override
	public void clicked(int slotId, int button, ClickType clickType, Player player) {
		if (slotId >= RECIPE_PREVIEW_START && slotId < RECIPE_PREVIEW_START + RECIPE_PREVIEW_COUNT) {
			int recipeIndex = slotId - RECIPE_PREVIEW_START;
			if (clickType == ClickType.PICKUP && previews.getItem(recipeIndex).isEmpty() == false) {
				selectedRecipe.set(recipeIndex);
				if (!recipes.isEmpty()) setupResult();
			}
			return;
		}
		super.clicked(slotId, button, clickType, player);
	}

	private void setupRecipes() {
		var preferredRecipeId = activeRecipe == null ? null : activeRecipe.id();

		recipes.clear();
		previews.clearContent();
		activeRecipe = null;
		result.setItem(0, ItemStack.EMPTY);

		ItemStack inputStack = input.getItem(0);
		if (inputStack.isEmpty()) {
			selectedRecipe.set(-1);
			broadcastChanges();
			return;
		}

		access.execute((level, pos) -> {
			if (!(level instanceof ServerLevel serverLevel)) return;
			SingleRecipeInput recipeInput = new SingleRecipeInput(inputStack);
			for (RecipeHolder<?> holder : serverLevel.getServer().getRecipeManager().getRecipes()) {
				if (!(holder.value() instanceof ClayGrinderRecipe recipe)) continue;
				if (recipe.getType() != ModRecipes.CLAY_GRINDING || !recipe.matches(recipeInput, serverLevel)) continue;
				recipes.add(new RecipeHolder<>(holder.id(), recipe));
				if (recipes.size() >= RECIPE_PREVIEW_COUNT) break;
			}

			for (int i = 0; i < recipes.size(); i++) {
				previews.setItem(i, recipes.get(i).value().assemble(recipeInput, serverLevel.registryAccess()));
			}
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
				setupResult();
			} else {
				broadcastChanges();
			}
		});
	}

	private void setupResult() {
		int selected = selectedRecipe.get();
		if (selected < 0 || selected >= recipes.size()) {
			activeRecipe = null;
			result.setItem(0, ItemStack.EMPTY);
			broadcastChanges();
			return;
		}
		access.execute((level, pos) -> {
			if (!(level instanceof ServerLevel serverLevel)) return;
			activeRecipe = recipes.get(selected);
			result.setItem(0, activeRecipe.value().assemble(new SingleRecipeInput(input.getItem(0)), serverLevel.registryAccess()));
			broadcastChanges();
		});
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
		} else if (index == INPUT_SLOT) {
			if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_END, false)) return ItemStack.EMPTY;
		} else if (!moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
		else slot.setChanged();
		if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
		slot.onTake(player, stack);
		return copy;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		access.execute((level, pos) -> clearContainer(player, input));
	}
}
