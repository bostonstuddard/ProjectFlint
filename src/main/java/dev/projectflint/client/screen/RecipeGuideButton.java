package dev.projectflint.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Compact, inset recipe-book control for Project Flint workstation title bars. */
public final class RecipeGuideButton extends AbstractButton {
	private static final ItemStack BOOK_ICON = new ItemStack(Items.BOOK);
	private final int baseFill;
	private final Runnable action;

	public RecipeGuideButton(int x, int y, int baseFill, Runnable action) {
		super(x, y, 20, 20, Component.literal("Recipes"));
		this.baseFill = baseFill;
		this.action = action;
	}

	@Override
	public void onPress(InputWithModifiers input) {
		action.run();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}

	@Override
	protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		int x = getX();
		int y = getY();
		int border = isHoveredOrFocused() ? 0xFFE8D36B : 0xFF292622;
		int fill = isHoveredOrFocused() ? 0xFFD7C9AA : baseFill;

		graphics.fill(x, y, x + width, y + height, border);
		graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, fill);
		graphics.renderItem(BOOK_ICON, x + 2, y + 2);

		if (isHovered()) {
			graphics.setTooltipForNextFrame(Minecraft.getInstance().font, Component.literal("Recipes"), mouseX, mouseY);
		}
	}
}
