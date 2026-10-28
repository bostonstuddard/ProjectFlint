package dev.projectflint.client.screen;

import dev.projectflint.menu.NestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class NestScreen extends AbstractContainerScreen<NestMenu> {
	private static final int FRAME = 0xFF2B2823;
	private static final int PANEL = 0xFFC7A87A;
	private static final int STORAGE_AREA = 0xFF765F48;
	private static final int SLOT_BORDER = 0xFFE2CF9F;
	private static final int SLOT_FILL = 0xFF8C765A;
	private static final int OCCUPIED_TEXT = 0xFF4B2D16;
	private static final int EMPTY_TEXT = 0xFF5C554C;

	public NestScreen(NestMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 176;
		imageHeight = 158;
		titleLabelX = 8;
		titleLabelY = 6;
		inventoryLabelX = 8;
		inventoryLabelY = 65;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = leftPos;
		int y = topPos;
		graphics.fill(x, y, x + imageWidth, y + imageHeight, FRAME);
		graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, PANEL);
		graphics.fill(x + 8, y + 25, x + imageWidth - 8, y + 56, STORAGE_AREA);

		for (int slot = 0; slot < NestMenu.NEST_SLOTS; slot++) {
			drawSlotWell(graphics, x + 52 + slot * 18, y + 32);
		}
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				drawSlotWell(graphics, x + 8 + col * 18, y + 77 + row * 18);
			}
		}
		for (int col = 0; col < 9; col++) {
			drawSlotWell(graphics, x + 8 + col * 18, y + 135);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		super.renderLabels(graphics, mouseX, mouseY);
		Component status = Component.translatable(menu.hasOccupant()
			? "gui.projectflint.nest.occupied"
			: "gui.projectflint.nest.unoccupied");
		graphics.drawString(font, status, imageWidth - 8 - font.width(status), 6,
			menu.hasOccupant() ? OCCUPIED_TEXT : EMPTY_TEXT, false);
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
