package dev.projectflint.client.screen;

import dev.projectflint.menu.WoodenCabinetMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class WoodenCabinetScreen extends AbstractContainerScreen<WoodenCabinetMenu> {
    private static final int FRAME = 0xFF2B2823;
    private static final int PANEL = 0xFFC7A87A;
    private static final int STORAGE = 0xFF765F48;
    private static final int SLOT_BORDER = 0xFFE2CF9F;
    private static final int SLOT_FILL = 0xFF8C765A;

    public WoodenCabinetScreen(WoodenCabinetMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 173;
        titleLabelX = 7;
        titleLabelY = 6;
        inventoryLabelX = 7;
        inventoryLabelY = 78;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, FRAME);
        graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, PANEL);
        graphics.fill(x + 5, y + 16, x + imageWidth - 5, y + 73, STORAGE);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) drawSlot(graphics, x + 7 + col * 18, y + 18 + row * 18);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) drawSlot(graphics, x + 7 + col * 18, y + 91 + row * 18);
        for (int col = 0; col < 9; col++) drawSlot(graphics, x + 7 + col * 18, y + 149);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_BORDER);
        graphics.fill(x, y, x + 16, y + 16, SLOT_FILL);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
