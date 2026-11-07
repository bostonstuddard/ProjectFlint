# Project Flint v0.31.63-b — Patch Manifest

**Baseline:** ProjectFlint v0.31.62-b source. This hotfix is intended for the ProjectFlint component of the v0.32.0 combined release; it does not modify FlintQuests or PF-EMI.

## Files included

- `src/main/java/dev/projectflint/mixin/ServerPlayerGameModeMixin.java` — report successful manual block breaking / replacement to Fabric's AFTER event using the original block state and block entity; guard against no-ops, unqualified mining, and vanilla duplicates.
- `src/main/java/dev/projectflint/client/screen/ProjectFlintFieldGuideContent.java` — update early Stone quest guidance.
- `gradle.properties`, `build.bat`, `APPLY_PROJECTFLINT_0_31_63_B.bat` — version/build/cleanup.
- `CHANGELOG.md`, `README.md`, `PATCH_MANIFEST.md` — release records.
- `docs/ARCHITECTURE.md`, `docs/CURRENT_SYSTEMS.md`, `docs/PRIMITIVE_PROGRESSION_MATRIX.md`, `docs/ROADMAP.md`, `docs/RELEASE_PROCESS.md` — current systems and future regression protection.
- `docs/decisions/0022-custom-player-break-events-for-transformed-blocks.md` — architectural decision.
- `docs/releases/0.31.63-b.md` — complete release note.
- `tools/validate_03163.py` — regression validator.

## Exact behavior

1. `minecraft:stone` processed by Crude Stone Hammer or Multitool becomes Crushed Stone + four Rocks, unchanged.
2. The canceled custom break issues Fabric `PlayerBlockBreakEvents.AFTER` **once**, with the **original** `minecraft:stone` state. FlintQuests' existing `BREAK_BLOCK` listener receives that normal event.
3. Other successfully manually removed or transformed player-targeted blocks use the same path.
4. Ordinary vanilla block breaks are handled by Fabric only; failed actions and unqualified restricted ores do not fire the custom event.
5. No recipe, texture, drop, mining-speed, or durability change.

## Verification

- Source/resource regression validator (`python tools/validate_03163.py`) passed.
- Verified the Fabric 1.21.11 `PlayerBlockBreakEvents.After` signature against the user's mapped dependency JAR.
- Verified current FlintQuests `QuestEngine` listens to `AFTER` for `TaskType.BREAK_BLOCK` and uses the original block registry ID.
- In-game bundled 0.32.0 survival validation and full Gradle build are **not** claimed.

## Applying to a bundled release

Extract at source root, run the incremented apply script automatically via `build.bat`, and build ProjectFlint. Replace the ProjectFlint JAR in the combined v0.32.0 bundle with the newly built version; do not include duplicate ProjectFlint JARs. FlintQuests does not need a source patch for this bug.
