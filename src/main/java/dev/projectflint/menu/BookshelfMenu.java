package dev.projectflint.menu;

import dev.projectflint.compat.flintquests.FlintQuestsBridge;
import dev.projectflint.block.entity.BookshelfBlockEntity;
import dev.projectflint.item.RecipeParchmentItem;
import dev.projectflint.recipe.BookshelfRecipeRegistry;
import dev.projectflint.registry.ModItems;
import dev.projectflint.registry.ModMenus;
import dev.projectflint.system.ModAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Bookshelf workstation: two responsive inputs, selectable research results, one output. */
public final class BookshelfMenu extends AbstractContainerMenu {
	public static final int INPUT_A_SLOT = 0;
	public static final int INPUT_B_SLOT = 1;
	public static final int RESULT_SLOT = 2;
	public static final int RECIPE_PREVIEW_START = 3;
	public static final int RECIPE_PREVIEW_COUNT = 6;
	private static final int PLAYER_SLOT_START = RECIPE_PREVIEW_START + RECIPE_PREVIEW_COUNT;
	private static final int PLAYER_SLOT_END = PLAYER_SLOT_START + 36;

	private final BookshelfBlockEntity shelf;
	private final ResultContainer result = new ResultContainer();
	private final SimpleContainer previews = new SimpleContainer(RECIPE_PREVIEW_COUNT);
	private final SimpleContainer inputs = new SimpleContainer(2) {
		@Override public void setChanged() {
			super.setChanged();
			if (!suppressRefresh) BookshelfMenu.this.slotsChanged(this);
		}
	};
	private final DataSlot books;
	private final DataSlot selectedRecipe = DataSlot.standalone();
	private final List<BookshelfRecipeRegistry.Match> activeRecipes = new ArrayList<>();
	private boolean suppressRefresh;

	public BookshelfMenu(int id, Inventory inventory) { this(id, inventory, null); }

	public BookshelfMenu(int id, Inventory inventory, BookshelfBlockEntity shelf) {
		super(ModMenus.BOOKSHELF, id);
		this.shelf = shelf;
		this.books = shelf == null ? DataSlot.standalone() : new DataSlot() {
			@Override public int get() { return shelf.getBooksStored(); }
			@Override public void set(int value) { shelf.setBooksStored(value); }
		};
		selectedRecipe.set(-1);
		addDataSlot(books);
		addDataSlot(selectedRecipe);

		addSlot(new Slot(inputs, INPUT_A_SLOT, 35, 35));
		addSlot(new Slot(inputs, INPUT_B_SLOT, 53, 35));
		addSlot(new Slot(result, 0, 116, 35) {
			@Override public boolean mayPlace(ItemStack stack) { return false; }
			@Override public void onTake(Player player, ItemStack stack) {
				BookshelfRecipeRegistry.Match recipe = selectedMatch();
				if (recipe != null) {
					suppressRefresh = true;
					try {
						inputs.removeItem(INPUT_A_SLOT, recipe.countA());
						inputs.removeItem(INPUT_B_SLOT, recipe.countB());
					} finally { suppressRefresh = false; }
					if (player instanceof ServerPlayer serverPlayer) {
						if (recipe.result().is(Items.BOOK)) ModAdvancements.awardAll(serverPlayer, "bookshelf_book");
						if (recipe.result().is(Items.WRITABLE_BOOK)) ModAdvancements.awardAll(serverPlayer, "bookshelf_book_and_quill");
						if (recipe.result().is(ModItems.RECIPE_PARCHMENT) && RecipeParchmentItem.isBound(recipe.result())) {
							ModAdvancements.awardAll(serverPlayer, "recipe_research");
						}
					}
				}
				if (player instanceof ServerPlayer serverPlayer) FlintQuestsBridge.onCraft(serverPlayer, "bookshelf", stack);
				refresh();
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
		refresh();
	}

	@Override public void slotsChanged(net.minecraft.world.Container container) {
		super.slotsChanged(container);
		if (container == inputs && !suppressRefresh) refresh();
	}

	@Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
		if (slotId >= RECIPE_PREVIEW_START && slotId < RECIPE_PREVIEW_START + RECIPE_PREVIEW_COUNT) {
			int index = slotId - RECIPE_PREVIEW_START;
			if (clickType == ClickType.PICKUP && index < activeRecipes.size() && !previews.getItem(index).isEmpty()) {
				selectedRecipe.set(index);
				updateResult();
			}
			return;
		}
		super.clicked(slotId, button, clickType, player);
	}

	private void refresh() {
		int previous = selectedRecipe.get();
		activeRecipes.clear();
		activeRecipes.addAll(BookshelfRecipeRegistry.findAll(inputs.getItem(INPUT_A_SLOT), inputs.getItem(INPUT_B_SLOT), books.get()));
		previews.clearContent();
		for (int i = 0; i < Math.min(RECIPE_PREVIEW_COUNT, activeRecipes.size()); i++) previews.setItem(i, activeRecipes.get(i).result().copy());
		selectedRecipe.set(activeRecipes.isEmpty() ? -1 : Math.min(Math.max(previous, 0), activeRecipes.size() - 1));
		updateResult();
	}

	private BookshelfRecipeRegistry.Match selectedMatch() {
		int selected = selectedRecipe.get();
		return selected >= 0 && selected < activeRecipes.size() ? activeRecipes.get(selected) : null;
	}

	private void updateResult() {
		BookshelfRecipeRegistry.Match match = selectedMatch();
		result.setItem(0, match == null ? ItemStack.EMPTY : match.result().copy());
		broadcastChanges();
	}

	public int booksStored() { return books.get(); }
	public int getSelectedRecipeIndex() { return selectedRecipe.get(); }

	@Override public boolean stillValid(Player player) { return shelf == null || shelf.stillValid(player); }

	@Override public ItemStack quickMoveStack(Player player, int index) {
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
		if (!player.level().isClientSide()) clearContainer(player, inputs);
	}
}
