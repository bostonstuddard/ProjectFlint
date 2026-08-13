package dev.projectflint.client.screen;

import dev.projectflint.menu.AdvancedKilnMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Project Flint-owned Advanced Kiln interface with exact slot-to-background alignment. */
public final class AdvancedKilnScreen extends AbstractContainerScreen<AdvancedKilnMenu> {
	private static final int FRAME = 0xFF2B2823;
	private static final int PANEL = 0xFFB8A88C;
	private static final int WORK_AREA = 0xFF6C6253;
	private static final int SLOT_BORDER = 0xFF292622;
	private static final int SLOT_FILL = 0xFFD7C9AA;
	private static final int PROGRESS_EMPTY = 0xFF3B352C;
	private static final int PROGRESS_FILL = 0xFFD1A15E;

	public AdvancedKilnScreen(AdvancedKilnMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = 166;
		this.titleLabelX = 8;
		this.titleLabelY = 6;
		this.inventoryLabelX = 8;
		this.inventoryLabelY = 72;
	}

	@Override
	protected void init() {
		super.init();
		addRenderableWidget(new RecipeGuideButton(
			leftPos + imageWidth - 25,
			topPos + 5,
			0xFFB8A88C,
			() -> ProjectFlintRecipeGuideScreen.open(this, ProjectFlintRecipeGuideScreen.Station.ADVANCED_KILN)
		));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = leftPos;
		int y = topPos;

		graphics.fill(x, y, x + imageWidth, y + imageHeight, FRAME);
		graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, PANEL);
		graphics.fill(x + 8, y + 18, x + imageWidth - 8, y + 65, WORK_AREA);

		// These coordinates exactly match AdvancedKilnMenu's five kiln slots.
		drawSlotWell(graphics, x + 14, y + 33);
		drawSlotWell(graphics, x + 38, y + 33);
		drawSlotWell(graphics, x + 62, y + 33);
		drawSlotWell(graphics, x + 86, y + 33);
		drawSlotWell(graphics, x + 144, y + 33);

		// Simple left-to-right processing bar. Empty when idle, fills as processing completes.
		graphics.fill(x + 108, y + 38, x + 142, y + 47, SLOT_BORDER);
		graphics.fill(x + 109, y + 39, x + 141, y + 46, PROGRESS_EMPTY);
		int progress = menu.progressPixels(32);
		if (progress > 0) {
			graphics.fill(x + 109, y + 39, x + 109 + progress, y + 46, PROGRESS_FILL);
		}

		drawPlayerInventory(graphics, x, y, 8, 84);
	}

	private static void drawPlayerInventory(GuiGraphics graphics, int originX, int originY, int startX, int startY) {
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				drawSlotWell(graphics, originX + startX + column * 18, originY + startY + row * 18);
			}
		}
		for (int column = 0; column < 9; column++) {
			drawSlotWell(graphics, originX + startX + column * 18, originY + startY + 58);
		}
	}

	private static void drawSlotWell(GuiGraphics graphics, int x, int y) {
		graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_BORDER);
		graphics.fill(x, y, x + 16, y + 16, SLOT_FILL);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}
}
