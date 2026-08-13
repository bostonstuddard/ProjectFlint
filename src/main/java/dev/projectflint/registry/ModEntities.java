package dev.projectflint.registry;

import dev.projectflint.ProjectFlint;
import dev.projectflint.entity.BlackBear;
import dev.projectflint.entity.BrownBear;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ModEntities {
	public static final EntityType<BlackBear> BLACK_BEAR = register(
		"black_bear",
		EntityType.Builder.<BlackBear>of(BlackBear::new, MobCategory.CREATURE)
			.sized(1.4F, 1.4F)
			.clientTrackingRange(10)
	);

	public static final EntityType<BrownBear> BROWN_BEAR = register(
		"brown_bear",
		EntityType.Builder.<BrownBear>of(BrownBear::new, MobCategory.CREATURE)
			.sized(1.8F, 1.8F)
			.clientTrackingRange(10)
	);

	private ModEntities() {}

	public static void initialize() {
		FabricDefaultAttributeRegistry.register(BLACK_BEAR, BlackBear.createBlackBearAttributes());
		FabricDefaultAttributeRegistry.register(BROWN_BEAR, BrownBear.createBrownBearAttributes());

		SpawnPlacements.register(BLACK_BEAR, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
		SpawnPlacements.register(BROWN_BEAR, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);

		BiomeModifications.addSpawn(
			BiomeSelectors.includeByKey(
				Biomes.FOREST,
				Biomes.FLOWER_FOREST,
				Biomes.BIRCH_FOREST,
				Biomes.OLD_GROWTH_BIRCH_FOREST,
				Biomes.DARK_FOREST,
				Biomes.TAIGA,
				Biomes.MEADOW,
				Biomes.WINDSWEPT_FOREST
			),
			MobCategory.CREATURE,
			BLACK_BEAR,
			6,
			1,
			2
		);

		BiomeModifications.addSpawn(
			BiomeSelectors.includeByKey(
				Biomes.OLD_GROWTH_PINE_TAIGA,
				Biomes.OLD_GROWTH_SPRUCE_TAIGA,
				Biomes.WINDSWEPT_HILLS,
				Biomes.WINDSWEPT_GRAVELLY_HILLS,
				Biomes.GROVE,
				Biomes.STONY_PEAKS,
				Biomes.JAGGED_PEAKS
			),
			MobCategory.CREATURE,
			BROWN_BEAR,
			4,
			1,
			2
		);
	}

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ProjectFlint.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}
}
