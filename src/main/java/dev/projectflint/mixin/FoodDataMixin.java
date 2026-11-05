package dev.projectflint.mixin;

import dev.projectflint.registry.ModEffects;
import dev.projectflint.system.PrimitiveExhaustionContext;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
	@ModifyVariable(method = "addExhaustion", at = @At("HEAD"), argsOnly = true)
	private float projectflint$scalePrimitiveExhaustion(float exhaustion) {
		return exhaustion * PrimitiveExhaustionContext.multiplier();
	}
	@Redirect(
		method = "tick",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V")
	)
	private void projectflint$modifyNaturalHealingWhileWet(net.minecraft.server.level.ServerPlayer player, float amount) {
		if (player.hasEffect(ModEffects.DRENCHED)) return;
		float multiplier = player.hasEffect(ModEffects.SOAKED) ? 0.5F : 1.0F;
        if (player.hasEffect(ModEffects.SPELUNCAPHOBIA)) {
            multiplier *= 0.8F / (1.0F + player.getEffect(ModEffects.SPELUNCAPHOBIA).getAmplifier() * 0.2F);
        }
        player.heal(amount * multiplier);
	}

}
