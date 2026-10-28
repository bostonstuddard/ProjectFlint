package dev.projectflint.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LogoRenderer.class)
public abstract class LogoRendererMixin {
	@Shadow private boolean showEasterEgg;
	@Shadow private boolean keepLogoThroughFade;

	@Inject(method = "renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IFI)V", at = @At("HEAD"), cancellable = true)
	private void projectflint$renderScaledLogo(GuiGraphics guiGraphics, int screenWidth, float alpha, int y, CallbackInfo ci) {
		final int sourceWidth = 256;
		final int sourceHeight = 44;
		final int textureWidth = 256;
		final int textureHeight = 64;
		final int logoWidth = 384;
		final int logoHeight = 66;
		final int x = screenWidth / 2 - logoWidth / 2;

		float effectiveAlpha = this.keepLogoThroughFade ? 1.0F : alpha;
		int color = ARGB.white(effectiveAlpha);

		guiGraphics.blit(
			RenderPipelines.GUI_TEXTURED,
			this.showEasterEgg ? LogoRenderer.EASTER_EGG_LOGO : LogoRenderer.MINECRAFT_LOGO,
			x,
			y,
			0.0F,
			0.0F,
			logoWidth,
			logoHeight,
			sourceWidth,
			sourceHeight,
			textureWidth,
			textureHeight,
			color
		);

		// Intentionally cancel vanilla rendering here so the Java Edition subtitle is not drawn.
		ci.cancel();
	}
}
