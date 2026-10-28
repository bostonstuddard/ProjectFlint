package dev.projectflint.client.screen;

import dev.projectflint.recipe.ClayGrinderRecipeGuideRegistry;
import dev.projectflint.recipe.BookshelfRecipeRegistry;
import dev.projectflint.recipe.BrickFurnaceRecipeRegistry;
import dev.projectflint.recipe.PersonalCraftingGuideRegistry;
import dev.projectflint.recipe.WoodenGrinderRecipeRegistry;
import dev.projectflint.recipe.SandingStationRecipeRegistry;
import dev.projectflint.recipe.PrimitiveStoveRecipeRegistry;
import dev.projectflint.item.RecipeParchmentItem;
import dev.projectflint.registry.ModItems;
import dev.projectflint.system.PrimitiveProcessingRecipe;
import dev.projectflint.system.PrimitiveProcessingRegistry;
import dev.projectflint.system.PrimitiveProcessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;

/** Player-facing recipe reference for Project Flint processing stations. */
public final class ProjectFlintRecipeGuideScreen extends Screen {
	private static final int RECIPES_PER_PAGE = 5;
	private static final int PANEL_WIDTH = 360;
	private static final int PANEL_HEIGHT = 238;
	private static final int ROW_HEIGHT = 29;
	private static final int ROW_TOP = 42;
	private static final int FOOTER_PAGE_Y = 194;
	private static final int FOOTER_BUTTON_Y = 210;

	private static Station lastStation = Station.BAKED_KILN;

	private final Screen parent;
	private Station station;
	private int page;
	private Button previousPage;
	private Button nextPage;

	public ProjectFlintRecipeGuideScreen(Screen parent, Station station) {
		super(Component.literal("Project Flint Recipes"));
		this.parent = parent;
		this.station = station;
		lastStation = station;
	}

	public static void open(Screen parent, Station station) {
		lastStation = station;
		if (net.minecraft.client.Minecraft.getInstance() != null) {
			net.minecraft.client.Minecraft.getInstance().setScreen(new ProjectFlintRecipeGuideScreen(parent, station));
		}
	}

	/** Opens the global guide on the last-viewed station (Baked Kiln initially). */
	public static void openGlobal(Screen parent) {
		open(parent, lastStation);
	}

