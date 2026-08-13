package dev.projectflint.system;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Seed-deterministic virtual groundwater field shared by the creative dowsing rod
 * and future Well infrastructure. Aquifers are never represented by blocks,
 * entities, chunk attachments, or saved-data entries.
 */
public final class AquiferSystem {
	private static final int CELL_SIZE = 32;
	private static final int EDGE_PADDING = 4;
	private static final int EXISTENCE_DENOMINATOR = 7;
	private static final int MIN_DEPTH = 20;
	private static final int DEPTH_VARIANCE = 7;
	private static final long POSITION_SALT = 0x45D9F3B4A17C92E1L;
	private static final long DEPTH_SALT = 0x9E3779B97F4A7C15L;
	private static final long EXISTENCE_SALT = 0xD1B54A32D192ED03L;

	private AquiferSystem() {}

	/**
	 * Returns nearby virtual aquifers. Only candidate cells are evaluated, so this
	 * remains cheap even though no aquifer data is stored in chunks or world saves.
	 */
	public static List<AquiferHit> findNearby(ServerLevel level, BlockPos origin, int horizontalRadius, int maxResults) {
		List<AquiferHit> found = new ArrayList<>();
		int minCellX = Math.floorDiv(origin.getX() - horizontalRadius, CELL_SIZE) - 1;
		int maxCellX = Math.floorDiv(origin.getX() + horizontalRadius, CELL_SIZE) + 1;
		int minCellZ = Math.floorDiv(origin.getZ() - horizontalRadius, CELL_SIZE) - 1;
		int maxCellZ = Math.floorDiv(origin.getZ() + horizontalRadius, CELL_SIZE) + 1;
		long radiusSq = (long) horizontalRadius * horizontalRadius;

		for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
			for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
				Optional<AquiferHit> candidate = aquiferForCell(level, cellX, cellZ);
				if (candidate.isEmpty()) continue;
				AquiferHit hit = candidate.get();
				long dx = (long) hit.center().getX() - origin.getX();
				long dz = (long) hit.center().getZ() - origin.getZ();
				if (dx * dx + dz * dz <= radiusSq) found.add(hit);
			}
		}

		found.sort(Comparator.comparingLong(hit -> horizontalDistanceSq(hit.center(), origin)));
		if (found.size() > maxResults) return List.copyOf(found.subList(0, maxResults));
		return List.copyOf(found);
	}

	/**
	 * Future Well hook. A Well can tap a virtual aquifer from any Y above its
	 * representative groundwater depth. With tolerance 0, the Well must stand over
	 * one of the five virtual footprint columns; larger tolerances expand that test.
	 */
	public static Optional<AquiferHit> findBelow(ServerLevel level, BlockPos wellPos, int horizontalTolerance) {
		int search = Math.max(0, horizontalTolerance) + 1;
		int minCellX = Math.floorDiv(wellPos.getX() - search, CELL_SIZE) - 1;
		int maxCellX = Math.floorDiv(wellPos.getX() + search, CELL_SIZE) + 1;
		int minCellZ = Math.floorDiv(wellPos.getZ() - search, CELL_SIZE) - 1;
		int maxCellZ = Math.floorDiv(wellPos.getZ() + search, CELL_SIZE) + 1;

		AquiferHit nearest = null;
		long nearestDistance = Long.MAX_VALUE;
		long toleranceSq = (long) horizontalTolerance * horizontalTolerance;
		for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
			for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
				Optional<AquiferHit> candidate = aquiferForCell(level, cellX, cellZ);
				if (candidate.isEmpty()) continue;
				AquiferHit hit = candidate.get();
				if (wellPos.getY() <= hit.center().getY()) continue;
				long distance = distanceSqToFootprint(hit.center(), wellPos.getX(), wellPos.getZ());
				if (distance > toleranceSq) continue;
				if (distance < nearestDistance) {
					nearest = hit;
					nearestDistance = distance;
				}
			}
		}
		return Optional.ofNullable(nearest);
	}

	/** Returns the five virtual X/Z columns represented by an aquifer. */
	public static List<BlockPos> footprint(AquiferHit hit) {
		BlockPos c = hit.center();
		return List.of(
			c,
			c.offset(1, 0, 0),
			c.offset(-1, 0, 0),
			c.offset(0, 0, 1),
			c.offset(0, 0, -1)
		);
	}

	private static Optional<AquiferHit> aquiferForCell(ServerLevel level, int cellX, int cellZ) {
		long seed = level.getSeed();
		long existenceHash = hash(seed, cellX, cellZ, EXISTENCE_SALT);
		if (Math.floorMod(existenceHash, EXISTENCE_DENOMINATOR) != 0L) return Optional.empty();

		long positionHash = hash(seed, cellX, cellZ, POSITION_SALT);
		int usableWidth = CELL_SIZE - EDGE_PADDING * 2;
		int offsetX = EDGE_PADDING + (int) Math.floorMod(positionHash, usableWidth);
		int offsetZ = EDGE_PADDING + (int) Math.floorMod(positionHash >>> 24, usableWidth);
		int x = cellX * CELL_SIZE + offsetX;
		int z = cellZ * CELL_SIZE + offsetZ;

		int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
		long depthHash = hash(seed, cellX, cellZ, DEPTH_SALT);
		int depth = MIN_DEPTH + (int) Math.floorMod(depthHash, DEPTH_VARIANCE);
		int y = Math.max(level.getMinY() + 4, surfaceY - depth);
		BlockPos center = new BlockPos(x, y, z);
		Holder<Biome> biome = level.getBiome(new BlockPos(x, Math.max(level.getMinY(), surfaceY - 1), z));
		return Optional.of(new AquiferHit(center, yieldForBiome(biome)));
	}

	private static long hash(long seed, int cellX, int cellZ, long salt) {
		long value = seed ^ salt;
		value ^= (long) cellX * 0x632BE59BD9B4E019L;
		value ^= (long) cellZ * 0x9E3779B185EBCA87L;
		value ^= value >>> 30;
		value *= 0xBF58476D1CE4E5B9L;
		value ^= value >>> 27;
		value *= 0x94D049BB133111EBL;
		return value ^ (value >>> 31);
	}

	private static long distanceSqToFootprint(BlockPos center, int x, int z) {
		long best = Long.MAX_VALUE;
		for (BlockPos point : List.of(
			center,
			center.offset(1, 0, 0),
			center.offset(-1, 0, 0),
			center.offset(0, 0, 1),
			center.offset(0, 0, -1)
		)) {
			long dx = (long) point.getX() - x;
			long dz = (long) point.getZ() - z;
			best = Math.min(best, dx * dx + dz * dz);
		}
		return best;
	}

	private static long horizontalDistanceSq(BlockPos a, BlockPos b) {
		long dx = (long) a.getX() - b.getX();
		long dz = (long) a.getZ() - b.getZ();
		return dx * dx + dz * dz;
	}

	public static Yield yieldForBiome(Holder<Biome> biome) {
		if (biome.is(BiomeTags.IS_JUNGLE) || biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP) || biome.is(BiomeTags.IS_RIVER)) {
			return Yield.ABUNDANT;
		}
		if (biome.is(Biomes.DESERT) || biome.is(BiomeTags.IS_BADLANDS) || biome.is(BiomeTags.IS_SAVANNA)) {
			return Yield.SCARCE;
		}
		if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA)) return Yield.GOOD;
		return Yield.NORMAL;
	}

	public enum Yield {
		SCARCE(0.25F, "Scarce"),
		NORMAL(0.60F, "Normal"),
		GOOD(0.85F, "Good"),
		ABUNDANT(1.20F, "Abundant");

		private final float rateMultiplier;
		private final String label;

		Yield(float rateMultiplier, String label) {
			this.rateMultiplier = rateMultiplier;
			this.label = label;
		}

		public float rateMultiplier() { return rateMultiplier; }
		public String label() { return label; }
	}

	public record AquiferHit(BlockPos center, Yield yield) {}
}
