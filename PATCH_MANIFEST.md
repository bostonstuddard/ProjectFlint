# Project Flint v0.31.65-b — patch manifest

**Base release:** v0.31.64-b. **Target release:** v0.31.65-b. **Target game:** Minecraft 1.21.11 Fabric.

Extract this archive directly into the Project Flint source root, overwriting modified files. `build.bat` calls `APPLY_PROJECTFLINT_0_31_65_B.bat` before building and puts a successfully built mod JAR in `dist/`. No full project directory is nested in the archive.

## Important gameplay changes

- Fireclay Forge resumes eligible Log-to-Charcoal batches automatically, but never burns Charcoal without a supported material input and available output space.
- Wooden Grinder recipes: 8 Mud Piles for Loose Mud Bricks; 2 Loose Mud Bricks for Bricks.
- Hardwood-only mud, earth-shape and masonry crafting additions; three-second recipe output gate unchanged.
- New Dirt Slabs/Stairs registered with blockstates/models/loot/item definitions and tool permissions.
- Packed Mud walking speed factor 1.10 and in-game tooltip.
- Personal crafting yields 2 Leather Straps, 1 Repair Kit and a 4-Clay-Ball Clay Block; Hardwood Table yields 3 Straps, 2 Kits and a 3-Clay-Ball Clay Block.

## Validation status

`tools/validate_03165.py` performs static checks (recipe outputs, Java source assertions and JSON parsing). Full Gradle compilation and in-game survival tests are not included in these checks and are **not** claimed as completed.

## Changed/new files at the archive root

- [NEW] `APPLY_PROJECTFLINT_0_31_65_B.bat`
- [MODIFIED] `CHANGELOG.md`
- [MODIFIED] `PATCH_MANIFEST.md`
- [MODIFIED] `README.md`
- [MODIFIED] `build.bat`
- [MODIFIED] `docs/ARCHITECTURE.md`
- [MODIFIED] `docs/CURRENT_SYSTEMS.md`
- [MODIFIED] `docs/PRIMITIVE_PROGRESSION_MATRIX.md`
- [MODIFIED] `docs/RECIPE_GUIDE_RELEASE_CHECKLIST.md`
- [MODIFIED] `docs/RELEASE_PROCESS.md`
- [MODIFIED] `docs/ROADMAP.md`
- [NEW] `docs/decisions/0024-forge-wood-autorelight-and-mud-crafting-economy.md`
- [NEW] `docs/releases/0.31.65-b.md`
- [MODIFIED] `gradle.properties`
- [MODIFIED] `src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java`
- [MODIFIED] `src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java`
- [MODIFIED] `src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java`
- [MODIFIED] `src/main/java/dev/projectflint/mixin/ItemTooltipMixin.java`
- [MODIFIED] `src/main/java/dev/projectflint/recipe/HardwoodCraftingGuideRegistry.java`
- [MODIFIED] `src/main/java/dev/projectflint/recipe/PersonalCraftingGuideRegistry.java`
- [MODIFIED] `src/main/java/dev/projectflint/recipe/WoodenGrinderRecipeRegistry.java`
- [MODIFIED] `src/main/java/dev/projectflint/registry/ModBlocks.java`
- [MODIFIED] `src/main/java/dev/projectflint/registry/ModCreativeTabs.java`
- [MODIFIED] `src/main/java/dev/projectflint/registry/ModItems.java`
- [MODIFIED] `src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java`
- [MODIFIED] `src/main/java/dev/projectflint/system/WoodenToolTargets.java`
- [NEW] `src/main/resources/assets/projectflint/blockstates/dirt_slab.json`
- [NEW] `src/main/resources/assets/projectflint/blockstates/dirt_stairs.json`
- [NEW] `src/main/resources/assets/projectflint/items/dirt_slab.json`
- [NEW] `src/main/resources/assets/projectflint/items/dirt_stairs.json`
- [MODIFIED] `src/main/resources/assets/projectflint/lang/en_us.json`
- [NEW] `src/main/resources/assets/projectflint/models/block/dirt_slab.json`
- [NEW] `src/main/resources/assets/projectflint/models/block/dirt_slab_top.json`
- [NEW] `src/main/resources/assets/projectflint/models/block/dirt_stairs.json`
- [NEW] `src/main/resources/assets/projectflint/models/block/dirt_stairs_inner.json`
- [NEW] `src/main/resources/assets/projectflint/models/block/dirt_stairs_outer.json`
- [NEW] `src/main/resources/assets/projectflint/models/item/dirt_slab.json`
- [NEW] `src/main/resources/assets/projectflint/models/item/dirt_stairs.json`
- [NEW] `src/main/resources/data/projectflint/loot_table/blocks/dirt_slab.json`
- [NEW] `src/main/resources/data/projectflint/loot_table/blocks/dirt_stairs.json`
- [MODIFIED] `src/main/resources/data/projectflint/recipe/hardwood_clay_block.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_dirt_from_loose_soil.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_dirt_slabs.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_dirt_stairs.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_loose_dirt_slabs.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_loose_mud_brick_slabs.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_loose_mud_bricks.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_loose_soil_from_dirt.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_mud_brick_slabs.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_mud_brick_stairs.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_mud_brick_walls.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_mud_bricks.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_mud_from_dirt.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_mud_from_dirt_vanilla_bucket.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_mud_piles_from_mud.json`
- [NEW] `src/main/resources/data/projectflint/recipe/hardwood_packed_mud.json`
- [MODIFIED] `src/main/resources/data/projectflint/recipe/leather_repair_kit.json`
- [MODIFIED] `src/main/resources/data/projectflint/recipe/leather_straps_from_sheet.json`
- [NEW] `tools/validate_03165.py`