	@Override
	protected void init() {
		int left = (width - PANEL_WIDTH) / 2;
		int top = (height - PANEL_HEIGHT) / 2;

		addRenderableWidget(Button.builder(Component.literal("<"), button -> changeStation(-1))
			.bounds(left + 10, top + 9, 24, 20).build());
		addRenderableWidget(Button.builder(Component.literal(">"), button -> changeStation(1))
			.bounds(left + PANEL_WIDTH - 34, top + 9, 24, 20).build());

		previousPage = addRenderableWidget(Button.builder(Component.literal("Prev"), button -> changePage(-1))
			.bounds(left + 76, top + FOOTER_BUTTON_Y, 58, 20).build());
		nextPage = addRenderableWidget(Button.builder(Component.literal("Next"), button -> changePage(1))
			.bounds(left + PANEL_WIDTH - 134, top + FOOTER_BUTTON_Y, 58, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Back"), button -> onClose())
			.bounds(left + PANEL_WIDTH / 2 - 29, top + FOOTER_BUTTON_Y, 58, 20).build());
		refreshPageButtons();
	}

	private void changeStation(int delta) {
		Station[] stations = Station.values();
		int index = Math.floorMod(station.ordinal() + delta, stations.length);
		station = stations[index];
		lastStation = station;
		page = 0;
		refreshPageButtons();
	}

	private void changePage(int delta) {
		int pages = pageCount(recipes());
		page = Math.max(0, Math.min(page + delta, pages - 1));
		refreshPageButtons();
	}

	private void refreshPageButtons() {
		if (previousPage == null || nextPage == null) return;
		int pages = pageCount(recipes());
		previousPage.active = page > 0;
		nextPage.active = page + 1 < pages;
	}

	private static int pageCount(List<GuideRecipe> recipes) {
		return Math.max(1, (recipes.size() + RECIPES_PER_PAGE - 1) / RECIPES_PER_PAGE);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		int left = (width - PANEL_WIDTH) / 2;
		int top = (height - PANEL_HEIGHT) / 2;
		graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFF2B2823);
		graphics.fill(left + 3, top + 3, left + PANEL_WIDTH - 3, top + PANEL_HEIGHT - 3, 0xFFB8A88C);
		graphics.fill(left + 10, top + 36, left + PANEL_WIDTH - 10, top + 190, 0xFF6C6253);

		graphics.drawCenteredString(font, station.title, width / 2, top + 15, 0xFFFFFFFF);
		List<GuideRecipe> recipes = recipes();
		int pages = pageCount(recipes);
		if (page >= pages) page = pages - 1;

		HoveredInput hovered = null;
		if (recipes.isEmpty()) {
			graphics.drawCenteredString(font, Component.literal("No recipes registered."), width / 2, top + 108, 0xFFFFFFFF);
		} else {
			int start = page * RECIPES_PER_PAGE;
			int end = Math.min(recipes.size(), start + RECIPES_PER_PAGE);
			for (int i = start; i < end; i++) {
				HoveredInput rowHovered = renderRecipe(graphics, recipes.get(i), left + 16, top + ROW_TOP + (i - start) * ROW_HEIGHT, mouseX, mouseY);
				if (rowHovered != null) hovered = rowHovered;
			}
		}

		graphics.drawCenteredString(font, Component.literal("Page " + (page + 1) + "/" + pages), width / 2, top + FOOTER_PAGE_Y, 0xFF3B352C);
		super.render(graphics, mouseX, mouseY, partialTick);
		if (hovered != null) {
			if (hovered.accepted.size() <= 1) graphics.setTooltipForNextFrame(font, hovered.stack, mouseX, mouseY);
			else {
				List<Component> lines = new ArrayList<>();
				lines.add(hovered.stack.getHoverName());
				// Long tags page their tooltip so the whole list remains readable on smaller screens.
				int pageSize = 10;
				int acceptedPages = (hovered.accepted.size() + pageSize - 1) / pageSize;
				int pageIndex = (int)((System.currentTimeMillis() / 3500L) % acceptedPages);
				lines.add(Component.literal("Accepted inputs" + (acceptedPages > 1 ? " (" + (pageIndex + 1) + "/" + acceptedPages + ")" : "") + ":"));
				for (int index = pageIndex * pageSize; index < Math.min(hovered.accepted.size(), (pageIndex + 1) * pageSize); index++) {
					ItemStack accepted = hovered.accepted.get(index);
					lines.add(Component.literal("  " + accepted.getCount() + " × ").append(accepted.getHoverName()));
				}
				graphics.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
			}
		}
	}

