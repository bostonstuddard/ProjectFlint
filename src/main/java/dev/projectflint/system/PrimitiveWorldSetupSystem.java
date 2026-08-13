package dev.projectflint.system;

import com.mojang.datafixers.util.Pair;
import dev.projectflint.ProjectFlint;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;

import java.util.Set;

public final class PrimitiveWorldSetupSystem {
	private static final Set<ResourceKey<Biome>> STARTER_SPAWN_BIOMES = Set.of(
		Biomes.FOREST,
		Biomes.FLOWER_FOREST,
		Biomes.MEADOW
	);

	private PrimitiveWorldSetupSystem() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(PrimitiveWorldSetupSystem::configureWorld);
	}

	private static void configureWorld(MinecraftServer server) {
		server.setDifficulty(Difficulty.HARD, true);
		server.setDifficultyLocked(true);
		server.overworld().getGameRules().set(GameRules.SPAWN_PHANTOMS, false, server);

		relocateSpawnIfNeeded(server.overworld());
	}

	private static void relocateSpawnIfNeeded(ServerLevel level) {
		BlockPos currentSpawn = level.getRespawnData().pos();
		if (isStarterBiome(level.getBiome(currentSpawn))) {
			return;
		}

		Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(
			PrimitiveWorldSetupSystem::isStarterBiome,
			currentSpawn,
			8192,
			32,
			64
		);

		if (found == null) {
			ProjectFlint.LOGGER.warn("Could not find a ProjectFlint starter spawn biome within 8192 blocks; keeping vanilla spawn {}", currentSpawn);
			return;
		}

		BlockPos biomePos = found.getFirst();
		int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, biomePos.getX(), biomePos.getZ());
		BlockPos newSpawn = new BlockPos(biomePos.getX(), surfaceY, biomePos.getZ());
		level.setRespawnData(LevelData.RespawnData.of(Level.OVERWORLD, newSpawn, 0.0F, 0.0F));
		ProjectFlint.LOGGER.info("Relocated ProjectFlint world spawn from {} to {}", currentSpawn, newSpawn);
	}

	private static boolean isStarterBiome(Holder<Biome> biome) {
		return STARTER_SPAWN_BIOMES.stream().anyMatch(biome::is);
	}
}
