package dev.projectflint.client.screen;

import dev.projectflint.menu.PrimitiveStoveMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class PrimitiveStoveScreen extends AbstractContainerScreen<PrimitiveStoveMenu> {
    private static final int FRAME = 0xFF2B2823, PANEL = 0xFFB8A88C, WORK = 0xFF6C6253;
    private static final int SLOT_BORDER = 0xFF292622, SLOT_FILL = 0xFFD7C9AA;

    public PrimitiveStoveScreen(PrimitiveStoveMenu menu, Inventory inventory, Component title) {
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
            0xFFB8A88C,
            () -> ProjectFlintRecipeGuideScreen.open(this, ProjectFlintRecipeGuideScreen.Station.PRIMITIVE_OVEN)
        ));
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, FRAME);
        g.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, PANEL);
        g.fill(x + 8, y + 18, x + imageWidth - 8, y + 65, WORK);
        drawSlot(g, x + 14, y + 35); drawSlot(g, x + 38, y + 35);
        if (!menu.getSlot(0).hasItem()) g.drawString(font, "W", x + 19, y + 39, 0xFF7C6E58, false); // Ghost wood fuel marker.

        drawSlot(g, x + 70, y + 26); drawSlot(g, x + 88, y + 26);
        drawSlot(g, x + 70, y + 44); drawSlot(g, x + 88, y + 44);
        drawSlot(g, x + 144, y + 35);
        g.fill(x + 110, y + 38, x + 140, y + 47, SLOT_BORDER);
        g.fill(x + 111, y + 39, x + 139, y + 46, 0xFF3B352C);
        int progress = menu.progressPixels(28);
        if (progress > 0) g.fill(x + 111, y + 39, x + 111 + progress, y + 46, 0xFFD1A15E);
        g.fill(x + 58, y + 24, x + 63, y + 58, SLOT_BORDER);
        int burn = menu.burnPixels(32);
        if (burn > 0) g.fill(x + 59, y + 57 - burn, x + 62, y + 57, 0xFFD17B43);
		int seconds = menu.remainingFuelSeconds();
		g.drawString(font, String.format("%d:%02d", seconds / 60, seconds % 60), x + 111, y + 54, 0xFFFFFFFF, false);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) drawSlot(g, x + 8 + col * 18, y + 84 + row * 18);
        for (int col = 0; col < 9; col++) drawSlot(g, x + 8 + col * 18, y + 142);
    }

    private static void drawSlot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, SLOT_BORDER);
        g.fill(x, y, x + 16, y + 16, SLOT_FILL);
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        if (!menu.getSlot(0).hasItem() && mouseX >= leftPos + 14 && mouseX < leftPos + 30
        	&& mouseY >= topPos + 35 && mouseY < topPos + 51) {
        	g.setTooltipForNextFrame(Component.translatable("gui.projectflint.fuel_hint"), mouseX, mouseY);
        }
    }
}