	private HoveredInput renderRecipe(GuiGraphics graphics, GuideRecipe recipe, int x, int y, int mouseX, int mouseY) {
		graphics.fill(x, y, x + PANEL_WIDTH - 32, y + 25, 0x66302A23);
		HoveredInput hovered = null;
		List<ItemStack> shownInputs = recipe.variants.get((int)((System.currentTimeMillis() / 1800L) % recipe.variants.size()));
		int itemX = x + 7;
		for (int i = 0; i < shownInputs.size(); i++) {
			ItemStack stack = shownInputs.get(i);
			graphics.renderItem(stack, itemX, y + 5);
			graphics.renderItemDecorations(font, stack, itemX, y + 5);
			if (inside(mouseX, mouseY, itemX, y + 5, 16, 16)) hovered = new HoveredInput(stack, recipe.accepted.get(i));
			itemX += 25;
			if (i + 1 < shownInputs.size()) {
				graphics.drawString(font, "+", itemX - 7, y + 9, 0xFFFFFFFF, false);
			}
		}

		int arrowX = x + 146;
		int outputX = x + 170;
		graphics.drawString(font, "→", arrowX, y + 9, 0xFFFFFFFF, false);
		graphics.renderItem(recipe.output, outputX, y + 5);
		graphics.renderItemDecorations(font, recipe.output, outputX, y + 5);
		if (inside(mouseX, mouseY, outputX, y + 5, 16, 16)) hovered = new HoveredInput(recipe.output, List.of(recipe.output));
		// Settings-style marquee: keep long names inside their row, pause at either end,
		// scroll only when their rendered width exceeds the available text area.
		int labelX = x + 194;
		int labelWidth = PANEL_WIDTH - 40 - 194;
		Component name = recipe.output.getHoverName();
		int overflow = Math.max(0, font.width(name) - labelWidth);
		int offset = 0;
		if (overflow > 0) {
			long travelMs = overflow * 35L;
			long phase = System.currentTimeMillis() % (travelMs * 2L + 1600L);
			if (phase < 600L) offset = 0;
			else if (phase < 600L + travelMs) offset = (int)((phase - 600L) / 35L);
			else if (phase < 1000L + travelMs) offset = overflow;
			else if (phase < 1000L + travelMs * 2L) offset = overflow - (int)((phase - 1000L - travelMs) / 35L);
		}
		graphics.enableScissor(labelX, y + 4, labelX + labelWidth, y + 23);
		graphics.drawString(font, name, labelX - offset, y + 9, 0xFFFFFFFF, false);
		graphics.disableScissor();
		if (inside(mouseX, mouseY, labelX, y + 4, labelWidth, 19)) hovered = new HoveredInput(recipe.output, List.of(recipe.output));
		return hovered;
	}

	private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private List<GuideRecipe> recipes() {
		return condense(switch (station) {
			case BAKED_KILN, MORTAR_KILN -> primitiveRecipes(PrimitiveProcessor.KILN);
			case CLAY_GRINDER -> clayGrinderRecipes();
			case WOODEN_GRINDER -> woodenGrinderRecipes();
			case BOOKSHELF -> bookshelfRecipes();
			case SANDING_STATION -> sandingStationRecipes();
			case ADVANCED_KILN -> advancedKilnRecipes();
			case PRIMITIVE_OVEN -> primitiveOvenRecipes();
			case BRICK_FURNACE -> brickFurnaceRecipes();
			case FIRECLAY_FORGE -> fireclayForgeRecipes();
			case BASIN -> basinRecipes();
			case PERSONAL_CRAFTING -> personalCraftingRecipes();
		});
	}

	private List<GuideRecipe> primitiveRecipes(PrimitiveProcessor processor) {
		List<GuideRecipe> result = new ArrayList<>();
		for (PrimitiveProcessingRecipe recipe : PrimitiveProcessingRegistry.recipesFor(processor)) {
			ItemStack input = firstStack(recipe.input(), 1);
			if (!input.isEmpty()) result.add(new GuideRecipe(List.of(input), recipe.output().copy(), List.of(allStacks(recipe.input(), 1))));
		}
		result.sort(Comparator.comparing(recipe -> recipe.output.getHoverName().getString()));
		return result;
	}

	private static List<GuideRecipe> clayGrinderRecipes() {
		List<GuideRecipe> result = new ArrayList<>();
		for (ClayGrinderRecipeGuideRegistry.Definition recipe : ClayGrinderRecipeGuideRegistry.recipes()) {
			ItemStack input = recipe.inputStack();
			ItemStack output = recipe.outputStack();
			if (!input.isEmpty() && !output.isEmpty()) result.add(new GuideRecipe(List.of(input), output));
		}
		result.sort(Comparator.comparing(recipe -> recipe.output.getHoverName().getString()));
		return result;
	}

	private List<GuideRecipe> woodenGrinderRecipes() {
		List<GuideRecipe> result = clayGrinderRecipes();
		for (WoodenGrinderRecipeRegistry.Definition recipe : WoodenGrinderRecipeRegistry.recipes()) {
			ItemStack a = firstStack(recipe.inputA(), recipe.countA());
			ItemStack b = firstStack(recipe.inputB(), recipe.countB());
			if (!a.isEmpty() && !b.isEmpty()) result.add(new GuideRecipe(List.of(a, b), recipe.result().copy(),
				List.of(allStacks(recipe.inputA(), recipe.countA()), allStacks(recipe.inputB(), recipe.countB()))));
		}
		result.sort(Comparator.comparing(recipe -> recipe.output.getHoverName().getString()));
		return result;
	}

