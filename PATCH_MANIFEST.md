# Project Flint v0.31.61-b — Patch Manifest

Base: v0.31.60-b · Target: Minecraft 1.21.11 Fabric, Java 21, Mojang mappings.

**Purpose:** Fix invalid glass class compilation and restrict PMI Sheets to Hardwood Table. Patch-only, root-relative changed files, no nested project folder. No AI-generated assets or unrelated gameplay changes.

**Important deletion:** The old personal recipe `src/main/resources/data/projectflint/recipe/personal_pmi_sheets.json` must be removed. Extracting a ZIP cannot delete it. `build.bat` calls `APPLY_PROJECTFLINT_0_31_61_B.bat` to remove this stale file before compilation; the cleanup script can also be run manually. Do not leave the JSON in the source tree.

## Files

- `APPLY_PROJECTFLINT_0_31_61_B.bat`
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
- `docs/decisions/0020-hardwood-only-pmi-sheets-and-mojang-glass-mappings.md`
- `docs/releases/0.31.61-b.md`
- `gradle.properties`
- `src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java`
- `src/main/java/dev/projectflint/recipe/PersonalCraftingGuideRegistry.java`
- `src/main/java/dev/projectflint/registry/ModBlocks.java`
- `tools/validate_03161.py`

## Validation

Mapped JAR inspection confirmed `TransparentBlock` exists while `GlassBlock` does not; `tools/validate_03161.py` checks recipe exclusivity, versions, and all resource JSON. Full Gradle build / in-game tests remain unverified.
