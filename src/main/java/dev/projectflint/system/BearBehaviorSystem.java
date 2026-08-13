package dev.projectflint.system;

import dev.projectflint.entity.BlackBear;
import dev.projectflint.entity.BrownBear;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public final class BearBehaviorSystem {
	private static final double AGGRO_RANGE = 8.0D;
	private static final double BLACK_BEAR_FLEE_RANGE = 8.0D;
	private static final double POLAR_BEAR_SPEED = 0.3125D;

	private BearBehaviorSystem() {}

	public static void initialize() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof PolarBear bear && bear.getType() == EntityType.POLAR_BEAR) {
				AttributeInstance movement = bear.getAttribute(Attributes.MOVEMENT_SPEED);
				if (movement != null) movement.setBaseValue(POLAR_BEAR_SPEED);
				AttributeInstance follow = bear.getAttribute(Attributes.FOLLOW_RANGE);
				if (follow != null) follow.setBaseValue(AGGRO_RANGE);
			}
		});
		ServerTickEvents.END_WORLD_TICK.register(BearBehaviorSystem::tickWorld);
	}

	private static void tickWorld(ServerLevel level) {
		if (level.getGameTime() % 5L != 0L) return;

		Set<PolarBear> handled = new HashSet<>();
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isCreative() || player.isSpectator()) continue;

			for (BrownBear bear : level.getEntitiesOfClass(BrownBear.class, player.getBoundingBox().inflate(AGGRO_RANGE), BrownBear::isAlive)) {
				if (handled.add(bear)) forceAggro(bear, player);
			}

			for (PolarBear bear : level.getEntitiesOfClass(PolarBear.class, player.getBoundingBox().inflate(AGGRO_RANGE), candidate -> candidate.isAlive() && candidate.getType() == EntityType.POLAR_BEAR)) {
				if (handled.add(bear)) forceAggro(bear, player);
			}

			for (BlackBear bear : level.getEntitiesOfClass(BlackBear.class, player.getBoundingBox().inflate(BLACK_BEAR_FLEE_RANGE), BlackBear::isAlive)) {
				if (!handled.add(bear)) continue;
				if (bear.getTarget() != null || bear.isAngry() || hasNearbyCub(level, bear)) continue;
				fleeFrom(bear, player);
			}
		}
	}

	private static void forceAggro(PolarBear bear, ServerPlayer player) {
		if (bear.getTarget() == null || bear.distanceToSqr(player) < bear.distanceToSqr(bear.getTarget())) {
			bear.setTarget(player);
			bear.setAggressive(true);
		}
	}

	private static boolean hasNearbyCub(ServerLevel level, BlackBear bear) {
		return !level.getEntitiesOfClass(
			BlackBear.class,
			bear.getBoundingBox().inflate(8.0D),
			candidate -> candidate.isAlive() && candidate.isBaby()
		).isEmpty();
	}

	private static void fleeFrom(BlackBear bear, ServerPlayer player) {
		Vec3 away = bear.position().subtract(player.position());
		if (away.lengthSqr() < 0.0001D) away = new Vec3(1.0D, 0.0D, 0.0D);
		away = away.normalize().scale(10.0D);
		Vec3 destination = bear.position().add(away);
		bear.setTarget(null);
		bear.setAggressive(false);
		bear.getNavigation().moveTo(destination.x, bear.getY(), destination.z, 1.25D);
	}
}
