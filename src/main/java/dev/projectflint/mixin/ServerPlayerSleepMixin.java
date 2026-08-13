package dev.projectflint.mixin;

import dev.projectflint.config.GameplayTuning;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Rewards a completed rest for both vanilla beds and Project Flint sleeping bags. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerSleepMixin {
	@Unique
	private boolean projectflint$completedRest;

	@Inject(method = "stopSleepInBed", at = @At("HEAD"))
	private void projectflint$captureCompletedRest(boolean wakeImmediately, boolean updateSleepingPlayers, CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		projectflint$completedRest = player.isSleepingLongEnough();
	}

	@Inject(method = "stopSleepInBed", at = @At("RETURN"))
	private void projectflint$grantMorningRecovery(boolean wakeImmediately, boolean updateSleepingPlayers, CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		if (!projectflint$completedRest || player.isCreative()) {
			projectflint$completedRest = false;
			return;
		}

		projectflint$completedRest = false;
		player.addEffect(new MobEffectInstance(
			MobEffects.REGENERATION,
			GameplayTuning.MORNING_REGENERATION_TICKS,
			0,
			false,
			true,
			true
		));
	}
}
