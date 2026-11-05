# ADR 0020 — Hardwood-only PMI Sheets and Mojang-mapped glass class

**Status:** Accepted in v0.31.61-b. Supersedes the dual-workstation recipe in ADR 0019 and corrects its nonexistent `GlassBlock` class reference. Retains the glass light-transmission decision and other v0.31.60-b behavior.

## Decision

Pure Mica Insulated Glass uses `net.minecraft.world.level.block.TransparentBlock` with full-copy vanilla Glass properties on Minecraft 1.21.11. This is the mapped implementation available in the target JAR. Preserve client translucency, blue-gray texture, and vest-only intact recovery.

PMI Sheets require the Hardwood Crafting Table, never personal 2×2 crafting. The single recipe is 1 Pure Mica Insulated Glass + Bone Knife → 2 non-placeable PMI Sheets. The Bone Knife loses one durability through the Hardwood Table's custom durable crafting path. Remove `personal_pmi_sheets.json` and its personal guide record, and delete the old JSON from existing installations through the next incremented apply script. In-game guides must describe the current restriction without version history.

## Regression requirements

1. `ModBlocks.java` imports and instantiates `TransparentBlock`; not `GlassBlock` or `TintedGlassBlock`.
2. Only `hardwood_pmi_sheets.json` remains. The old personal file is absent after running the apply script.
3. The personal grid produces no PMI Sheets; the Hardwood Table produces two with a three-second delay and correct Bone Knife wear (regular and shift-click).
4. Glass renders and passes light normally and recovers with the Vest; the successful build publishes only its finished runtime JAR in `dist/`.
