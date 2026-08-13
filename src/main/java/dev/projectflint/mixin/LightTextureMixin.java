package dev.projectflint.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Creative mode should bypass Project Flint's true-darkness treatment in the same
 * way as Night Vision. We reuse the vanilla NightVisionFactor lightmap input rather
 * than adding another GPU/lightmap mutation path.
 */
@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
	@Redirect(
		method = "updateLightTexture",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z",
			ordinal = 0
		)
	)
	private boolean projectflint$creativeCountsAsNightVision(LocalPlayer player, Holder<MobEffect> effect) {
		return player.isCreative() || player.hasEffect(effect);
	}

	@Redirect(
		method = "updateLightTexture",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/GameRenderer;getNightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F"
		)
	)
	private float projectflint$fullCreativeVision(LivingEntity entity, float partialTick) {
		return entity instanceof LocalPlayer player && player.isCreative()
			? 1.0F
			: GameRenderer.getNightVisionScale(entity, partialTick);
	}
}
