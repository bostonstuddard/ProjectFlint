package dev.projectflint.system;

import dev.projectflint.block.entity.NestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.chicken.Chicken;

import java.util.UUID;

/** Server-side nest lookup used only when an adult chicken is ready to lay. */
public final class ChickenNestSystem {
	public static final int SEARCH_RADIUS = 10;

	private ChickenNestSystem() {
	}

	public static boolean tryLayEgg(ServerLevel level, Chicken chicken) {
		UUID chickenId = chicken.getUUID();
		BlockPos center = chicken.blockPosition();
		NestCandidate owned = null;
		NestCandidate free = null;
		boolean ownsNearbyNest = false;

		BlockPos min = center.offset(-SEARCH_RADIUS, -SEARCH_RADIUS, -SEARCH_RADIUS);
		BlockPos max = center.offset(SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS);
		for (BlockPos scanPos : BlockPos.betweenClosed(min, max)) {
			if (!(level.getBlockEntity(scanPos) instanceof NestBlockEntity nest)) continue;

			double distance = scanPos.distSqr(center);
			if (distance > SEARCH_RADIUS * SEARCH_RADIUS) continue;
			if (nest.isOwnedBy(chickenId)) {
				ownsNearbyNest = true;
				if (nest.canAcceptEgg() && (owned == null || distance < owned.distance())) {
					owned = new NestCandidate(scanPos.immutable(), nest, distance);
				}
			} else if (!nest.hasOwner() && nest.canAcceptEgg() && (free == null || distance < free.distance())) {
				free = new NestCandidate(scanPos.immutable(), nest, distance);
			}
		}

		NestCandidate target = owned != null ? owned : (ownsNearbyNest ? null : free);
		if (target == null || !target.nest().addEgg(chickenId)) return false;

		level.sendBlockUpdated(target.pos(), level.getBlockState(target.pos()), level.getBlockState(target.pos()), 3);
		return true;
	}

	private record NestCandidate(BlockPos pos, NestBlockEntity nest, double distance) {
	}
}
