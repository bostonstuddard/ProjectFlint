# Project Flint v0.31.50-b — Patch manifest

Base: v0.31.49-b complete Fabric Minecraft 1.21.11 source (Java 21). Extract directly to the root of the existing ProjectFlint source, overwrite matching files and run `build.bat`. No nested directory.

### Gameplay / UI
- `src/main/java/dev/projectflint/menu/HardwoodCraftingMenu.java` — server-validated 60-tick recipe production, state synchronization and crafting abuse prevention.
- `src/main/java/dev/projectflint/client/screen/HardwoodCraftingScreen.java` — custom 3×3 screen with visible processing bar.
- `src/main/java/dev/projectflint/registry/ModMenus.java` — dedicated menu registration.
- `src/main/java/dev/projectflint/client/ProjectFlintClient.java` — screen registration.

### Release and validation
- `gradle.properties`, `build.bat`, `APPLY_PROJECTFLINT_0_31_50_B.bat`
- `CHANGELOG.md`, `README.md`, `docs/releases/0.31.50-b.md`, `PATCH_MANIFEST.md`
- `tools/validate_release.py` — updated three-second behavior checks.

Previous patch releases are not included or re-applied. Existing custom artwork and game recipes are untouched.
