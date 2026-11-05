# Project Flint v0.31.55-b — Patch Manifest

**Base:** Project Flint v0.31.54-b (Minecraft 1.21.11, Fabric, Mojang mappings, Java 21)
**Distribution:** changed/new source and resources at ZIP root; no nested project folder, Gradle cache, build outputs or generated JARs.
**Apply:** extract over the existing v0.31.54-b source tree, overwrite changed files and run `build.bat`. The updated build calls `APPLY_PROJECTFLINT_0_31_55_B.bat` before compilation.

## Implemented

- Locked all Deepslate-family blocks and Ores across survival mining progress and server drops; Hammer drops one Raw Iron/Copper from regular stone ore and two Coal from Coal Ore.
- Restricted bare-hand recovery to a reviewed safe whitelist: vanilla/Flint/Fat torches, Chicken Nest (with contents returned), Axle; no structural stone, fireclay or furnace shortcuts and no Utility Vest miner override.
- Introduced Claustrophobia below Y=-40 and stacking Speluncaphobia below Y=0, with direct daytime sun and block light >=13 exemptions. Speluncaphobia's added exhaustion stops when food falls to 6, and its debuff causes no direct damage.
- Added Hardwood Sticks to Creative and Hardwood tool recipes; added craftable axis-oriented placeable Axle with model, blockstate, loot, and item definition.
- Added alternative Basin recipe, probabilistic Raw Iron/Copper, stone, gravel, sand and slag washing with independently rolled byproducts, and 12 user-art-derived dust/fertilizer materials.
- Added Cobblestone→Stone in Brick Furnace, Cobblestone/Andesite/Granite/Diorite slabs and stairs via Hardwood Crafting Table, restored original early Hinged Doors, and separated horizontal 2-plank Slabs from vertical Sticks.
- Updated G Field Guide, K Recipe Guide (including byproduct-specific entries and probabilities), `CURRENT_SYSTEMS`, Basin yield documentation, architecture/progression/roadmap, release process/checklist, texture provenance, ADRs 0012–0014, README and changelog.

## Verification

- `python tools/validate_0_31_55_b.py`: **149 targeted checks passed**; 1,582 JSON resources successfully parsed.
- Targeted Java 21 checks earlier passed for ore gates, effects and Basin byproduct logic against local Minecraft 1.21.11 mapped classes with temporary dependency stubs.
- The historic `tools/validate_release.py` is retained and version-bumped but its legacy requirements were not fully satisfied in this *reconstructed partial source tree*: it expects deleted prior APPLY scripts and exact old Basin state counts. This does not imply a complete Gradle build.
- **Full Gradle build and Minecraft runtime test: not verified.** Validate on the user's local dev environment before deployment.

## Contents

