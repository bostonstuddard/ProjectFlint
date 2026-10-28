package dev.projectflint.system;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** One independently rolled 5% pack encounter per player per night, beginning on night six. */
public final class NightWolfPackSystem {
	private static final Map<UUID, Long> LAST_NIGHT_CHECKED = new HashMap<>();
	private static final float NIGHTLY_CHANCE = 0.05F;
	private NightWolfPackSystem() {}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> LAST_NIGHT_CHECKED.clear());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> LAST_NIGHT_CHECKED.clear());
		ServerTickEvents.END_WORLD_TICK.register(NightWolfPackSystem::tick);
	}

	private static void tick(ServerLevel level) {
		if (!level.dimension().equals(Level.OVERWORLD) || level.getGameTime() % 20L != 0L) return;
		long time = level.getDayTime();
		long day = Math.floorDiv(time, 24000L);
		long tickOfDay = Math.floorMod(time, 24000L);
		if (day < 5L || tickOfDay < 13000L || tickOfDay >= 23000L) return;
		for (ServerPlayer player : level.players()) {
			if (!player.isAlive() || player.isCreative() || player.isSpectator()) continue;
			UUID uuid = player.getUUID();
			if (LAST_NIGHT_CHECKED.getOrDefault(uuid, -1L) == day) continue;
			LAST_NIGHT_CHECKED.put(uuid, day);
			if (level.random.nextFloat() < NIGHTLY_CHANCE) spawnPack(level, player);
		}
	}

	private static void spawnPack(ServerLevel level, ServerPlayer player) {
		int size = 3 + level.random.nextInt(3); // three to five, one shared spawn cluster.
		double angle = level.random.nextDouble() * Math.PI * 2.0D;
		int distance = 30 + level.random.nextInt(16);
		int originX = player.blockPosition().getX() + (int) Math.round(Math.cos(angle) * distance);
		int originZ = player.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * distance);
		for (int i = 0; i < size; i++) {
			boolean placed = false;
			for (int attempt = 0; attempt < 10 && !placed; attempt++) {
				int x = originX + level.random.nextInt(7) - 3;
				int z = originZ + level.random.nextInt(7) - 3;
				if (!level.hasChunk(x >> 4, z >> 4)) continue;
				BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
				if (!level.hasChunkAt(pos) || !level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())) continue;
				if (!level.canSeeSky(pos) || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) continue;
				if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D) < 30.0D * 30.0D
					|| player.distanceToSqr(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D) > 45.0D * 45.0D) continue;
				Wolf wolf = EntityType.WOLF.create(level, EntitySpawnReason.EVENT);
				if (wolf == null) return;
				wolf.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
				wolf.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
				placed = level.addFreshEntity(wolf);
			}
		}
	}
}
