package dev.projectflint.client.screen;

import dev.projectflint.menu.WovenChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Woven chest storage laid out like a vanilla dispenser. */
public final class WovenChestScreen extends AbstractContainerScreen<WovenChestMenu> {
	private static final int FRAME = 0xFF2B2823;
	private static final int PANEL = 0xFFC7A87A;
	private static final int STORAGE_AREA = 0xFF765F48;
	private static final int SLOT_BORDER = 0xFFE2CF9F;
	private static final int SLOT_FILL = 0xFF8C765A;

	public WovenChestScreen(WovenChestMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = 184;
		this.imageHeight = 166;
		this.titleLabelX = 11;
		this.titleLabelY = 6;
		this.inventoryLabelX = 11;
		this.inventoryLabelY = 72;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = leftPos;
		int y = topPos;
		graphics.fill(x, y, x + imageWidth, y + imageHeight, FRAME);
		graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, PANEL);
		graphics.fill(x + 8, y + 18, x + imageWidth - 8, y + 70, STORAGE_AREA);

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				drawSlotWell(graphics, x + 66 + col * 18, y + 17 + row * 18);
			}
		}

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				drawSlotWell(graphics, x + 11 + col * 18, y + 84 + row * 18);
			}
		}
		for (int col = 0; col < 9; col++) {
			drawSlotWell(graphics, x + 11 + col * 18, y + 142);
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
