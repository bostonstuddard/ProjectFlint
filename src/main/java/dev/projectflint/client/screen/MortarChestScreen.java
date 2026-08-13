package dev.projectflint.client.screen;

import dev.projectflint.menu.MortarChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Three-slot Project Flint mortar chest interface. */
public final class MortarChestScreen extends AbstractContainerScreen<MortarChestMenu> {
	private static final int FRAME = 0xFF2B2823;
	private static final int PANEL = 0xFFC7A87A;
	private static final int STORAGE_AREA = 0xFF765F48;
	private static final int SLOT_BORDER = 0xFFE2CF9F;
	private static final int SLOT_FILL = 0xFF8C765A;

	public MortarChestScreen(MortarChestMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = 146;
		this.titleLabelX = 8;
		this.titleLabelY = 6;
		this.inventoryLabelX = 8;
		this.inventoryLabelY = 53;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = leftPos;
		int y = topPos;
		graphics.fill(x, y, x + imageWidth, y + imageHeight, FRAME);
		graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, PANEL);
		graphics.fill(x + 8, y + 18, x + imageWidth - 8, y + 48, STORAGE_AREA);
		for (int slot = 0; slot < MortarChestMenu.STORAGE_SLOTS; slot++) {
			drawSlotWell(graphics, x + 61 + slot * 18, y + 25);
		}
		drawPlayerInventory(graphics, x, y, 8, 65);
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
