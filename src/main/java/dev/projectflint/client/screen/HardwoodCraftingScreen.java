package dev.projectflint.client.screen;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.projectflint.menu.HardwoodCraftingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** A vanilla-size 3x3 worktable with a server-synchronized, visible crafting countdown. */
public final class HardwoodCraftingScreen extends AbstractContainerScreen<HardwoodCraftingMenu> {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");

    public HardwoodCraftingScreen(HardwoodCraftingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        titleLabelX = 8;
        titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos,
            0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        int ticks = menu.getProgressTicks();
        int x = leftPos + 108;
        int y = topPos + 63;
        graphics.fill(x, y, x + 47, y + 5, 0xFF3E2C1D);
        if (ticks > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + (45 * ticks / HardwoodCraftingMenu.CRAFT_TICKS), y + 4, 0xFFCDA66F);
            String label = ticks >= HardwoodCraftingMenu.CRAFT_TICKS ? "Ready" : String.format(java.util.Locale.ROOT, "%.1fs", (HardwoodCraftingMenu.CRAFT_TICKS - ticks) / 20.0);
            graphics.drawString(font, label, x + 24 - font.width(label) / 2, y + 7, 0x404040, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
