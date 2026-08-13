package dev.projectflint.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void projectflint$preventFallingMeleeAttacks(Entity target, CallbackInfo ci) {
		Player player = (Player) (Object) this;
		if (!player.isCreative() && !player.onGround() && player.getDeltaMovement().y < 0.0D) {
			ci.cancel();
		}
	}
}
