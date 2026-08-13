package dev.projectflint.mixin;

import dev.projectflint.system.PrimitiveExhaustionContext;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMovementMixin {
	@Inject(method = "checkMovementStatistics", at = @At("HEAD"))
	private void projectflint$beginPrimitiveMovementExhaustion(double dx, double dy, double dz, CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		if (player.isInWater() && (dx != 0.0D || dy != 0.0D || dz != 0.0D)) {
			PrimitiveExhaustionContext.beginSwimmingMovement();
		} else if (player.isSprinting() && player.onGround()) {
			PrimitiveExhaustionContext.beginSprintMovement();
		}
	}

	@Inject(method = "checkMovementStatistics", at = @At("RETURN"))
	private void projectflint$endSprintMovementExhaustion(double dx, double dy, double dz, CallbackInfo ci) {
		PrimitiveExhaustionContext.clear();
	}

	@Inject(method = "jumpFromGround", at = @At("HEAD"))
	private void projectflint$beginSprintJumpExhaustion(CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		if (player.isSprinting()) {
			PrimitiveExhaustionContext.beginSprintJump();
		}
	}

	@Inject(method = "jumpFromGround", at = @At("RETURN"))
	private void projectflint$endSprintJumpExhaustion(CallbackInfo ci) {
		PrimitiveExhaustionContext.clear();
	}
}
