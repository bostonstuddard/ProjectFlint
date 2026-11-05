package dev.projectflint.system;

import dev.projectflint.registry.ModEffects;
import dev.projectflint.registry.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PrimitiveEnvironmentEffectSystem {
	private static final int REFRESH_TICKS = 100;
	private static final int REFRESH_THRESHOLD = 60;
	private static final int DRENCHED_AFTER_TICKS = 20 * 90;
	private static final int TERRIFIED_EFFECT_DELAY = 20 * 10;
	private static final int TERRIFIED_DAMAGE_DELAY = 20 * 45;
	private static final int TERRIFIED_DAMAGE_INTERVAL = 40;
	private static final Map<UUID, Long> DARK_SINCE = new HashMap<>();
	private static final Map<UUID, Long> WET_SINCE = new HashMap<>();
    private static final Map<UUID, Long> UNDERGROUND_SINCE = new HashMap<>();
    private static final int SPELUNCA_STACK_TICKS = 20 * 60;
    private static final int MAX_SPELUNCA_AMPLIFIER = 3;

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
                UNDERGROUND_SINCE.remove(player.getUUID());
                player.removeEffect(ModEffects.CLAUSTROPHOBIA);
                player.removeEffect(ModEffects.SPELUNCAPHOBIA);
				continue;
			}

            // Sunlit vertical shafts and strong artificial light (block level 13+) are safe.
            // Sky access alone during nighttime is not direct sunlight.
            boolean inSunlight = level.isBrightOutside() && level.canSeeSky(player.blockPosition().above())
                && !level.isRainingAt(player.blockPosition().above());
            boolean brightEnough = level.getBrightness(LightLayer.BLOCK, player.blockPosition().above()) >= 13;
            if (inSunlight || brightEnough) {
                UNDERGROUND_SINCE.remove(player.getUUID());
                player.removeEffect(ModEffects.CLAUSTROPHOBIA);
                player.removeEffect(ModEffects.SPELUNCAPHOBIA);
            } else {
                if (player.getBlockY() < -40) refresh(player, ModEffects.CLAUSTROPHOBIA);
                else player.removeEffect(ModEffects.CLAUSTROPHOBIA);
                if (player.getBlockY() < 0) {
                    long from = UNDERGROUND_SINCE.computeIfAbsent(player.getUUID(), id -> level.getGameTime());
                    int stacks = Math.min(MAX_SPELUNCA_AMPLIFIER, (int)((level.getGameTime() - from) / SPELUNCA_STACK_TICKS));
                    refresh(player, ModEffects.SPELUNCAPHOBIA, stacks);
                    // Added exhaustion is small, scales with depth exposure, and never deals fatal damage.
                    if (level.getGameTime() % 20L == 0L && player.getFoodData().getFoodLevel() > 6)
                        player.causeFoodExhaustion(0.06F * (stacks + 1));
                } else {
                    UNDERGROUND_SINCE.remove(player.getUUID());
                    player.removeEffect(ModEffects.SPELUNCAPHOBIA);
                }
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
				long start = DARK_SINCE.computeIfAbsent(player.getUUID(), ignored -> level.getGameTime());
				long elapsed = level.getGameTime() - start;
				if (elapsed >= TERRIFIED_EFFECT_DELAY) {
					refresh(player, ModEffects.TERRIFIED);
				} else {
					player.removeEffect(ModEffects.TERRIFIED);
				}
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
        refresh(player, effect, 0);
    }
    private static void refresh(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
        MobEffectInstance active = player.getEffect(effect);
        if (active == null || active.getDuration() < REFRESH_THRESHOLD || active.getAmplifier() != amplifier)
            player.addEffect(new MobEffectInstance(effect, REFRESH_TICKS, amplifier, false, false, true));
    }
}