	private static List<GuideRecipe> bookshelfRecipes() {
		List<GuideRecipe> result = new ArrayList<>();
		for (BookshelfRecipeRegistry.Definition recipe : BookshelfRecipeRegistry.recipes()) {
			ItemStack a = bookshelfInput(recipe.inputA(), recipe.countA(), recipe.parchmentRecipeA());
			ItemStack b = bookshelfInput(recipe.inputB(), recipe.countB(), recipe.parchmentRecipeB());
			if (!a.isEmpty() && !b.isEmpty()) result.add(new GuideRecipe(List.of(a, b), recipe.result().copy()));
		}
		result.sort(Comparator.comparing(recipe -> recipe.output.getHoverName().getString()));
		return result;
	}

	private static ItemStack bookshelfInput(net.minecraft.world.item.crafting.Ingredient ingredient, int count, String parchmentRecipe) {
		if (parchmentRecipe != null) {
			ItemStack parchment = parchmentRecipe.isEmpty() ? new ItemStack(ModItems.RECIPE_PARCHMENT) : RecipeParchmentItem.boundStack(parchmentRecipe);
			parchment.setCount(count);
			return parchment;
		}
		return firstStack(ingredient, count);
	}

	private static List<GuideRecipe> sandingStationRecipes() {
		List<GuideRecipe> result = new ArrayList<>();
		for (SandingStationRecipeRegistry.Definition recipe : SandingStationRecipeRegistry.recipes()) {
			ItemStack input = firstStack(recipe.input(), recipe.count());
			if (!input.isEmpty()) result.add(new GuideRecipe(List.of(input), recipe.result().copy(), List.of(allStacks(recipe.input(), recipe.count()))));
		}
		result.sort(Comparator.comparing(recipe -> recipe.output.getHoverName().getString()));
		return result;
	}

	private static List<GuideRecipe> basinRecipes() {
		return List.of(new GuideRecipe(List.of(new ItemStack(Items.STRIPPED_OAK_LOG)), new ItemStack(ModItems.TANNIN, 2)),
			new GuideRecipe(List.of(new ItemStack(Items.STRIPPED_OAK_WOOD)), new ItemStack(ModItems.TANNIN, 2)),
			new GuideRecipe(List.of(new ItemStack(Items.STRIPPED_BIRCH_LOG)), new ItemStack(ModItems.TANNIN, 2)),
			new GuideRecipe(List.of(new ItemStack(Items.STRIPPED_BIRCH_WOOD)), new ItemStack(ModItems.TANNIN, 2)));
	}

	private static List<GuideRecipe> personalCraftingRecipes() {
		List<GuideRecipe> result = new ArrayList<>();
		for (PersonalCraftingGuideRegistry.Definition entry : PersonalCraftingGuideRegistry.recipes())
			result.add(new GuideRecipe(entry.inputs(), entry.result()));
		result.sort(Comparator.comparing(recipe -> recipe.output.getHoverName().getString()));
		return result;
	}

	private static List<GuideRecipe> brickFurnaceRecipes() {
		List<GuideRecipe> results = new ArrayList<>();
		for (PrimitiveProcessingRecipe process : PrimitiveProcessingRegistry.recipesFor(PrimitiveProcessor.KILN)) {
			ItemStack input = firstStack(process.input(), 1);
			if (!input.isEmpty()) results.add(new GuideRecipe(List.of(input), process.resultCopy()));
		}
		results.add(new GuideRecipe(List.of(new ItemStack(Items.LEATHER)), BrickFurnaceRecipeRegistry.TANNED_LEATHER.resultCopy()));
		results.add(new GuideRecipe(List.of(new ItemStack(Items.SAND)), BrickFurnaceRecipeRegistry.GLASS.resultCopy()));
		results.sort(Comparator.comparing(recipe -> recipe.output.getHoverName().getString()));
		return results;
	}

