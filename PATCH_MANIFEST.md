# Project Flint v0.31.62-b — Patch Manifest

Patch-only update over the **v0.31.61-b** source folder. Copy directly to the existing project root and overwrite matching files (no nested ProjectFlint folder).

## Player-facing fix
Restores Crude Stone Hammer/early Multitool action on natural Stone: converts to Crushed Stone and drops four Rocks; one durability cost, original speed, no expansion of pickaxe or vest permissions.

## Installation
1. Copy the patch contents into the ProjectFlint source root.
2. Run `build.bat`; it runs `APPLY_PROJECTFLINT_0_31_62_B.bat` and builds without a persistent Gradle daemon.
3. Successful output: `dist/projectflint-0.31.62-b.jar`.
4. Replace the old v0.31.61-b JAR in the game mods folder; do not load both.

## Validation status
- Targeted static source/resource regression checks passed (1,681 assertions).
- `OreMiningRules.java` compiled against the 1.21.11 Mojang-mapped Minecraft JAR plus Fabric API and simple test stubs for Project Flint registries.
- **Full Gradle build, integrated Minecraft launch, client/server interaction and survival playtest were not performed.**

## Changed or new paths
- `APPLY_PROJECTFLINT_0_31_62_B.bat`
- `build.bat`
- `gradle.properties`
- `CHANGELOG.md`
- `README.md`
- `docs/ARCHITECTURE.md`
- `docs/CURRENT_SYSTEMS.md`
- `docs/PRIMITIVE_PROGRESSION_MATRIX.md`
- `docs/ROADMAP.md`
- `docs/RECIPE_GUIDE_RELEASE_CHECKLIST.md`
- `docs/RELEASE_PROCESS.md`
- `docs/decisions/0021-primitive-stone-working-exception-before-mining-gates.md`
- `docs/releases/0.31.62-b.md`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java`
- `src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java`
- `src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java`
- `src/main/java/dev/projectflint/system/OreMiningRules.java`
- `src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java`
- `tools/validate_03162.py`
- `PATCH_MANIFEST.md` (this file)

## Cleanup/removal
The incremented apply script preserves previous cleanup lineage, including deleting the obsolete personal PMI Sheet recipe. It does not remove any active early-game recipes or textures.

## Required release smoke test
Crude Stone Hammer on natural Stone -> Crushed Stone + 4 Rocks; the early Multitools also work. Verify hand/bread/Utility Vest cannot mine Stone, and that Cobblestone, ore/deepslate, Wooden/Hardwood Pickaxes and Hardwood Hammer retain their established behavior.
