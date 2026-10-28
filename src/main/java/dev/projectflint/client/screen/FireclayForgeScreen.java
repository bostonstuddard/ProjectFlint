package dev.projectflint.client.screen;

import dev.projectflint.menu.FireclayForgeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.List;

public final class FireclayForgeScreen extends AbstractContainerScreen<FireclayForgeMenu> {
	private static final int FRAME = 0xFF292520;
	private static final int PANEL = 0xFFB9A68A;
	private static final int WORK = 0xFF5A4537;

	public FireclayForgeScreen(FireclayForgeMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 190;
		imageHeight = 179;
		titleLabelX = 8;
		titleLabelY = 6;
		inventoryLabelX = 8;
		inventoryLabelY = 85;
	}
	@Override protected void init() {
		super.init();
		addRenderableWidget(new RecipeGuideButton(leftPos + imageWidth - 25, topPos + 5, PANEL,
			() -> ProjectFlintRecipeGuideScreen.open(this, ProjectFlintRecipeGuideScreen.Station.FIRECLAY_FORGE)));
		addRenderableWidget(Button.builder(Component.translatable("gui.projectflint.fireclay_forge.ignite"), button -> {
			if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
		}).bounds(leftPos + 109, topPos + 64, 68, 17).build());
	}
	@Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
		int x = leftPos, y = topPos;
		g.fill(x, y, x + imageWidth, y + imageHeight, FRAME);
		g.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, PANEL);
		g.fill(x + 8, y + 18, x + imageWidth - 8, y + 85, WORK);
		for (int sx : new int[]{17,44,71,130,158}) slot(g, x + sx, y + 34);
		if (!menu.getSlot(1).hasItem()) g.drawString(font, "F", x + 49, y + 39, 0xFF786852, false);
		if (!menu.getSlot(2).hasItem()) g.drawString(font, "I", x + 77, y + 39, 0xFF786852, false);
		if (!menu.getSlot(3).hasItem()) g.drawString(font, "R", x + 135, y + 39, 0xFF786852, false);
		if (!menu.getSlot(4).hasItem()) g.drawString(font, "C", x + 163, y + 39, 0xFF786852, false);
		g.fill(x + 96, y + 51, x + 130, y + 59, FRAME);
		int pixels = menu.burnPixels(32);
		if (pixels > 0) g.fill(x + 97, y + 52, x + 97 + pixels, y + 58, 0xFFF4A34F);
		for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) slot(g, x + 14 + 18*col, y + 97 + row*18);
		for (int col = 0; col < 9; col++) slot(g, x + 14 + 18*col, y + 155);
	}
	@Override protected void renderLabels(GuiGraphics g, int mx, int my) {
		super.renderLabels(g, mx, my);
		g.drawString(font, menu.formed() ? "Structure: Formed" : "Structure: Incomplete", 8, 20,
			menu.formed() ? 0xFF58D889 : 0xFFFF9292, false);
		g.drawString(font, menu.lit() ? menu.remainingSeconds() + "s" : "Unlit", 98, 42,
			menu.lit() ? 0xFFFFC36B : 0xFFD7D1C8, false);
	}
	private static void slot(GuiGraphics g, int x, int y) {
		g.fill(x-1, y-1, x+17, y+17, 0xFF2B221B);
		g.fill(x, y, x+16, y+16, 0xFFC7B697);
	}
	@Override public void render(GuiGraphics g, int mx, int my, float partialTick) {
		super.render(g, mx, my, partialTick);
		renderTooltip(g, mx, my);
		if (!menu.getSlot(0).hasItem() && inside(mx, my, 17, 34))
			g.setComponentTooltipForNextFrame(font, List.of(Component.translatable("gui.projectflint.forge.materials")), mx, my);
		if (!menu.getSlot(1).hasItem() && inside(mx, my, 44, 34))
			g.setComponentTooltipForNextFrame(font, List.of(
				Component.translatable("gui.projectflint.forge.fuel"),
				Component.translatable("gui.projectflint.forge.fuel_wood"),
				Component.translatable("gui.projectflint.forge.fuel_charcoal")
			), mx, my);
		if (!menu.getSlot(2).hasItem() && inside(mx, my, 71, 34))
			g.setComponentTooltipForNextFrame(font, List.of(
				Component.translatable("gui.projectflint.forge.firestarter"),
				Component.translatable("gui.projectflint.forge.firestarter_use")
			), mx, my);
		if (!menu.getSlot(3).hasItem() && inside(mx, my, 130, 34))
			g.setComponentTooltipForNextFrame(font, List.of(Component.translatable("gui.projectflint.forge.output")), mx, my);
		if (!menu.getSlot(4).hasItem() && inside(mx, my, 158, 34))
			g.setComponentTooltipForNextFrame(font, List.of(Component.translatable("gui.projectflint.forge.charcoal_output")), mx, my);
	}
	private boolean inside(int mx, int my, int x, int y) {
		return mx >= leftPos+x && mx < leftPos+x+16 && my >= topPos+y && my < topPos+y+16;
	}
}
