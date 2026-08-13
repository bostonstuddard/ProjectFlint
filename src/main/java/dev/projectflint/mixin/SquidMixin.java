package dev.projectflint.mixin;

import dev.projectflint.config.GameplayTuning;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Squids become territorial predators only while a valid player is in water. */
@Mixin(Squid.class)
public abstract class SquidMixin {
	@Shadow Vec3 movementVector;
	@Unique private int projectflint$attackCooldown;


	@Redirect(
		method = "registerGoals",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V")
	)
	private void projectflint$removeVanillaFleeGoal(GoalSelector selector, int priority, Goal goal) {
		if (priority == 1) return;
		selector.addGoal(priority, goal);
	}

	@Inject(method = "aiStep", at = @At("TAIL"))
	private void projectflint$huntSwimmingPlayers(CallbackInfo ci) {
		Squid squid = (Squid) (Object) this;
		if (!(squid.level() instanceof ServerLevel level)) {
			return;
		}
		if (projectflint$attackCooldown > 0) {
			projectflint$attackCooldown--;
		}

		Player target = level.getNearestPlayer(
			squid.getX(), squid.getY(), squid.getZ(), GameplayTuning.SQUID_AGGRO_RANGE,
			entity -> entity instanceof Player player
				&& player.isAlive()
				&& !player.isCreative()
				&& !player.isSpectator()
				&& player.isInWater()
		);
		if (target == null || !squid.isInWater()) {
			squid.setTarget(null);
			return;
		}

		squid.setTarget(target);
		Vec3 toward = target.getEyePosition().subtract(squid.position());
		if (toward.lengthSqr() > 0.0001D) {
			movementVector = toward.normalize().scale(GameplayTuning.SQUID_CHASE_SPEED);
		}

		double attackRangeSq = GameplayTuning.SQUID_ATTACK_RANGE * GameplayTuning.SQUID_ATTACK_RANGE;
		if (projectflint$attackCooldown <= 0 && squid.distanceToSqr(target) <= attackRangeSq) {
			target.hurtServer(level, squid.damageSources().mobAttack(squid), GameplayTuning.SQUID_ATTACK_DAMAGE);
			projectflint$attackCooldown = GameplayTuning.SQUID_ATTACK_COOLDOWN_TICKS;
		}
	}
}
