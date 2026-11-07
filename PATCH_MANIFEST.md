# Project Flint v0.31.69-b — Patch Manifest

**Baseline:** v0.31.68-b. Extract directly into the source root; no nested folder.

**Note:** Only new/modified files are supplied. Prior release assets and code must remain in place.

## Changed files
- `APPLY_PROJECTFLINT_0_31_69_B.bat` — SHA-256 `bc596ed880f57899`
- `CHANGELOG.md` — SHA-256 `07cdacaa3297db46`
- `README.md` — SHA-256 `968131dbcf4e7d93`
- `build.bat` — SHA-256 `36a839401b1d549c`
- `docs/ARCHITECTURE.md` — SHA-256 `dd535b3fc0ccf6ab`
- `docs/CURRENT_SYSTEMS.md` — SHA-256 `3905b7b0ed168a03`
- `docs/PRIMITIVE_PROGRESSION_MATRIX.md` — SHA-256 `035908695a0244cd`
- `docs/RELEASE_PROCESS.md` — SHA-256 `f713920a0f39c3e2`
- `docs/ROADMAP.md` — SHA-256 `709be442dd02b41f`
- `docs/decisions/0028-hardwood-table-wood-dust-recipe-and-controlled-dynamite-radius.md` — SHA-256 `8da3c873c97bdeb3`
- `docs/releases/0.31.69-b.md` — SHA-256 `d000cb458386af9b`
- `gradle.properties` — SHA-256 `461dee48eab8a40c`
- `src/main/java/dev/projectflint/block/entity/DynamiteChargeBlockEntity.java` — SHA-256 `b00de7cae072c9b4`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java` — SHA-256 `81200be81e775490`
- `src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java` — SHA-256 `e8e33e5ae65b74f5`
- `src/main/java/dev/projectflint/recipe/HardwoodCraftingGuideRegistry.java` — SHA-256 `b4de6d11c3fa14ae`
- `src/main/java/dev/projectflint/system/OreMiningRules.java` — SHA-256 `149f45a7c3ce78a9`
- `src/main/java/dev/projectflint/system/PrimitiveInteractionSystem.java` — SHA-256 `46ad2c1c771f11b5`
- `src/main/java/dev/projectflint/system/WoodenToolTargets.java` — SHA-256 `c15962730a3a2e14`
- `src/main/resources/data/projectflint/recipe/hardwood_hardwood_from_wood_dust.json` — SHA-256 `5192a1effbe0b081`
- `src/main/resources/data/projectflint/recipe/hardwood_hardwood_from_wood_dust_vanilla_bucket.json` — SHA-256 `690abe526f9c4a63`
- `tools/validate_03169.py` — SHA-256 `ff6447c6151318c9`

## Verification
- `python tools/validate_03169.py`: focused source, recipe, masonry and blast checks passed
- Isolated Java 21 compile: `WoodenToolTargets`, `OreMiningRules`, Basin yield system and Dynamite block entity passed against MC 1.21.11 mapped classes
- ZIP file integrity and root-relative patch paths checked
- **Full Gradle compilation and in-game testing are still unverified.**
