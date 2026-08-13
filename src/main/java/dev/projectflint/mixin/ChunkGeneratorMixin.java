package dev.projectflint.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
	private static final Set<String> PROJECTFLINT$REMOVED_SURFACE_STRUCTURES = Set.of(
		"pillager_outpost",
		"desert_pyramid",
		"jungle_pyramid",
		"swamp_hut",
		"igloo",
		"woodland_mansion",
		"shipwreck",
		"shipwreck_beached",
		"ocean_ruin_cold",
		"ocean_ruin_warm",
		"monument",
		"buried_treasure",
		"trail_ruins"
	);

	@Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
	private void projectflint$removeVanillaSurfaceStructures(
		StructureSet.StructureSelectionEntry entry,
		StructureManager structureManager,
		RegistryAccess registryAccess,
		RandomState randomState,
		StructureTemplateManager structureTemplateManager,
		long seed,
		ChunkAccess chunk,
		ChunkPos chunkPos,
		SectionPos sectionPos,
		ResourceKey<Level> dimension,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (!dimension.equals(Level.OVERWORLD)) {
			return;
		}

		String path = entry.structure().unwrapKey()
			.map(key -> key.identifier().getPath())
			.orElse("");

		if (path.startsWith("village_")
			|| path.startsWith("ruined_portal")
			|| PROJECTFLINT$REMOVED_SURFACE_STRUCTURES.contains(path)) {
			cir.setReturnValue(false);
		}
	}
}
