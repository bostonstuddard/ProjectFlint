package dev.projectflint.client.screen;

import dev.projectflint.block.entity.SandingStationBlockEntity;
import dev.projectflint.menu.SandingStationMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class SandingStationScreen extends AbstractContainerScreen<SandingStationMenu> {
    private static final int RESERVE_X = 12;
    private static final int RESERVE_Y = 72;
    private static final int RESERVE_WIDTH = 152;
    private static final int RESERVE_HEIGHT = 7;

    public SandingStationScreen(SandingStationMenu menu, Inventory inventory, Component title) {
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
            0xFFB99060,
            () -> ProjectFlintRecipeGuideScreen.open(this, ProjectFlintRecipeGuideScreen.Station.SANDING_STATION)
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // Match the established Project Flint workstation framing.
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF2B2823);
        graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFFB99060);
        graphics.fill(x + 8, y + 18, x + imageWidth - 8, y + 86, 0xFF705640);

        drawSlotWell(graphics, x + 44, y + 35);
        drawSlotWell(graphics, x + 116, y + 35);
        drawProcessArrow(graphics, x, y);

        int durability = Math.max(0, Math.min(menu.getDurability(), SandingStationBlockEntity.MAX_DURABILITY));
        Component reserveLabel = Component.literal("Sanding Reserve");
        Component reserveAmount = Component.literal(durability + " / " + SandingStationBlockEntity.MAX_DURABILITY);
        graphics.drawString(font, reserveLabel, x + 12, y + 59, 0xFFF3E8D4, false);
        graphics.drawString(font, reserveAmount, x + imageWidth - 12 - font.width(reserveAmount), y + 59, 0xFFF3E8D4, false);
        drawReserveBar(graphics, x + RESERVE_X, y + RESERVE_Y, durability);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotWell(graphics, x + 8 + col * 18, y + 100 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotWell(graphics, x + 8 + col * 18, y + 158);
        }
    }

    private static void drawProcessArrow(GuiGraphics graphics, int x, int y) {
        graphics.fill(x + 72, y + 42, x + 100, y + 46, 0xFF33271E);
        graphics.fill(x + 98, y + 38, x + 102, y + 50, 0xFF33271E);
        graphics.fill(x + 102, y + 40, x + 106, y + 48, 0xFF33271E);
        graphics.fill(x + 106, y + 42, x + 110, y + 46, 0xFF33271E);
    }

    private static void drawReserveBar(GuiGraphics graphics, int x, int y, int durability) {
        // Eight visible cells make the 32-durability-per-Sanding-Paper refill immediately readable.
        final int cells = SandingStationBlockEntity.MAX_DURABILITY / 32;
        final int cellWidth = 18;
        final int gap = 1;

        graphics.fill(x - 1, y - 1, x + RESERVE_WIDTH + 1, y + RESERVE_HEIGHT + 1, 0xFF2B2823);
        for (int cell = 0; cell < cells; cell++) {
            int cellX = x + cell * (cellWidth + gap);
            int amountInCell = Math.max(0, Math.min(32, durability - cell * 32));
            int filledWidth = Math.round((amountInCell / 32.0F) * cellWidth);

            graphics.fill(cellX, y, cellX + cellWidth, y + RESERVE_HEIGHT, 0xFF4B4034);
            if (filledWidth > 0) {
                graphics.fill(cellX, y, cellX + filledWidth, y + RESERVE_HEIGHT, 0xFFD7B66C);
            }
        }
    }

    private static void drawSlotWell(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF292622);
        graphics.fill(x, y, x + 16, y + 16, 0xFFD7C9AA);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        int barX = leftPos + RESERVE_X;
        int barY = topPos + RESERVE_Y;
        if (mouseX >= barX && mouseX < barX + RESERVE_WIDTH && mouseY >= barY && mouseY < barY + RESERVE_HEIGHT) {
            graphics.setTooltipForNextFrame(
                Minecraft.getInstance().font,
                Component.literal("1 Sanding Paper = 32 reserve • 1 recipe = 1 reserve"),
                mouseX,
                mouseY
            );
        }
    }
}
