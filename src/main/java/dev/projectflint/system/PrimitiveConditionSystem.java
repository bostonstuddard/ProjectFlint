package dev.projectflint.system;

import dev.projectflint.config.GameplayTuning;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Server-authoritative injury and hunger effects. */
public final class PrimitiveConditionSystem {
	private PrimitiveConditionSystem() {}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (server.getTickCount() % 10 == 0) tickConditions(player);
			}
		});
	}

	private static void tickConditions(ServerPlayer player) {
		if (player.isCreative() || player.isSpectator() || !player.isAlive()) return;
		boolean dying = player.getHealth() < GameplayTuning.DYING_HEALTH_THRESHOLD;
		boolean weakened = !dying && player.getHealth() < GameplayTuning.WEAKENED_HEALTH_THRESHOLD;
		boolean hungry = player.getFoodData().getFoodLevel() < GameplayTuning.HUNGRY_FOOD_THRESHOLD;

		if (dying) {
			refresh(player, MobEffects.SLOWNESS, 1);
			refresh(player, MobEffects.MINING_FATIGUE, 0);
		} else if (weakened) {
			refresh(player, MobEffects.SLOWNESS, 0);
		}

		int weaknessSources = (dying ? 1 : 0) + (hungry ? 1 : 0);
		if (weaknessSources > 0) refresh(player, MobEffects.WEAKNESS, weaknessSources - 1);
	}

	private static void refresh(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
		MobEffectInstance active = player.getEffect(effect);
		if (active == null || active.getAmplifier() <= amplifier || active.getDuration() <= 10) {
			player.addEffect(new MobEffectInstance(effect, GameplayTuning.CONDITION_EFFECT_REFRESH_TICKS, amplifier, true, true, true));
		}
	}
}
