package dev.projectflint.client.screen;

import dev.projectflint.menu.BrickFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.List;

public final class BrickFurnaceScreen extends AbstractContainerScreen<BrickFurnaceMenu> {
	private static final int FRAME = 0xFF282421;
	private static final int PANEL = 0xFFB6A58D;
	private static final int WORK = 0xFF665B50;
	private static final int SLOT_BORDER = 0xFF27231F;
	private static final int SLOT_FILL = 0xFFD6C6AA;

	public BrickFurnaceScreen(BrickFurnaceMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 176;
		imageHeight = 166;
		titleLabelX = 8;
		titleLabelY = 6;
		inventoryLabelX = 8;
		inventoryLabelY = 72;
	}

	@Override
	protected void init() {
		super.init();
		addRenderableWidget(new RecipeGuideButton(
			leftPos + imageWidth - 25,
			topPos + 5,
			PANEL,
			() -> ProjectFlintRecipeGuideScreen.open(this, ProjectFlintRecipeGuideScreen.Station.BRICK_FURNACE)
		));
	}

	@Override
	protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
		int x = leftPos;
		int y = topPos;
		g.fill(x, y, x + imageWidth, y + imageHeight, FRAME);
		g.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, PANEL);
		g.fill(x + 8, y + 18, x + imageWidth - 8, y + 65, WORK);

		drawSlot(g, x + 20, y + 35);
		if (!menu.getSlot(0).hasItem()) g.drawString(font, "C", x + 25, y + 39, 0xFF7C6E58, false);

		drawSlot(g, x + 56, y + 35);
		drawSlot(g, x + 112, y + 26);
		drawSlot(g, x + 130, y + 26);
		drawSlot(g, x + 112, y + 44);
		drawSlot(g, x + 130, y + 44);

		g.fill(x + 78, y + 38, x + 106, y + 47, SLOT_BORDER);
		g.fill(x + 79, y + 39, x + 105, y + 46, 0xFF3B352C);
		int progress = menu.progressPixels(26);
		if (progress > 0) g.fill(x + 79, y + 39, x + 79 + progress, y + 46, 0xFFD19A58);

		g.fill(x + 43, y + 24, x + 48, y + 58, SLOT_BORDER);
		int burn = menu.burnPixels(32);
		if (burn > 0) g.fill(x + 44, y + 57 - burn, x + 47, y + 57, 0xFFD16F3E);
		int seconds = menu.remainingFuelSeconds();
		g.drawString(font, String.format("%d:%02d", seconds / 60, seconds % 60), x + 78, y + 54, 0xFFFFFFFF, false);

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) drawSlot(g, x + 8 + col * 18, y + 84 + row * 18);
		}
		for (int col = 0; col < 9; col++) drawSlot(g, x + 8 + col * 18, y + 142);
	}

	private static void drawSlot(GuiGraphics g, int x, int y) {
		g.fill(x - 1, y - 1, x + 17, y + 17, SLOT_BORDER);
		g.fill(x, y, x + 16, y + 16, SLOT_FILL);
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.render(g, mouseX, mouseY, partialTick);
		renderTooltip(g, mouseX, mouseY);
		if (!menu.getSlot(0).hasItem() && mouseX >= leftPos + 20 && mouseX < leftPos + 36
			&& mouseY >= topPos + 35 && mouseY < topPos + 51) {
			g.setComponentTooltipForNextFrame(font, List.of(
				Component.translatable("gui.projectflint.brick_furnace.fuel"),
				Component.translatable("gui.projectflint.brick_furnace.fuel_time")
			), mouseX, mouseY);
		}
	}
}
