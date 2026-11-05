package dev.projectflint.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Placeholder for future player-facing difficulty and creative settings. */
public final class ProjectFlintConfigScreen extends Screen {
    private final Screen parent;
    public ProjectFlintConfigScreen(Screen parent) {
        super(Component.literal("Project Flint"));
        this.parent = parent;
    }
    @Override protected void init() {
        int cx = width / 2;
        int cy = height / 2;
        addRenderableWidget(Button.builder(Component.literal("Field Guide"),
            button -> minecraft.setScreen(new ProjectFlintFieldGuideScreen(this)))
            .bounds(cx - 104, cy + 16, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Recipes"),
            button -> ProjectFlintRecipeGuideScreen.openGlobal(this))
            .bounds(cx + 4, cy + 16, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
            .bounds(cx - 50, cy + 44, 100, 20).build());
    }
    @Override public void render(GuiGraphics g, int mx, int my, float partialTick) {
        g.fill(0, 0, width, height, 0xE0201B1A);
        g.drawCenteredString(font, title, width / 2, height / 2 - 47, 0xFFE6D0A3);
        g.drawCenteredString(font, "Nothing to see here! :) - for now.", width / 2,
            height / 2 - 17, 0xFFD4C7B5);
        super.render(g, mx, my, partialTick);
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
}
