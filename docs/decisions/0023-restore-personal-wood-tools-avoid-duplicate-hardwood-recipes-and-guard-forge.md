# ADR 0023 — Early wooden-tool recipe ownership and guarded Forge cycles

**Status:** Accepted for v0.31.64-b.

**Context:** Disabling vanilla crafting accidentally filtered Project Flint's own early 2×2 tool recipes. Oak Sheet and Board recipes were also moved into the Hardwood Table, duplicating generic all-plank entries while making the personal guide inaccurate. The Utility Vest missed Minecraft's new Bush/Dry Grass variants. A fully manual Forge mode stopped valid heating recipes from continuing.

**Decision:** Preserve Project Flint namespace personal recipes even though vanilla namespace crafting stays disabled. Keep Oak-only durable Sheet/Board crafts in personal 2×2, retaining lower yields, and keep universal planks crafting in the Hardwood Table with higher yields. Wooden Sword and Hoe gain personal 2×2 alternatives. Vest adds only explicit vegetation coverage; stone/ore prohibitions remain above it. Auto-relight Charcoal cycles only after verifying material, output room, fuel, functional Firestarter and complete multiblock. Wood fuel remains manual.

**Consequence / tests:** Always audit the personal recipe guide against registered and filtered recipes, and use two recipes with the same inputs only if they're in different stations and intentionally different outputs. Test plain hand vs Vest on vanilla and custom bushes. Test empty-input, full-output and valid Forge cycles. Never use the old retirement list to remove these personal recipes again.
- Preserve the Flint Axe Log-to-Planks bridge to avoid the Wooden Sheet ↔ Wooden Axe prerequisite cycle.
