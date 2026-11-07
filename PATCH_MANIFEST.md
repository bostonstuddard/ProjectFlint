# Project Flint v0.31.66-b — Source Patch Manifest

**Baseline:** ProjectFlint v0.31.65-b source.
**Format:** Patch-only archive. Extract directly into the existing ProjectFlint source root.
**Changes:** Crash fix for Packed Mud Mixin; remove Flint Axe plank permission; first-sheet bridge; new stone-selective dynamite and fuse/coal/carbon chain.

## Installation

1. Back up the v0.31.65-b source. Extract the ZIP into its source root, overwriting files.
2. Run `APPLY_PROJECTFLINT_0_31_66_B.bat` (optional; `build.bat` also runs it).
3. Run `build.bat`; on success get `dist/projectflint-0.31.66-b.jar`.
4. Remove the old ProjectFlint JAR from the mods folder before installing the new one.

## Validation

`python tools/validate_03166.py` performs static source and JSON/PNG validations.
The Minecraft 1.21.11 mapped JAR was inspected to confirm the wrong Mixin target and the correct `Entity.getBlockSpeedFactor` owner.
Full Gradle compile, server and client launch, gameplay, performance, and multiplayer behavior are **not** verified in this environment.

## Modified and new files (69)

SHA-256 below is the *patch file* content checksum. The manifest itself is omitted from hash list to avoid self-hashing.

