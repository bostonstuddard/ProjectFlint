package dev.projectflint.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Creative and Night Vision bypass Project Flint's extra darkness without turning
 * Creative into permanent Night Vision/fullbright.
 */
@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
	@Shadow private boolean updateLightTexture;
	@Unique private int projectflint$lastLightingMode = -1;

	@Inject(method = "updateLightTexture", at = @At("HEAD"))
	private void projectflint$refreshDarknessOnModeChange(float partialTick, CallbackInfo ci) {
		LocalPlayer player = Minecraft.getInstance().player;
		int lightingMode = 0;
		if (player != null) {
			if (player.hasEffect(MobEffects.NIGHT_VISION)) lightingMode = 2;
			else if (player.isCreative()) lightingMode = 1;
		}
		if (lightingMode != projectflint$lastLightingMode) {
			projectflint$lastLightingMode = lightingMode;
			updateLightTexture = true;
		}
	}

	@Redirect(
		method = "updateLightTexture",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z",
			ordinal = 0
		)
	)
	private boolean projectflint$creativeBypassesFlintDarkness(LocalPlayer player, Holder<MobEffect> effect) {
		return player.isCreative() || player.hasEffect(effect);
	}

	@Redirect(
		method = "updateLightTexture",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/GameRenderer;getNightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F"
		)
	)
	private float projectflint$creativeUsesVanillaLightmap(LivingEntity entity, float partialTick) {
		if (entity instanceof LocalPlayer player && player.isCreative() && !player.hasEffect(MobEffects.NIGHT_VISION)) {
			// Negative is a Project Flint-only sentinel consumed by lightmap.fsh.
			// It bypasses hardened darkness without applying Night Vision brightening.
			return -1.0F;
		}
		return GameRenderer.getNightVisionScale(entity, partialTick);
	}
}
