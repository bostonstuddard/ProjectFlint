package dev.projectflint.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
	@Inject(method = "init", at = @At("TAIL"))
	private void projectflint$closeRecipeBook(CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.player.isCreative()) {
			return;
		}
		InventoryScreen screen = (InventoryScreen) (Object) this;
		client.player.getRecipeBook().setOpen(screen.getMenu().getRecipeBookType(), false);
	}

	@Inject(method = "getRecipeBookButtonPosition", at = @At("HEAD"), cancellable = true)
	private void projectflint$hideRecipeButton(CallbackInfoReturnable<ScreenPosition> cir) {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null && !client.player.isCreative()) {
			cir.setReturnValue(new ScreenPosition(-1000, -1000));
		}
	}

	@Inject(method = "onRecipeBookButtonClick", at = @At("HEAD"), cancellable = true)
	private void projectflint$disableRecipeBook(CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null && !client.player.isCreative()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderLabels", at = @At("HEAD"), cancellable = true)
	private void projectflint$hidePersonalCraftingLabels(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null && !client.player.isCreative()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderBg", at = @At("TAIL"))
	private void projectflint$renderPrimitiveInventoryBackground(GuiGraphics graphics, float delta, int mouseX, int mouseY, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.player.isCreative()) {
			return;
		}

		int left = (graphics.guiWidth() - 176) / 2;
		int top = (graphics.guiHeight() - 166) / 2;

		// Hide only the removed crafting interior. Do not draw replacement border lines;
		// the surrounding vanilla inventory frame remains untouched.
		graphics.fill(left + 97, top + 17, left + 173, top + 83, 0xFFC6C6C6);
	}
}