	/** Wood fuel yields charcoal as a byproduct; future material recipes belong in the forge registry. */
	private static List<GuideRecipe> fireclayForgeRecipes() {
		return List.of(new GuideRecipe(List.of(new ItemStack(Items.OAK_LOG, 4)), new ItemStack(Items.CHARCOAL, 4),
			List.of(acceptedForgeWood())));
	}

	private static List<ItemStack> acceptedForgeWood() {
		return BuiltInRegistries.ITEM.stream()
			.map(item -> new ItemStack(item, 4))
			.filter(dev.projectflint.block.entity.FireclayForgeBlockEntity::isCharcoalWood)
			.toList();
	}

	private static List<GuideRecipe> primitiveOvenRecipes() {
		List<GuideRecipe> result = new ArrayList<>();
		for (PrimitiveStoveRecipeRegistry.Definition recipe : PrimitiveStoveRecipeRegistry.parchmentRecipes()) {
			List<ItemStack> inputs = new ArrayList<>();
			List<List<ItemStack>> accepts = new ArrayList<>();
			ItemStack parchment = RecipeParchmentItem.boundStack(recipe.id());
			inputs.add(parchment);
			accepts.add(List.of(parchment));
			for (PrimitiveStoveRecipeRegistry.Requirement requirement : recipe.requirements()) {
				ItemStack ingredient = firstStack(requirement.ingredient(), requirement.count());
				if (!ingredient.isEmpty()) {
					inputs.add(ingredient);
					accepts.add(allStacks(requirement.ingredient(), requirement.count()));
				}
			}
			result.add(new GuideRecipe(inputs, recipe.result().copy(), accepts));
		}
		result.sort(Comparator.comparing(recipe -> recipe.output.getHoverName().getString()));
		return result;
	}

	private static List<GuideRecipe> advancedKilnRecipes() {
		List<GuideRecipe> result = new ArrayList<>();
		result.add(recipe(new ItemStack(ModItems.FIRED_CLAY_BLOCK_MOLD), new ItemStack(ModItems.BRANCH, 4), new ItemStack(Items.FLINT, 4), new ItemStack(ModItems.BURNT_BRANCH_BLOCK)));
		result.add(recipe(new ItemStack(ModItems.FIRED_CLAY_BLOCK_MOLD), new ItemStack(ModItems.BURNT_BRANCH, 8), new ItemStack(ModItems.UNFIRED_CLAY_BALL, 8), new ItemStack(ModItems.CRUDE_CLAY_BLOCK, 3)));
		addAdvancedMold(result, ModItems.FIRED_CLAY_AXE_MOLD, ModItems.FLINT_AXE_PIECES, 1);
		addAdvancedMold(result, ModItems.FIRED_CLAY_BRICK_MOLD, ModItems.MORTAR_BRICKS, 6);
		addAdvancedMold(result, ModItems.FIRED_CLAY_CUTTERS_MOLD, ModItems.FLINT_CUTTER_PIECE, 1);
		addAdvancedMold(result, ModItems.FIRED_CLAY_MULTITOOL_MOLD, ModItems.REFINED_FLINT_MULTITOOL_PIECE, 1);
		addAdvancedMold(result, ModItems.FIRED_CLAY_POT_MOLD, ModItems.CLAY_POT, 1);
		return result;
	}

	private static void addAdvancedMold(List<GuideRecipe> recipes, net.minecraft.world.level.ItemLike mold, net.minecraft.world.level.ItemLike output, int outputCount) {
		recipes.add(recipe(new ItemStack(mold), new ItemStack(ModItems.BURNT_BRANCH, 8), new ItemStack(Items.FLINT, 8), new ItemStack(ModItems.FIRED_CLAY, 4), new ItemStack(output, outputCount)));
	}

	private static GuideRecipe recipe(ItemStack a, ItemStack b, ItemStack output) {
		return new GuideRecipe(List.of(a, b), output);
	}

