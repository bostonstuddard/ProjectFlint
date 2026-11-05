# ADR 0016 — Basin Stomp Economy and Mica Glass Processing

**Status:** Accepted, Project Flint v0.31.57-b

## Context

The Basin previously charged one hunger point per completed process and allowed up to five stomps. This was neither the intended per-landing exertion economy nor sufficiently uniform across material tiers. Several dust and glass outputs needed to fit the existing Hardwood → Forge progression without leaking into vanilla's global recipe pool. The Hardwood Crafting Table itself had inadvertently become an impossible prerequisite of its own crafting recipe.

## Decisions

1. A **valid landing** costs exactly one food point, even before completion. Deny progress when hunger is depleted. Creative mode has no cost.
2. All Basin material processing requires **two or three** stomps, with input-bound persisted progress. Keep item rendering and block visuals independent. Return input from the Basin without ingredient conversion if player extracts it.
3. Each chance in the materials table is **independent**; do not combine the Stone/Cobblestone 66/44/13.33% roll into an all-or-nothing bundle. Chosen small chances: Copper Slag 5%; Iron Gold Dust 3%, subject to playtest tuning.
4. Reserve `projectflint:hardwood_*` recipe type for the Hardwood Table. The **Hardwood Table bootstrap** is a personal 2×2 `minecraft:crafting_shapeless` recipe, not a 3×3 exclusive.
5. Knife/File actions use `hardwood_durable`, not ordinary shapeless crafting. Both items receive wear when required by a recipe; they must not disappear as consumed materials. Clay Water Buckets return an empty Clay Bucket through the item remainder.
6. Add each dust as a distinct item, creatively accessible and included in user-visible recipes; pixels originate from existing approved Wood Dust artwork, recolored for identification.
7. Forge processing of MBSSIG Dust requires **one entire 6000-tick Charcoal burn** and is mutually exclusive with Hardwood Alloy per input slot. Changes to the input invalidate partial progress.
8. Keep G/K guides, odds documentation, progression notes and release tests synchronized. PF-EMI is separate and must not be implemented by a Project Flint-owned EMI plugin.

## Rejected alternatives

No instant Basin output, no free stomps before the final hit, no default vanilla crafting exposure of the 3x3 Hardwood recipes, no arbitrary recipe chance exclusivity or hidden tool consumption, and no AI-generated sprite substitution.

## Future work

In-game validation of item display transforms, durability/container remainders, Forge elapsed time and read-only PF-EMI custom recipe presentation. Fine-tune the two unspecified "small" chances after actual playtesting.
