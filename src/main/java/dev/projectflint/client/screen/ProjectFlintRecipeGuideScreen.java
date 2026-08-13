package dev.projectflint.client.screen;

import dev.projectflint.recipe.ClayGrinderRecipeGuideRegistry;
import dev.projectflint.recipe.WoodenGrinderRecipeRegistry;
import dev.projectflint.recipe.SandingStationRecipeRegistry;
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

		ItemStack hovered = ItemStack.EMPTY;
		if (recipes.isEmpty()) {
			graphics.drawCenteredString(font, Component.literal("No recipes registered."), width / 2, top + 108, 0xFFFFFFFF);
		} else {
			int start = page * RECIPES_PER_PAGE;
			int end = Math.min(recipes.size(), start + RECIPES_PER_PAGE);
			for (int i = start; i < end; i++) {
				ItemStack rowHovered = renderRecipe(graphics, recipes.get(i), left + 16, top + ROW_TOP + (i - start) * ROW_HEIGHT, mouseX, mouseY);
				if (!rowHovered.isEmpty()) hovered = rowHovered;
			}
		}

		graphics.drawCenteredString(font, Component.literal("Page " + (page + 1) + "/" + pages), width / 2, top + FOOTER_PAGE_Y, 0xFF3B352C);
		super.render(graphics, mouseX, mouseY, partialTick);
		if (!hovered.isEmpty()) {
			graphics.setTooltipForNextFrame(font, hovered, mouseX, mouseY);
		}
	}

	private ItemStack renderRecipe(GuiGraphics graphics, GuideRecipe recipe, int x, int y, int mouseX, int mouseY) {
		graphics.fill(x, y, x + PANEL_WIDTH - 32, y + 25, 0x66302A23);
		ItemStack hovered = ItemStack.EMPTY;
		int itemX = x + 7;
		for (int i = 0; i < recipe.inputs.size(); i++) {
			ItemStack stack = recipe.inputs.get(i);
			graphics.renderItem(stack, itemX, y + 5);
			graphics.renderItemDecorations(font, stack, itemX, y + 5);
			if (inside(mouseX, mouseY, itemX, y + 5, 16, 16)) hovered = stack;
			itemX += 25;
			if (i + 1 < recipe.inputs.size()) {
				graphics.drawString(font, "+", itemX - 7, y + 9, 0xFFFFFFFF, false);
			}
		}

		int arrowX = x + 146;
		int outputX = x + 170;
		graphics.drawString(font, "→", arrowX, y + 9, 0xFFFFFFFF, false);
		graphics.renderItem(recipe.output, outputX, y + 5);
		graphics.renderItemDecorations(font, recipe.output, outputX, y + 5);
		if (inside(mouseX, mouseY, outputX, y + 5, 16, 16)) hovered = recipe.output;
		graphics.drawString(font, recipe.output.getHoverName(), x + 194, y + 9, 0xFFFFFFFF, false);
		return hovered;
	}

	private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private List<GuideRecipe> recipes() {
		return switch (station) {
			case BAKED_KILN, MORTAR_KILN -> primitiveRecipes(PrimitiveProcessor.KILN);
			case CLAY_GRINDER -> clayGrinderRecipes();
			case WOODEN_GRINDER -> woodenGrinderRecipes();
			case SANDING_STATION -> sandingStationRecipes();
			case ADVANCED_KILN -> advancedKilnRecipes();
		};
	}

	private List<GuideRecipe> primitiveRecipes(PrimitiveProcessor processor) {
		List<GuideRecipe> result = new ArrayList<>();
		for (PrimitiveProcessingRecipe recipe : PrimitiveProcessingRegistry.recipesFor(processor)) {
			ItemStack input = firstStack(recipe.input(), 1);
			if (!input.isEmpty()) result.add(new GuideRecipe(List.of(input), recipe.output().copy()));
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
			if (!a.isEmpty() && !b.isEmpty()) result.add(new GuideRecipe(List.of(a, b), recipe.result().copy()));
		}
		result.sort(Comparator.comparing(recipe -> recipe.output.getHoverName().getString()));
		return result;
	}

	private static List<GuideRecipe> sandingStationRecipes() {
		List<GuideRecipe> result = new ArrayList<>();
		for (SandingStationRecipeRegistry.Definition recipe : SandingStationRecipeRegistry.recipes()) {
			ItemStack input = firstStack(recipe.input(), recipe.count());
			if (!input.isEmpty()) result.add(new GuideRecipe(List.of(input), recipe.result().copy()));
		}
		result.sort(Comparator.comparing(recipe -> recipe.output.getHoverName().getString()));
		return result;
	}

	private static List<GuideRecipe> advancedKilnRecipes() {
		List<GuideRecipe> result = new ArrayList<>();
		result.add(recipe(new ItemStack(ModItems.FIRED_CLAY_BLOCK_MOLD), new ItemStack(ModItems.BRANCH, 4), new ItemStack(Items.FLINT, 4), new ItemStack(ModItems.BURNT_BRANCH_BLOCK)));
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

	private record GuideRecipe(List<ItemStack> inputs, ItemStack output) {}

	public enum Station {
		BAKED_KILN(Component.literal("Baked Kiln")),
		MORTAR_KILN(Component.literal("Mortar Kiln")),
		CLAY_GRINDER(Component.literal("Clay Grinder")),
		WOODEN_GRINDER(Component.literal("Wooden Grinder")),
		SANDING_STATION(Component.literal("Sanding Station")),
		ADVANCED_KILN(Component.literal("Advanced Kiln"));

		private final Component title;
		Station(Component title) { this.title = title; }
	}
}
