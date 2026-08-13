package dev.projectflint.system;

import dev.projectflint.config.GameplayTuning;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.HashMap;
import java.util.Map;

public final class WildWolfAggressionSystem {
	private WildWolfAggressionSystem() {
	}

	public static void initialize() {
		ServerTickEvents.END_WORLD_TICK.register(WildWolfAggressionSystem::tickWorld);
	}

	private static void tickWorld(ServerLevel level) {
		if (level.getGameTime() % GameplayTuning.WILD_WOLF_SCAN_INTERVAL_TICKS != 0L) {
			return;
		}

		Map<Wolf, TargetCandidate> nearestTargets = new HashMap<>();
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isCreative() || player.isSpectator()) {
				continue;
			}

			for (Wolf wolf : level.getEntitiesOfClass(
				Wolf.class,
				player.getBoundingBox().inflate(GameplayTuning.WILD_WOLF_AGGRO_RANGE),
				candidate -> candidate.isAlive() && !candidate.isTame()
			)) {
				double distanceSq = wolf.distanceToSqr(player);
				TargetCandidate current = nearestTargets.get(wolf);
				if (current == null || distanceSq < current.distanceSq()) {
					nearestTargets.put(wolf, new TargetCandidate(player, distanceSq));
				}
			}
		}

		for (Map.Entry<Wolf, TargetCandidate> entry : nearestTargets.entrySet()) {
			Wolf wolf = entry.getKey();
			ServerPlayer player = entry.getValue().player();
			wolf.setTarget(player);
			wolf.getLookControl().setLookAt(player, 30.0F, 30.0F);
			wolf.getNavigation().moveTo(player, GameplayTuning.WILD_WOLF_MOVE_SPEED);
		}
	}

	private record TargetCandidate(ServerPlayer player, double distanceSq) {
	}
}
