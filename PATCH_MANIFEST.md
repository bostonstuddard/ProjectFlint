# Patch Manifest — Project Flint v0.28.41-a

## Modified / New Files
- `src/main/java/dev/projectflint/system/ToolCapabilities.java`
- `gradle.properties`
- `build.bat`
- `APPLY_PROJECTFLINT_0_28_41_A.bat`
- `tools/validate_release.py`
- `CHANGELOG.md`
- `PATCH_MANIFEST.md`
- `docs/VERSIONING.md`
- `docs/ARCHITECTURE.md`
- `docs/ROADMAP.md`
- `docs/releases/0.28.41-a.md`

## Gameplay fix
Restores Crude Multitool leaf harvesting. The intended output remains **4 Twigs + 1 Branch** for 1 durability, which reopens the progression-critical Branch path.

## Validation
`tools/validate_release.py` passes for v0.28.41-a and includes a regression check for the Crude Multitool foliage capability. Full Gradle compilation remains blocked by unavailable Fabric Loom `1.14.10` plugin resolution in this environment.