	private static GuideRecipe recipe(ItemStack a, ItemStack b, ItemStack c, ItemStack output) {
		return new GuideRecipe(List.of(a, b, c), output);
	}

	private static GuideRecipe recipe(ItemStack a, ItemStack b, ItemStack c, ItemStack d, ItemStack output) {
		return new GuideRecipe(List.of(a, b, c, d), output);
	}

	private static ItemStack firstStack(Ingredient ingredient, int count) {
		return ingredient.items().findFirst()
			.map(holder -> new ItemStack(holder.value(), count))
			.orElse(ItemStack.EMPTY);
	}

	@Override
	public void onClose() {
		if (minecraft != null) minecraft.setScreen(parent);
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	/** Whole alternatives cycle as a unit; unrelated ingredient positions are never mixed. */
	private static final class GuideRecipe {
		private final List<ItemStack> inputs;
		private final ItemStack output;
		private final List<List<ItemStack>> variants = new ArrayList<>();
		private final List<List<ItemStack>> accepted = new ArrayList<>();
		private GuideRecipe(List<ItemStack> inputs, ItemStack output) {
			this(inputs, output, inputs.stream().map(List::of).toList());
		}
		private GuideRecipe(List<ItemStack> inputs, ItemStack output, List<List<ItemStack>> choices) {
			this.inputs = List.copyOf(inputs);
			this.output = output;
			variants.add(this.inputs);
			for (List<ItemStack> choice : choices) accepted.add(new ArrayList<>(choice));
		}
		private void merge(GuideRecipe other) {
			variants.addAll(other.variants);
			for (int i = 0; i < accepted.size(); i++) {
				for (ItemStack option : other.accepted.get(i)) {
					if (accepted.get(i).stream().noneMatch(old -> ItemStack.isSameItemSameComponents(old, option) && old.getCount() == option.getCount()))
						accepted.get(i).add(option);
				}
			}
		}
	}

	private record HoveredInput(ItemStack stack, List<ItemStack> accepted) {}

	private static List<ItemStack> allStacks(Ingredient ingredient, int count) {
		return ingredient.items().map(holder -> new ItemStack(holder.value(), count)).toList();
	}

	/** One visible row per equivalent result; hover an input to see every supported option. */
	private static List<GuideRecipe> condense(List<GuideRecipe> recipes) {
		Map<String, GuideRecipe> grouped = new LinkedHashMap<>();
		for (GuideRecipe recipe : recipes) {
			String key = BuiltInRegistries.ITEM.getKey(recipe.output.getItem()) + ":" + recipe.output.getCount()
				+ ":" + recipe.output.getComponents().hashCode() + ":" + recipe.inputs.size();
			GuideRecipe existing = grouped.get(key);
			if (existing == null) grouped.put(key, recipe);
			else existing.merge(recipe);
		}
		return List.copyOf(grouped.values());
	}

	/** RELEASE CHECKLIST: Every new workstation needs a Station entry, its gameplay
	 * recipe source in recipes(), an in-workstation RecipeGuideButton and a check in
	 * docs/RECIPE_GUIDE_RELEASE_CHECKLIST.md. Keep this guide separate from PF-EMI.
	 */
	public enum Station {
		BAKED_KILN(Component.literal("Baked Kiln")),
		MORTAR_KILN(Component.literal("Mortar Kiln")),
		CLAY_GRINDER(Component.literal("Clay Grinder")),
		WOODEN_GRINDER(Component.literal("Wooden Grinder")),
		BOOKSHELF(Component.literal("Bookshelf")),
		SANDING_STATION(Component.literal("Sanding Station")),
		ADVANCED_KILN(Component.literal("Advanced Kiln")),
		PRIMITIVE_OVEN(Component.literal("Primitive Oven")),
		BRICK_FURNACE(Component.literal("Brick Furnace")),
		FIRECLAY_FORGE(Component.literal("Fireclay Forge")),
		BASIN(Component.literal("Basin")),
		PERSONAL_CRAFTING(Component.literal("Personal Crafting"));

		private final Component title;
		Station(Component title) { this.title = title; }
	}
}
