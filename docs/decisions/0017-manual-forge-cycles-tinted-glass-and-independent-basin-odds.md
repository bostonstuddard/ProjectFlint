# ADR 0017 — Manual Forge Lighting, Insulated Glass and Independent Basin Chances

**Status:** Accepted, v0.31.58-b

## Context

The Forge automatically ignited remaining fuel after completed burns, even without a recipe input; Pure Mica Insulated Glass could not be placed; Basin rarity was tuned and still needed independent multi-byproduct behavior. A few cup/seed/fertilizer crafts were inaccessible.

## Decisions

1. **Manual-only Forge ignition:** remove the server tick's automatic `ignite()` call. Charcoal requires a valid Hardwood/MBSSIG input and output capacity, protecting finite fuel. Wood can still be burned into Charcoal manually. Do not persist a queued auto-relight mechanism.
2. **Fat Torch:** one manually supplied Fat charge lasts 60,000 ticks (50 minutes). At negative Y coordinates, no ignition; Y = 0 is allowed. Respect rain and wall orientation behavior.
3. **Tinted glass:** represent Pure Mica Insulated Glass with a registered `TintedGlassBlock`, not just an inventory item. Retain vanilla light blocking, translucent rendering and block-item loot. Recolor vanilla Tinted Glass pixel art to blue-gray (not AI-generated). This registered item must remain the product of the 6000-tick MBSSIG Forge process.
4. **Basin rolls:** maintain an independent Bernoulli draw for *every chance*. A Clay Block may yield all its possible dusts simultaneously. Current rates: Raw Copper Slag 12.5%, Clay/Sand/Gravel Rare Earth 4% each, Slag Rare Earth 1.8%.
5. **Dual crafting tiers:** Bone Knife + Wooden Sheet yields three cups in personal 2×2, four in Hardwood 3×3; the tool wears, never disappears. Two cups plus a Clay Milk Bucket yield five or six Raw Milk cups respectively and return the emptied bucket. Flax Seeds + water and Potassium Fertilizer remain Hardwood Table exclusives.
6. **Potassium-Rich Fertilizer:** Bone Meal effect on normal growth interactions except saplings/tree fungi, to preserve the tree-growing gameplay stages.
7. **Design parity:** player guides show current procedures only; exact probabilities and material provenance belong in the current-state docs; no Project Flint-owned EMI plugin and no AI sprites.

## Rejected alternatives

Automatic fuel relighting, discarding remainders, exclusive Basin drop branches, fake non-placeable decorative glass, generic bonemeal that instantly grows trees, and exposing all Hardwood recipes to normal crafting.

## Validation needed

In-game Minecraft/Fabric Forge light/render pass, full Gradle build, recipe load and durability regression, saved Torch timers, independent Basin roll tests, normal versus shift-click crafting, and multiplayer state synchronization.
