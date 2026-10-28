package dev.projectflint.mixin;

import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TitleScreen.class, priority = 500)
public abstract class TitleScreenMixin extends Screen {
	@Shadow private RealmsNotificationsScreen realmsNotificationsScreen;
	@Unique private boolean projectflint$realmsLayoutProcessed;

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void projectflint$disableRealmsNotifications(CallbackInfo ci) {
		// Leave the vanilla Realms widget in place until init is fully finished.
		// Mod Menu's title-screen compatibility hooks need to see the normal
		// vanilla layout while their own AFTER_INIT logic runs.
		this.projectflint$realmsLayoutProcessed = false;
		this.realmsNotificationsScreen = null;
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void projectflint$stripRealmsAfterCompatInit(
		GuiGraphics graphics,
		int mouseX,
		int mouseY,
		float partialTick,
		CallbackInfo ci
	) {
		if (this.projectflint$realmsLayoutProcessed) return;
		this.projectflint$realmsLayoutProcessed = true;

		GuiEventListener realmsButton = null;
		int realmsY = Integer.MIN_VALUE;

		for (GuiEventListener listener : List.copyOf(this.children())) {
			if (!(listener instanceof AbstractWidget widget)) continue;
			if (!(widget.getMessage().getContents() instanceof TranslatableContents contents)) continue;
			if (!"menu.online".equals(contents.getKey())) continue;

			realmsButton = listener;
			realmsY = widget.getY();
			break;
		}

		if (realmsButton == null) return;

		this.removeWidget(realmsButton);

		// If another menu mod has already replaced the Realms row with its
		// own widget (for example Mod Menu's compact/replace styles), keep
		// the rows below where they are. Otherwise close the now-empty row.
		boolean replacementOccupiesRealmsRow = false;
		for (GuiEventListener listener : List.copyOf(this.children())) {
			if (!(listener instanceof AbstractWidget widget)) continue;
			if (widget.getY() != realmsY) continue;
			if (widget.getWidth() < 150 || widget.getMessage().getString().equalsIgnoreCase("Mods")) {
				replacementOccupiesRealmsRow = true;
				break;
			}
		}

		if (replacementOccupiesRealmsRow) return;

		for (GuiEventListener listener : List.copyOf(this.children())) {
			if (!(listener instanceof AbstractWidget widget)) continue;
			if (widget.getY() <= realmsY) continue;
			if (widget.getY() >= this.height - 20) continue;
			widget.setY(widget.getY() - 24);
		}
	}
}
