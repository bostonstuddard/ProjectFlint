# Project Flint v0.31.56-b — Patch Manifest

**Base:** v0.31.55-b Project Flint source, Minecraft 1.21.11 Fabric, Mojang mappings, Java 21.

**Output:** patch-only ZIP of changed/new source and release docs, placed at archive root (no nested ProjectFlint directory). **Files:** 25 including this manifest. The update must be extracted directly over the previous source.

## Functional changes

- Basin: real ItemStack block-entity rendering; synchronized inventory/partial stomp state; 2–5 stomp thresholds per input; 1 hunger point charged when the input completes; original output chances unchanged.
- Hardwood Table: correct return/durability for Bone Knife, Flint File and other reusable inputs in separate hardwood recipe types. Preserve the 60-tick crafting timer and ordinary item consumption.
- Guides, current-state docs, architecture, ADR 0015, release notes and cleanup/build scripts updated.

## Verification

`python tools/validate_03156.py` passes source/JSON consistency checks. Mapped 1.21.11 API signatures for block-entity renderer and crafting result slot inspected. Offline Gradle compile attempted but blocked because Fabric Loom 1.14.10 plugin is not present in the local cache. In-game multiplayer/singleplayer tests pending.

## File list

- `APPLY_PROJECTFLINT_0_31_56_B.bat`
- `CHANGELOG.md`
- `README.md`
- `build.bat`
- `gradle.properties`
- `docs/ARCHITECTURE.md`
- `docs/BASIN_MATERIAL_YIELDS.md`
- `docs/CURRENT_SYSTEMS.md`
- `docs/PRIMITIVE_PROGRESSION_MATRIX.md`
- `docs/RECIPE_GUIDE_RELEASE_CHECKLIST.md`
- `docs/RELEASE_PROCESS.md`
- `docs/ROADMAP.md`
- `docs/decisions/0015-physical-basin-and-durable-remainders.md`
- `docs/releases/0.31.56-b.md`
- `src/main/java/dev/projectflint/block/BasinBlock.java`
- `src/main/java/dev/projectflint/block/entity/BasinBlockEntity.java`
- `src/main/java/dev/projectflint/client/BasinItemRenderer.java`
- `src/main/java/dev/projectflint/client/ProjectFlintClient.java`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java`
- `src/main/java/dev/projectflint/menu/HardwoodCraftingMenu.java`
- `src/main/java/dev/projectflint/system/BasinMaterialProcessingSystem.java`
- `src/main/resources/assets/projectflint/blockstates/basin.json`
- `tools/validate_03156.py`
- `PATCH_MANIFEST.md`