- `APPLY_PROJECTFLINT_0_31_66_B.bat` — `c429e1a7128c8667`
- `CHANGELOG.md` — `567ef62f5481c1f7`
- `README.md` — `6484bff3c2741613`
- `build.bat` — `098acf5fca89bbea`
- `docs/ARCHITECTURE.md` — `41dc5aeca2d6f94c`
- `docs/ASSET_SOURCES.md` — `fa8b42205979b7ba`
- `docs/CURRENT_SYSTEMS.md` — `197f3666dbce42ae`
- `docs/PRIMITIVE_PROGRESSION_MATRIX.md` — `58469e21d53141ee`
- `docs/RELEASE_PROCESS.md` — `863c1cf45407007b`
- `docs/ROADMAP.md` — `0c414d888a246459`
- `docs/decisions/0025-packed-mud-mixin-and-selective-dynamite-processing.md` — `7c3cdbdd7b8ba246`
- `docs/releases/0.31.66-b.md` — `6e3f86c44698286c`
- `gradle.properties` — `1f1d888f063e2665`
- `src/main/java/dev/projectflint/block/DynamiteChargeBlock.java` — `096a37fc0c691a07`
- `src/main/java/dev/projectflint/block/entity/BrickFurnaceBlockEntity.java` — `3eb6a15685a0e7a6`
- `src/main/java/dev/projectflint/block/entity/DynamiteChargeBlockEntity.java` — `60d3aa62833831cf`
- `src/main/java/dev/projectflint/block/entity/FireclayForgeBlockEntity.java` — `97b4b000a484b5e5`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java` — `9fb1a8bd01f47e52`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintRecipeGuideScreen.java` — `b456757b92453372`
- `src/main/java/dev/projectflint/item/DynamiteItem.java` — `6d7b4cd707dd7ad9`
- `src/main/java/dev/projectflint/mixin/BlockStateBaseMixin.java` — `a99e61a430caa985`
- `src/main/java/dev/projectflint/mixin/PackedMudMovementMixin.java` — `5f2db7165ac436f8`
- `src/main/java/dev/projectflint/recipe/BrickFurnaceRecipeRegistry.java` — `185385969ea90fc2`
- `src/main/java/dev/projectflint/recipe/HardwoodCraftingGuideRegistry.java` — `1757ce3aaacdd6ab`
- `src/main/java/dev/projectflint/recipe/PersonalCraftingGuideRegistry.java` — `525379f47176e1c5`
- `src/main/java/dev/projectflint/registry/ModBlockEntities.java` — `0a49c0e034e5400b`
- `src/main/java/dev/projectflint/registry/ModBlocks.java` — `0ddd1108056dbc99`
- `src/main/java/dev/projectflint/registry/ModCreativeTabs.java` — `61646222ef063765`
- `src/main/java/dev/projectflint/registry/ModItems.java` — `48e8ad5630de6fd1`
- `src/main/java/dev/projectflint/system/BasinMaterialProcessingSystem.java` — `14d8d906ae398f51`
- `src/main/resources/assets/projectflint/blockstates/dynamite_charge.json` — `947fa968091e806e`
- `src/main/resources/assets/projectflint/items/black_powder.json` — `69e3fcf3ae994e00`
- `src/main/resources/assets/projectflint/items/carbon_dust.json` — `69aff48b9c401bc3`
- `src/main/resources/assets/projectflint/items/dynamite.json` — `c37f3ec5d1de768a`
- `src/main/resources/assets/projectflint/items/fine_fuse_powder.json` — `c5f74001c9524901`
- `src/main/resources/assets/projectflint/items/fuse.json` — `8262f57d35b78b67`
- `src/main/resources/assets/projectflint/items/fuse_ready_dust.json` — `94196320811af6ae`
- `src/main/resources/assets/projectflint/items/pure_carbon_dust.json` — `1229ae54e20761cc`
- `src/main/resources/assets/projectflint/items/sulfur_dust.json` — `960867eb37b24730`
- `src/main/resources/assets/projectflint/lang/en_us.json` — `91d71fe198fef5ae`
- `src/main/resources/assets/projectflint/models/block/dynamite_charge.json` — `c2b0ef58b288db25`
- `src/main/resources/assets/projectflint/models/item/black_powder.json` — `3737b0654cc81693`
- `src/main/resources/assets/projectflint/models/item/carbon_dust.json` — `e25830567c5f578f`
- `src/main/resources/assets/projectflint/models/item/dynamite.json` — `fb54baa04b50c38e`
- `src/main/resources/assets/projectflint/models/item/fine_fuse_powder.json` — `53938762a50c5781`
- `src/main/resources/assets/projectflint/models/item/fuse.json` — `dd331401237ff560`
- `src/main/resources/assets/projectflint/models/item/fuse_ready_dust.json` — `12ddd833e39aa7b9`
- `src/main/resources/assets/projectflint/models/item/pure_carbon_dust.json` — `0a6a0752679d7c5b`
- `src/main/resources/assets/projectflint/models/item/sulfur_dust.json` — `df8b5e348c73c1ec`
- `src/main/resources/assets/projectflint/textures/block/dynamite_charge.png` — `64bfefd68b77c51e`
- `src/main/resources/assets/projectflint/textures/item/black_powder.png` — `26ae27ab76706dcb`
- `src/main/resources/assets/projectflint/textures/item/carbon_dust.png` — `c03ad865154f165a`
- `src/main/resources/assets/projectflint/textures/item/dynamite.png` — `64bfefd68b77c51e`
- `src/main/resources/assets/projectflint/textures/item/fine_fuse_powder.png` — `f3febf3abbde010f`
- `src/main/resources/assets/projectflint/textures/item/fuse.png` — `2c236a091b1434a4`
- `src/main/resources/assets/projectflint/textures/item/fuse_ready_dust.png` — `d008931270e0a94c`
- `src/main/resources/assets/projectflint/textures/item/pure_carbon_dust.png` — `e5bdb53ae096237d`
- `src/main/resources/assets/projectflint/textures/item/sulfur_dust.png` — `e7ed4193125ed1a6`
- `src/main/resources/data/projectflint/recipe/hardwood_black_powder_gunpowder.json` — `4a13afd252714693`
- `src/main/resources/data/projectflint/recipe/hardwood_black_powder_potassium.json` — `8bc9011aa20283dd`
- `src/main/resources/data/projectflint/recipe/hardwood_dynamite.json` — `3ab2d103367d8687`
- `src/main/resources/data/projectflint/recipe/hardwood_fuse.json` — `e0e7fc74e2767caa`
- `src/main/resources/data/projectflint/recipe/hardwood_fuse_ready_dust.json` — `bda93352dd8ab46d`
- `src/main/resources/data/projectflint/recipe/primitive_wooden_sheet_from_stripped_log.json` — `63f81eb283d00c72`
- `src/main/resources/data/projectflint/tags/item/plank_cutting_axes.json` — `a17a923a8571d0c7`
- `src/main/resources/data/projectflint/tags/item/primitive_stripped_logs_for_sheets.json` — `11aacf5593d6382e`
- `src/main/resources/projectflint.mixins.json` — `0cbb1cc68c033fb2`
- `tools/validate_03166.py` — `2d3896e1f4d1029f`
