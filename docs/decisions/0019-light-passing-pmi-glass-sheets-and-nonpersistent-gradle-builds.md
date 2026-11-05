# ADR 0019 — Light-passing Mica Glass, Sheets and build distribution

**Status:** Accepted in Project Flint v0.31.60-b. Supersedes the glass light-blocking part of ADR 0017. Keeps glass recovery rules from ADR 0018.

## Context

Tinted-glass mechanics made Pure Mica Insulated Glass visibly dark and prevented light passing despite the requirement for a transparent glass material. The owner requested a nonplaceable glass sheet cut with a Bone Knife. Persistent Gradle daemons were left running after Windows builds, and output in `build/libs` was inconvenient to collect.

## Decision

1. Register Pure Mica Insulated Glass as vanilla `GlassBlock` with copied Glass properties. Preserve blue-gray identity through a **more transparent derivative** of its existing approved texture and leave the translucent render layer intact. Keep vest-only intact recovery and Forge production unchanged.
2. Register `projectflint:pmi_sheet` as a normal Item (not BlockItem), max stack 64, with its own glass-pane-like flat item sprite. Offer **1 Pure Mica Insulated Glass + Bone Knife → 2 PMI Sheets** in personal and Hardwood crafting, using their durable recipe serializers to damage—not consume—the Knife by one per craft. Update both G and K guides.
3. Run Gradle with `--no-daemon` on wrapper and bootstrap execution and set `org.gradle.daemon=false`. Do **not** terminate other Java processes. Only after successful build, expected-JAR presence and PF-EMI nesting validation copy the runtime JAR to `dist/`, purge older Project Flint release JARs in that directory, and open Explorer to the output. Sources/dev JARs stay outside `dist`.

## Regression and verification

- Sunlight transmission and visible glass transparency (no dark occlusion); vest/mining permission, drop and multiplayer parity.
- Both crafting interfaces: tool durability remainders, full grid / inventory, shift-click, timer in Hardwood Table; PMI Sheet not placeable.
- Windows Gradle wrapper/bootstrap paths, download/reuse paths, success/failure exit codes, no persistent Gradle daemon, only finished JAR in `dist`.
- Static checks are not substitutes for Gradle compilation or in-game testing.
