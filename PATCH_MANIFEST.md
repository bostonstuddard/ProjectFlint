# Project Flint v0.31.64-b — Patch Manifest

Base: v0.31.63-b. Target: v0.31.64-b. Minecraft 1.21.11, Fabric, Mojang mappings.

**Apply:** Extract this ZIP at the ProjectFlint source root. The archive has no nested project folder. Run `build.bat`; it invokes `APPLY_PROJECTFLINT_0_31_64_B.bat`, and successful output goes in `dist/`.

## Changes
- Restore early personal 2×2 Wooden Axe/Pickaxe/Shovel recipes and add Wooden Sword/Hoe alternatives.
- Restore Oak Wooden Sheet/Board personal recipes and eliminate duplicated Hardwood-only Oak entries; retain universal Hardwood recipe variants.
- Add Flint Axe to `plank_cutting_axes`, fixing the Wooden Sheet -> Wooden Axe prerequisite loop; update personal recipe reference.
- Allow Tanned Utility Vest hand-clearing for vanilla Bush, Firefly Bush and dry grass without stone/ore permissions.
- Guarded Charcoal auto-relighting for fully valid Fireclay Forge processing only; retain manual Log-to-Charcoal ignition.
- Update G and K in-game guides, docs, release notes, decision record and validation.

## Validation
- Run `python tools/validate_03164.py` after applying. The release was validated with 1,764 static checks including all resource JSON and key early progression permissions.
- Verified `Blocks.BUSH`, `Blocks.FIREFLY_BUSH`, `Blocks.SHORT_DRY_GRASS`, `Blocks.TALL_DRY_GRASS` against the uploaded Minecraft 1.21.11 mapped JAR.
- Full Gradle build and in-game playtest **not performed in this environment**. No guarantee of runtime verification is claimed.

## Root-relative files in this patch
- `APPLY_PROJECTFLINT_0_31_64_B.bat`
- `CHANGELOG.md`
- `PATCH_MANIFEST.md`
- `README.md`
- `build.bat`
- `docs/ARCHITECTURE.md`
- `docs/CURRENT_SYSTEMS.md`
- `docs/PRIMITIVE_PROGRESSION_MATRIX.md`
- `docs/RECIPE_GUIDE_RELEASE_CHECKLIST.md`
- `docs/RELEASE_PROCESS.md`
- `docs/ROADMAP.md`
- `docs/decisions/0023-restore-personal-wood-tools-avoid-duplicate-hardwood-recipes-and-guard-forge.md`
- `docs/releases/0.31.64-b.md`
- `gradle.properties`
- `src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java`
- `src/main/java/dev/projectflint/mixin/RecipeManagerMixin.java`
- `src/main/java/dev/projectflint/recipe/PersonalCraftingGuideRegistry.java`
- `src/main/java/dev/projectflint/system/TannedUtilityVestSystem.java`
- `src/main/resources/data/projectflint/recipe/oak_board_from_plank.json`
- `src/main/resources/data/projectflint/recipe/oak_sheet_from_plank.json`
- `src/main/resources/data/projectflint/recipe/wooden_hoe_personal.json`
- `src/main/resources/data/projectflint/recipe/wooden_sword_personal.json`
- `src/main/resources/data/projectflint/tags/item/plank_cutting_axes.json`
- `tools/CURRENT_PERSONAL_CRAFTING_GATES.md`
- `tools/validate_03164.py`
