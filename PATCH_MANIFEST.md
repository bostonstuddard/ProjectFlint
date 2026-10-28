# Project Flint v0.31.48-b — incremental source hotfix

**Base:** Fully patched **v0.31.47-b** source on Minecraft 1.21.11, Fabric, Java 21.

**Apply:** Extract this patch ZIP **directly into the source root**, overwriting matching files. Do **not** extract a nested ProjectFlint folder. Run `build.bat` to produce `projectflint-0.31.48-b.jar`. The builder invokes `APPLY_PROJECTFLINT_0_31_48_B.bat`, preserving previous cleanup lineage and retiring the v0.31.47-b cleanup script.

**Fix:** `ProjectFlintFieldGuideScreen.render()` no longer makes a second screen background-blur request that triggers `IllegalStateException: Can only blur once per frame` when G opens the manual. It draws a semi-transparent dimmer instead.

**Contents:** One Field Guide Java fix, version metadata, builder wiring, next-version apply/cleanup BAT, README, CHANGELOG, release notes and validator regression guard. No JAR or nested directories; no change to gameplay, quests, EMI, or the existing recipe-browser hotkey.

**Runtime validation:** Cause grounded in the user's exact 1.21.11 Fabric crash stack. Automated Java compile and static validation are not an in-game runtime test.
