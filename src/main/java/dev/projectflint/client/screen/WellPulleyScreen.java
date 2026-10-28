package dev.projectflint.client.screen;

import dev.projectflint.menu.WellPulleyMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class WellPulleyScreen extends AbstractContainerScreen<WellPulleyMenu> {
	private static final int FRAME = 0xFF292720;
	private static final int PANEL = 0xFFC6B58D;
	private static final int WORK = 0xFF71654E;
	private static final int SLOT_BORDER = 0xFF292622;
	private static final int SLOT_FILL = 0xFFD7C9AA;

	public WellPulleyScreen(WellPulleyMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 176;
		imageHeight = 166;
		titleLabelX = 8;
		titleLabelY = 6;
		inventoryLabelX = 8;
		inventoryLabelY = 72;
	}

	@Override
	protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
		int x = leftPos;
		int y = topPos;
		g.fill(x, y, x + imageWidth, y + imageHeight, FRAME);
		g.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, PANEL);
		g.fill(x + 8, y + 18, x + imageWidth - 8, y + 65, WORK);

		drawSlot(g, x + 17, y + 44);
		if (!menu.getSlot(0).hasItem()) g.drawString(font, "R", x + 22, y + 48, 0xFF7C6E58, false);
		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 4; col++) drawSlot(g, x + 96 + col * 18, y + 26 + row * 18);
		}

		g.fill(x + 40, y + 45, x + 82, y + 52, SLOT_BORDER);
		g.fill(x + 41, y + 46, x + 81, y + 51, 0xFF3B352C);
		int progress = menu.progressPixels(40);
		if (progress > 0) g.fill(x + 41, y + 46, x + 41 + progress, y + 51, 0xFF77A6B8);

		int infoColor = menu.structureValid() ? 0xFFE9DFC4 : 0xFFFF8D78;
		g.drawString(font, menu.yieldLabel(), x + 9, y + 21, infoColor, false);
		g.drawString(font, "Quality " + menu.qualityPoints() + "%", x + 9, y + 31, infoColor, false);

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
		if (!menu.getSlot(0).hasItem() && mouseX >= leftPos + 17 && mouseX < leftPos + 33
			&& mouseY >= topPos + 44 && mouseY < topPos + 60) {
			g.setTooltipForNextFrame(Component.translatable("gui.projectflint.rope_hint"), mouseX, mouseY);
		}
	}
}
