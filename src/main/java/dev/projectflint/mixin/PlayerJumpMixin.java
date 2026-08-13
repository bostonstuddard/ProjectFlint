package dev.projectflint.mixin;

import dev.projectflint.config.GameplayTuning;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class PlayerJumpMixin {
	@Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
	private void projectflint$preventHungryJump(CallbackInfo ci) {
		if ((Object) this instanceof Player player
			&& !player.isCreative()
			&& !player.isSpectator()
			&& player.getFoodData().getFoodLevel() < GameplayTuning.HUNGRY_FOOD_THRESHOLD) {
			ci.cancel();
		}
	}
}
