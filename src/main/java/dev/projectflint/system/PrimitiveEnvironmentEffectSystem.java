package dev.projectflint.system;

import dev.projectflint.registry.ModEffects;
import dev.projectflint.registry.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PrimitiveEnvironmentEffectSystem {
	private static final int REFRESH_TICKS = 100;
	private static final int REFRESH_THRESHOLD = 60;
	private static final int DRENCHED_AFTER_TICKS = 20 * 90;
	private static final int TERRIFIED_DAMAGE_DELAY = 20 * 45;
	private static final int TERRIFIED_DAMAGE_INTERVAL = 40;
	private static final Map<UUID, Long> DARK_SINCE = new HashMap<>();
	private static final Map<UUID, Long> WET_SINCE = new HashMap<>();

	private PrimitiveEnvironmentEffectSystem() {}

	public static void initialize() {
		ServerTickEvents.END_WORLD_TICK.register(PrimitiveEnvironmentEffectSystem::tickWorld);
	}

	private static void tickWorld(ServerLevel level) {
		if (level.getGameTime() % 5L != 0L) return;

		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isCreative() || player.isSpectator()) {
				DARK_SINCE.remove(player.getUUID());
				WET_SINCE.remove(player.getUUID());
				continue;
			}

			boolean soaked = player.isInWater() || level.isRainingAt(player.blockPosition());
			if (soaked) {
				long wetStart = WET_SINCE.computeIfAbsent(player.getUUID(), ignored -> level.getGameTime());
				long wetDuration = level.getGameTime() - wetStart;
				if (wetDuration >= DRENCHED_AFTER_TICKS) {
					player.removeEffect(ModEffects.SOAKED);
					refresh(player, ModEffects.DRENCHED);
				} else {
					player.removeEffect(ModEffects.DRENCHED);
					refresh(player, ModEffects.SOAKED);
				}
			} else {
				WET_SINCE.remove(player.getUUID());
			}

			int effectiveBrightness = level.getMaxLocalRawBrightness(player.blockPosition());
			if (hasCarriedCrudePlantTorch(player)) {
				effectiveBrightness = Math.max(effectiveBrightness, 6);
			}
			boolean terrified = effectiveBrightness < 5;
			if (terrified) {
				refresh(player, ModEffects.TERRIFIED);

				long start = DARK_SINCE.computeIfAbsent(player.getUUID(), ignored -> level.getGameTime());
				long elapsed = level.getGameTime() - start;
				if (elapsed >= TERRIFIED_DAMAGE_DELAY && elapsed % TERRIFIED_DAMAGE_INTERVAL < 5L) {
					player.hurtServer(level, level.damageSources().wither(), 1.0F);
				}
			} else {
				DARK_SINCE.remove(player.getUUID());
				player.removeEffect(ModEffects.TERRIFIED);
			}
		}
	}

	private static boolean hasCarriedCrudePlantTorch(ServerPlayer player) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			if (player.getInventory().getItem(slot).is(ModItems.CRUDE_PLANT_TORCH)) return true;
		}
		return player.getOffhandItem().is(ModItems.CRUDE_PLANT_TORCH);
	}

	private static void refresh(ServerPlayer player, Holder<MobEffect> effect) {
		MobEffectInstance active = player.getEffect(effect);
		if (active == null || active.getDuration() < REFRESH_THRESHOLD) {
			player.addEffect(new MobEffectInstance(effect, REFRESH_TICKS, 0, false, false, true));
		}
	}
}
