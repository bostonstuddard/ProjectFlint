# Project Flint v0.31.68-b — Patch Manifest

**Baseline:** v0.31.67-b. Extract files directly into the project source root (no nested directory); run `build.bat`, which calls `APPLY_PROJECTFLINT_0_31_68_B.bat` for legacy cleanup.

**No gameplay assets were replaced.** Existing Dynamite/Fuse images, all workstation recipes and crafting-time logic are preserved except for the specified Dynamite recipe pattern.

## Changed/new files
- `APPLY_PROJECTFLINT_0_31_68_B.bat` — SHA-256 `786eff3ff6b95306`
- `CHANGELOG.md` — SHA-256 `d6cb5461169b9977`
- `README.md` — SHA-256 `a20e62074d4145ca`
- `build.bat` — SHA-256 `f709fc6acaef2cde`
- `docs/ARCHITECTURE.md` — SHA-256 `d2cf98faef2e2f9a`
- `docs/CURRENT_SYSTEMS.md` — SHA-256 `9c4f4c2437b2dfaa`
- `docs/PRIMITIVE_PROGRESSION_MATRIX.md` — SHA-256 `72cac3edc6317a58`
- `docs/RELEASE_PROCESS.md` — SHA-256 `2aa41f1a4068fe8f`
- `docs/ROADMAP.md` — SHA-256 `5c1b548512008046`
- `docs/decisions/0027-small-dynamite-blast-and-later-geology.md` — SHA-256 `3b4cc6df85c1d785`
- `docs/releases/0.31.68-b.md` — SHA-256 `0c3968f21ffdd0e9`
- `gradle.properties` — SHA-256 `486e921155f84c56`
- `src/main/java/dev/projectflint/block/entity/DynamiteChargeBlockEntity.java` — SHA-256 `28499727a71ee27e`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java` — SHA-256 `8531aea8553a4c57`
- `src/main/java/dev/projectflint/recipe/HardwoodCraftingGuideRegistry.java` — SHA-256 `5dceab4698b000fd`
- `src/main/java/dev/projectflint/system/BasinMaterialProcessingSystem.java` — SHA-256 `acdb7f5c89ab0945`
- `src/main/java/dev/projectflint/system/OreMiningRules.java` — SHA-256 `2b5a3726440d6265`
- `src/main/resources/data/projectflint/recipe/hardwood_dynamite.json` — SHA-256 `9d9fb5f673277e1d`
- `tools/validate_03168.py` — SHA-256 `0e5643adc2e572c7`

## Validation
- Isolated Java 21 compilation succeeded for `DynamiteChargeBlockEntity`, `OreMiningRules`, and `BasinMaterialProcessingSystem` against the mapped Minecraft 1.21.11 JAR, extracted Fabric API modules, and test-only registry stubs.
- `python tools/validate_03168.py` passed 1,752 source/resource assertions.
- Full Gradle build and Minecraft client/server smoke tests are **not** verified.
