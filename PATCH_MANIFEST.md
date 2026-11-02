# Project Flint v0.31.52-b — Patch manifest

**Apply to:** Project Flint v0.31.51-b source, Minecraft 1.21.11 Fabric / Mojang mappings.
**Patch type:** compilation hotfix. Extract at project root with no nested folder.

## Changes
- `src/main/java/dev/projectflint/recipe/HardwoodCraftingRecipe.java`: move `RecipeDisplay` import to `net.minecraft.world.item.crafting.display.RecipeDisplay`.
- `gradle.properties`, `build.bat`, `APPLY_PROJECTFLINT_0_31_52_B.bat`: version bump and cleanup/apply helper.
- `CHANGELOG.md`, `README.md`, `docs/releases/0.31.52-b.md`: release documentation.
- `tools/validate_release.py`: version update and a regression check for the import.

## Validation
The corrected class path and `Recipe.display()` return type were confirmed using `javap` against the *Minecraft 1.21.11 mapped JAR included in the user's source archive*. Patch contents and archive integrity were checked. A full Project Flint Gradle build and Minecraft runtime test were not performed.

No older Project Flint content is re-applied or changed beyond the listed files.
