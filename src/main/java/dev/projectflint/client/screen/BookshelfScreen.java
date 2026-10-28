package dev.projectflint.client.screen;

import dev.projectflint.menu.BookshelfMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Wooden-Grinder-style Bookshelf workstation screen. */
public final class BookshelfScreen extends AbstractContainerScreen<BookshelfMenu> {
	public BookshelfScreen(BookshelfMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 176;
		imageHeight = 182;
		inventoryLabelY = 88;
	}

	@Override
	protected void init() {
		super.init();
		addRenderableWidget(new RecipeGuideButton(
			leftPos + imageWidth - 25,
			topPos + 5,
			0xFFB79A70,
			() -> ProjectFlintRecipeGuideScreen.open(this, ProjectFlintRecipeGuideScreen.Station.BOOKSHELF)
		));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = leftPos;
		int y = topPos;
		graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF2B2823);
		graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFFB79A70);
		graphics.fill(x + 8, y + 18, x + imageWidth - 8, y + 86, 0xFF66523D);
		drawSlotWell(graphics, x + 35, y + 35, false);
		drawSlotWell(graphics, x + 53, y + 35, false);
		drawSlotWell(graphics, x + 116, y + 35, false);

		graphics.fill(x + 78, y + 42, x + 100, y + 46, 0xFF30271F);
		graphics.fill(x + 98, y + 38, x + 102, y + 50, 0xFF30271F);
		graphics.fill(x + 102, y + 40, x + 106, y + 48, 0xFF30271F);
		graphics.fill(x + 106, y + 42, x + 110, y + 46, 0xFF30271F);

		for (int i = 0; i < BookshelfMenu.RECIPE_PREVIEW_COUNT; i++) {
			boolean activePreview = menu.getSelectedRecipeIndex() == i && menu.getSlot(BookshelfMenu.RECIPE_PREVIEW_START + i).hasItem();
			drawSlotWell(graphics, x + 35 + i * 18, y + 64, activePreview);
		}
		graphics.drawString(font, "Books: " + menu.booksStored() + "/4", x + 88, y + 7, 0xFF30271F, false);

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) drawSlotWell(graphics, x + 8 + col * 18, y + 100 + row * 18, false);
		}
		for (int col = 0; col < 9; col++) drawSlotWell(graphics, x + 8 + col * 18, y + 158, false);
	}

	private static void drawSlotWell(GuiGraphics graphics, int x, int y, boolean selected) {
		graphics.fill(x - 1, y - 1, x + 17, y + 17, selected ? 0xFFE8D36B : 0xFF292622);
		graphics.fill(x, y, x + 16, y + 16, 0xFFD7C9AA);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}
}
