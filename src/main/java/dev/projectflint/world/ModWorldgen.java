package dev.projectflint.world;

import dev.projectflint.ProjectFlint;
import dev.projectflint.registry.ModBlocks;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class ModWorldgen {
	private ModWorldgen() {
	}

	private static ResourceKey<PlacedFeature> placed(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, ProjectFlint.id(name));
	}

	private static ResourceKey<PlacedFeature> vanillaPlaced(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", name));
	}


	public static void initialize() {

		// Aquifers are virtual and seed-derived; no groundwater blocks/features are scheduled here.

		// Active bush definitions intentionally exclude beaches/shores in alpha.2.
		BiomeModifications.addFeature(
			BiomeSelectors.foundInOverworld(),
			GenerationStep.Decoration.VEGETAL_DECORATION,
			placed("loose_rock")
		);


		// Add an extra half-strength pass anywhere vanilla already schedules Sugar Cane.
		// The matching custom placed features use twice each vanilla rarity denominator,
		// giving roughly +50% attempts without introducing cane into new biome families.
		BiomeModifications.addFeature(
			context -> context.hasPlacedFeature(vanillaPlaced("patch_sugar_cane")),
			GenerationStep.Decoration.VEGETAL_DECORATION,
			placed("extra_sugar_cane")
		);
		BiomeModifications.addFeature(
			context -> context.hasPlacedFeature(vanillaPlaced("patch_sugar_cane_swamp")),
			GenerationStep.Decoration.VEGETAL_DECORATION,
			placed("extra_sugar_cane_swamp")
		);
		BiomeModifications.addFeature(
			context -> context.hasPlacedFeature(vanillaPlaced("patch_sugar_cane_badlands")),
			GenerationStep.Decoration.VEGETAL_DECORATION,
			placed("extra_sugar_cane_badlands")
		);
		BiomeModifications.addFeature(
			context -> context.hasPlacedFeature(vanillaPlaced("patch_sugar_cane_desert")),
			GenerationStep.Decoration.VEGETAL_DECORATION,
			placed("extra_sugar_cane_desert")
		);

		for (ModBlocks.ForageBushDefinition bush : ModBlocks.FORAGE_BUSHES) {
			BiomeModifications.addFeature(
				BiomeSelectors.includeByKey(bush.biome()),
				GenerationStep.Decoration.VEGETAL_DECORATION,
				placed(bush.blockPath())
			);
		}
	}
}
