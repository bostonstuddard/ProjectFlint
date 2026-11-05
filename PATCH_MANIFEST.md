# Project Flint v0.31.57-b — Patch Manifest

**Base:** apply on top of Project Flint v0.31.56-b.  
**Format:** patch-only, no enclosing project directory and no compiled JAR.  
**Minecraft:** 1.21.11, Fabric Loader 0.19.3, Java 21.  
**Update:** per-stomp Basin hunger and 2–3 stomp cap; physical Basin item model transforms; Basin ore/clay/rare-earth yields; 10 dusts and insulated glass; Hardwood recipe expansion; bootstrap table craft; new Forge recipe; G/K guide and architectural documentation synchronization.

**Release checks:** `python tools/validate_03157.py` (stdlib only); full Gradle compilation attempted but blocked by unavailable offline Fabric Loom 1.14.10 plugin. No claim of client or server gameplay testing.

**Installation:** extract every file here directly into your existing source tree, replacing matching paths. Use the new `APPLY_PROJECTFLINT_0_31_57_B.bat` cleanup script and `build.bat` for packaging on Windows.

**New small chance defaults:** Iron Gold Dust 3%; Copper Slag 5% (user supplied "small" but did not specify exact rates).

## Modified/new files

- `APPLY_PROJECTFLINT_0_31_57_B.bat`
- `CHANGELOG.md`
- `README.md`
- `build.bat`
- `docs/ARCHITECTURE.md`
- `docs/BASIN_MATERIAL_YIELDS.md`
- `docs/CURRENT_SYSTEMS.md`
- `docs/PRIMITIVE_PROGRESSION_MATRIX.md`
- `docs/RECIPE_GUIDE_RELEASE_CHECKLIST.md`
- `docs/RELEASE_PROCESS.md`
- `docs/ROADMAP.md`
- `docs/TEMPORARY_ASSETS.md`
- `docs/decisions/0016-basin-stomp-economy-and-mica-glass-chain.md`
- `docs/releases/0.31.57-b.md`
- `gradle.properties`
- `src/main/java/dev/projectflint/block/entity/BasinBlockEntity.java`
- `src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java`
- `src/main/java/dev/projectflint/recipe/HardwoodCraftingGuideRegistry.java`
- `src/main/java/dev/projectflint/recipe/PersonalCraftingGuideRegistry.java`
- `src/main/java/dev/projectflint/registry/ModCreativeTabs.java`
- `src/main/java/dev/projectflint/registry/ModItems.java`
- `src/main/java/dev/projectflint/system/BasinMaterialProcessingSystem.java`
- `src/main/resources/assets/projectflint/items/aluminum_dust.json`
- `src/main/resources/assets/projectflint/items/borosilicate_dust.json`
- `src/main/resources/assets/projectflint/items/bsig_dust.json`
- `src/main/resources/assets/projectflint/items/bssig_dust.json`
- `src/main/resources/assets/projectflint/items/gallium_dust.json`
- `src/main/resources/assets/projectflint/items/glass_dust.json`
- `src/main/resources/assets/projectflint/items/lithium_dust.json`
- `src/main/resources/assets/projectflint/items/mbssig_dust.json`
- `src/main/resources/assets/projectflint/items/pure_mica_insulated_glass.json`
- `src/main/resources/assets/projectflint/items/rare_earth_dust.json`
- `src/main/resources/assets/projectflint/items/tin_dust.json`
- `src/main/resources/assets/projectflint/lang/en_us.json`
- `src/main/resources/assets/projectflint/models/item/aluminum_dust.json`
- `src/main/resources/assets/projectflint/models/item/basin.json`
- `src/main/resources/assets/projectflint/models/item/borosilicate_dust.json`
- `src/main/resources/assets/projectflint/models/item/bsig_dust.json`
- `src/main/resources/assets/projectflint/models/item/bssig_dust.json`
- `src/main/resources/assets/projectflint/models/item/gallium_dust.json`
- `src/main/resources/assets/projectflint/models/item/glass_dust.json`
- `src/main/resources/assets/projectflint/models/item/lithium_dust.json`
- `src/main/resources/assets/projectflint/models/item/mbssig_dust.json`
- `src/main/resources/assets/projectflint/models/item/pure_mica_insulated_glass.json`
- `src/main/resources/assets/projectflint/models/item/rare_earth_dust.json`
- `src/main/resources/assets/projectflint/models/item/tin_dust.json`
- `src/main/resources/assets/projectflint/textures/item/aluminum_dust.png`
- `src/main/resources/assets/projectflint/textures/item/borosilicate_dust.png`
- `src/main/resources/assets/projectflint/textures/item/bsig_dust.png`
- `src/main/resources/assets/projectflint/textures/item/bssig_dust.png`
- `src/main/resources/assets/projectflint/textures/item/gallium_dust.png`
- `src/main/resources/assets/projectflint/textures/item/glass_dust.png`
- `src/main/resources/assets/projectflint/textures/item/lithium_dust.png`
- `src/main/resources/assets/projectflint/textures/item/mbssig_dust.png`
- `src/main/resources/assets/projectflint/textures/item/rare_earth_dust.png`
- `src/main/resources/assets/projectflint/textures/item/tin_dust.png`
- `src/main/resources/data/projectflint/recipe/hardwood_advanced_flint_multitool.json`
- `src/main/resources/data/projectflint/recipe/hardwood_aluminum_dust_from_rare_earth.json`
- `src/main/resources/data/projectflint/recipe/hardwood_boards_any_planks.json`
- `src/main/resources/data/projectflint/recipe/hardwood_bone_club.json`
- `src/main/resources/data/projectflint/recipe/hardwood_bone_hoe.json`
- `src/main/resources/data/projectflint/recipe/hardwood_borosilicate_dust.json`
- `src/main/resources/data/projectflint/recipe/hardwood_bound_wood_processor.json`
- `src/main/resources/data/projectflint/recipe/hardwood_brick_shingles.json`
- `src/main/resources/data/projectflint/recipe/hardwood_bsig_dust.json`
- `src/main/resources/data/projectflint/recipe/hardwood_bssig_dust.json`
- `src/main/resources/data/projectflint/recipe/hardwood_clay_block.json`
- `src/main/resources/data/projectflint/recipe/hardwood_crafting_table.json`
- `src/main/resources/data/projectflint/recipe/hardwood_fat_burning_torches.json`
- `src/main/resources/data/projectflint/recipe/hardwood_flint_axe.json`
- `src/main/resources/data/projectflint/recipe/hardwood_flint_plant_cutters.json`
- `src/main/resources/data/projectflint/recipe/hardwood_flint_torch_holder.json`
- `src/main/resources/data/projectflint/recipe/hardwood_glass_dust.json`
- `src/main/resources/data/projectflint/recipe/hardwood_leather_repair_kits.json`
- `src/main/resources/data/projectflint/recipe/hardwood_leather_sheets.json`
- `src/main/resources/data/projectflint/recipe/hardwood_leather_straps.json`
- `src/main/resources/data/projectflint/recipe/hardwood_mbssig_dust.json`
- `src/main/resources/data/projectflint/recipe/hardwood_sheets_any_planks.json`
- `src/main/resources/data/projectflint/recipe/hardwood_tanned_leather.json`
- `src/main/resources/data/projectflint/recipe/hardwood_torch_holder.json`
- `src/main/resources/data/projectflint/recipe/hardwood_unlit_flint_torch.json`
- `src/main/resources/data/projectflint/recipe/hardwood_wattle_daub_floor_tiles.json`
- `src/main/resources/data/projectflint/recipe/hardwood_wattle_daub_windows.json`
- `src/main/resources/data/projectflint/recipe/hardwood_woven_fans.json`
- `src/main/resources/data/projectflint/recipe/leather_repair_kit.json`
- `src/main/resources/data/projectflint/recipe/leather_straps_from_sheet.json`
- `tools/validate_03157.py`