- `APPLY_PROJECTFLINT_0_31_55_B.bat`
- `CHANGELOG.md`
- `README.md`
- `build.bat`
- `docs/ARCHITECTURE.md`
- `docs/ASSET_SOURCES.md`
- `docs/BASIN_MATERIAL_YIELDS.md`
- `docs/CURRENT_SYSTEMS.md`
- `docs/PRIMITIVE_PROGRESSION_MATRIX.md`
- `docs/RECIPE_GUIDE_RELEASE_CHECKLIST.md`
- `docs/RELEASE_PROCESS.md`
- `docs/ROADMAP.md`
- `docs/TEXTURE_PROVENANCE.md`
- `docs/decisions/0012-small-decoration-recovery-without-vest-mining-bypass.md`
- `docs/decisions/0013-stone-ore-gates-and-probabilistic-basin.md`
- `docs/decisions/0014-depth-fear-light-exemptions.md`
- `docs/releases/0.31.55-b.md`
- `gradle.properties`
- `src/main/java/dev/projectflint/block/AxleBlock.java`
- `src/main/java/dev/projectflint/block/BasinBlock.java`
- `src/main/java/dev/projectflint/block/entity/BasinBlockEntity.java`
- `src/main/java/dev/projectflint/block/entity/BrickFurnaceBlockEntity.java`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java`
- `src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java`
- `src/main/java/dev/projectflint/mixin/FoodDataMixin.java`
- `src/main/java/dev/projectflint/mixin/ItemTooltipMixin.java`
- `src/main/java/dev/projectflint/mixin/RecipeManagerMixin.java`
- `src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java`
- `src/main/java/dev/projectflint/recipe/BrickFurnaceRecipeRegistry.java`
- `src/main/java/dev/projectflint/registry/ModBlocks.java`
- `src/main/java/dev/projectflint/registry/ModCreativeTabs.java`
- `src/main/java/dev/projectflint/registry/ModEffects.java`
- `src/main/java/dev/projectflint/registry/ModItems.java`
- `src/main/java/dev/projectflint/system/BasinMaterialProcessingSystem.java`
- `src/main/java/dev/projectflint/system/OreMiningRules.java`
- `src/main/java/dev/projectflint/system/PlayerPlacedBlockLedger.java`
- `src/main/java/dev/projectflint/system/PrimitiveEnvironmentEffectSystem.java`
- `src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java`
- `src/main/resources/assets/projectflint/blockstates/axle.json`
- `src/main/resources/assets/projectflint/blockstates/basin.json`
- `src/main/resources/assets/projectflint/items/axle.json`
- `src/main/resources/assets/projectflint/items/copper_dust.json`
- `src/main/resources/assets/projectflint/items/fertilizer.json`
- `src/main/resources/assets/projectflint/items/gold_dust.json`
- `src/main/resources/assets/projectflint/items/iron_dust.json`
- `src/main/resources/assets/projectflint/items/mica_dust.json`
- `src/main/resources/assets/projectflint/items/nickel_dust.json`
- `src/main/resources/assets/projectflint/items/potassium_dust.json`
- `src/main/resources/assets/projectflint/items/quartz_dust.json`
- `src/main/resources/assets/projectflint/items/silicon_dust.json`
- `src/main/resources/assets/projectflint/items/silver_dust.json`
- `src/main/resources/assets/projectflint/items/slag.json`
- `src/main/resources/assets/projectflint/items/stone_dust.json`
- `src/main/resources/assets/projectflint/lang/en_us.json`
- `src/main/resources/assets/projectflint/models/block/axle.json`
- `src/main/resources/assets/projectflint/models/item/axle.json`
- `src/main/resources/assets/projectflint/models/item/copper_dust.json`
- `src/main/resources/assets/projectflint/models/item/fertilizer.json`
- `src/main/resources/assets/projectflint/models/item/gold_dust.json`
- `src/main/resources/assets/projectflint/models/item/hardwood_sticks.json`
- `src/main/resources/assets/projectflint/models/item/iron_dust.json`
- `src/main/resources/assets/projectflint/models/item/mica_dust.json`
- `src/main/resources/assets/projectflint/models/item/nickel_dust.json`
- `src/main/resources/assets/projectflint/models/item/potassium_dust.json`
- `src/main/resources/assets/projectflint/models/item/quartz_dust.json`
- `src/main/resources/assets/projectflint/models/item/silicon_dust.json`
- `src/main/resources/assets/projectflint/models/item/silver_dust.json`
- `src/main/resources/assets/projectflint/models/item/slag.json`
- `src/main/resources/assets/projectflint/models/item/stone_dust.json`
- `src/main/resources/assets/projectflint/textures/item/copper_dust.png`
- `src/main/resources/assets/projectflint/textures/item/fertilizer.png`
- `src/main/resources/assets/projectflint/textures/item/gold_dust.png`
- `src/main/resources/assets/projectflint/textures/item/hardwood_sticks.png`
- `src/main/resources/assets/projectflint/textures/item/iron_dust.png`
- `src/main/resources/assets/projectflint/textures/item/mica_dust.png`
- `src/main/resources/assets/projectflint/textures/item/nickel_dust.png`
- `src/main/resources/assets/projectflint/textures/item/potassium_dust.png`
- `src/main/resources/assets/projectflint/textures/item/quartz_dust.png`
- `src/main/resources/assets/projectflint/textures/item/silicon_dust.png`
- `src/main/resources/assets/projectflint/textures/item/silver_dust.png`
- `src/main/resources/assets/projectflint/textures/item/slag.png`
- `src/main/resources/assets/projectflint/textures/item/stone_dust.png`
- `src/main/resources/data/projectflint/loot_table/blocks/axle.json`
- `src/main/resources/data/projectflint/recipe/axle.json`
- `src/main/resources/data/projectflint/recipe/basin_alternative.json`
- `src/main/resources/data/projectflint/recipe/hardwood_acacia_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_andesite_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_andesite_stairs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_axe.json`
- `src/main/resources/data/projectflint/recipe/hardwood_bamboo_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_birch_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_cherry_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_cobblestone_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_cobblestone_stairs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_crimson_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_dark_oak_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_diorite_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_diorite_stairs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_granite_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_granite_stairs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_hammer.json`
- `src/main/resources/data/projectflint/recipe/hardwood_hoe.json`
- `src/main/resources/data/projectflint/recipe/hardwood_jungle_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_mangrove_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_oak_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_pale_oak_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_pickaxe.json`
- `src/main/resources/data/projectflint/recipe/hardwood_shovel.json`
- `src/main/resources/data/projectflint/recipe/hardwood_spruce_slabs.json`
- `src/main/resources/data/projectflint/recipe/hardwood_sword.json`
- `src/main/resources/data/projectflint/recipe/hardwood_warped_slabs.json`
- `tools/validate_0_31_55_b.py`
- `tools/validate_release.py`
- `PATCH_MANIFEST.md`
