## v0.31.57-b — Current Basin & Glass Progression

Basin requires **2–3 landings**, **one hunger point per valid landing**. Clay Blocks, Rare Earth Dust and Cobblestone are accepted. See `docs/BASIN_MATERIAL_YIELDS.md` for all independent rolls. Hardwood Crafting Table must first be made in personal 2x2 with 2 Hardwood Alloy + 1 Cobblestone + 1 Bound Wood Processor. Hardwood-only recipes include the new dust/glass line; MBSSIG Dust becomes Pure Mica Insulated Glass in the Forge after **6000 ticks** of uninterrupted Charcoal fuel.

# Project Flint — Current implementation reference

**Authoritative as of v0.31.56-b (Minecraft 1.21.11, Fabric, Mojang mappings).** This file summarizes current behavior, not the historical development path. More complete early-game pathways remain in `PRIMITIVE_PROGRESSION_MATRIX.md` and the corresponding gameplay source.

## World editing and mining

| World interaction | Accepted tool/condition | Target speed and results |
|---|---|---|
| Vanilla stone-family blocks (Stone, Cobblestone, Andesite, Diorite, Granite, Tuff, vanilla pickaxe stonework and equivalent placed blocks) | Wooden Pickaxe, Hardwood Pickaxe, Hardwood Hammer, correctly tiered vanilla Pickaxe | Wood/Hardwood Pickaxe 270 ticks (13.5s); Hardwood Hammer 240 ticks (12s); vanilla upgraded pickaxes use ordinary vanilla speed. Ordinary block drops. Stone drops Cobblestone and applies Flint's adjacent Stone effect. |
| Same stone-family blocks with bare hand, food, cutters, vest, etc. | Not permitted | 0 progress; no ghost completion/drops |
| Coal Ore | Wooden Pickaxe, Hardwood Pickaxe, Hardwood Hammer, compatible vanilla Pickaxe | 270 ticks with basic pickaxes / 240 ticks with Hammer; 1 Coal, or 2 with Hammer |
| Iron/Copper Stone Ore (regular variants) | Hardwood Hammer or vanilla Stone-or-better Pickaxe | Hammer 270 ticks; vanilla mining speed and vanilla drops; Hardwood Hammer yields exactly 1 Raw Iron or Raw Copper |
| Deepslate and Deepslate Ore variants | No available survival tool | No mining progress or drops, even with vanilla upgraded Pickaxes |
| Other ores | Properly tiered vanilla Pickaxes only for drops | Unauthorized tools: 900 ticks (45s) and no drops; no Hardwood tier bypass |
| Project Flint custom brick/workstation blocks and vanilla Bricks | Existing explicit tool/capability handlers | Preserve their configured timing and salvage; not covered by broad vanilla rock handling |
| Hand-recoverable small decorations (Fat/Flint/vanilla Torches, Chicken Nest, Axle) | By hand without Vest | 5 tick break window, recover original item; Nest ejects its inventory; no generalized station or structural-block salvage |
| Vegetation | Tools specified by harvesting systems, or Tanned Utility Vest where explicitly allowed | Vest hand-clearing grants plant handling, **not** mining or machine salvage |
| Well Beams/Pulley | Explicit Well-compatible tools | Correct item/screw/inventory salvage retained; vest alone cannot dismantle |

## Code authority and synchronization

`OreMiningRules.isRestricted`, `isStoneFamily`, `canGetDrops`, and `progress` define vanilla rock/ore eligibility. `BlockStateBaseMixin` predicts breaking speed; `PrimitiveInteractionSystem` handles client/server attack and BEFORE-break; `ServerPlayerGameModeMixin` authorizes actual outcomes and loot. All four paths must agree; no hardness-based player-placement preview is acceptable. Legacy `PlayerPlacedBlockLedger` remains a read-compatible persisted type; its placement-recording mixin remains inactive. A fixed decoration whitelist explicitly authorizes light pickups without using armor, hardness, or generic player-placement recovery. The whitelist is server-authoritative and mirrored in the client progress check.

## Player-facing references

- `G` — Field Guide: current survival and progression, including stone mining tiers and Utility Vest limitations.
- `K` — Recipe Guide: includes the new independent-probability Basin outputs (chance information on output hover), Wood Dust/Tannin, Fireclay Forge and Brick Furnace Stone recipe. Hardwood tools/crafts are driven by a separate dedicated recipe type.
- Flint Quests owns tracked quests. Project Flint must not reintroduce its old advancement-based tree or EMI plugin.
- Player-facing descriptions must not discuss dev implementation, release numbers, or outdated functionality.

## Other active gameplay boundaries (audit pointers)

- Hardwood Crafting Table: 3×3 crafting, separate recipe type, 60-server-tick per-result output; see `HardwoodCraftingMenu`.
- Fireclay Forge: uses Charcoal and time-gated heating for Hardwood Alloy; see forge entity and relevant recipe guide.
- Fat-Burning Torch: initially unfueled; wall and floor mount; each ignition consumes stored Fat, wall rotation follows vanilla Wall Torch orientation.
- Primitive inventory: five initial hotbar slots, expanded by progression; see `PrimitiveInventorySystem`.
- Full provenance / texture policy: `ASSET_SOURCES.md` and `TEXTURE_PROVENANCE.md`.
- Hardwood Sticks require two Hardwood; every Hardwood Tool, including Hammer, uses them; an Axle uses eight around Rope, and is a placeable axis-oriented shaft.
- Deep environment: Y below 0 applies stacking Speluncaphobia (one stack per 60s up to IV, increasing hunger above 6 food points and slowing healing); Y below -40 additionally applies Claustrophobia (-7% movement, -1 attack damage). Direct daytime sun exposure or block light >=13 clears both; they have no direct lethal damage.
- Basin stone/ore/gravel/sand/slag processing consumes 1 hunger point and rolls every possible byproduct independently. A guaranteed output is explicit where applicable; Slag can produce nothing.
- Brick Furnace processes Cobblestone to Stone; recipes for Cobblestone/Granite/Diorite/Andesite slabs and stairs use the Hardwood Table. Early Hinged Doors remain craftable.

## Checklist for future revisions

1. Before editing permissions, enumerate *both* natural and placed stone variants, and audit all four mining entry points.
2. Do not authorize based solely on hardness, equipped armor, block location or client-only hints.
3. Keep exact timing and drops consistent between guides, tests and source.
4. Update this document, appropriate ADR and release notes whenever a rule changes.
5. Record testing separately as source-static, Java compilation, client/server world test; never conflate them.

## v0.31.56-b: Basin and Hardwood crafting invariants

The Basin owns a stack of processable inputs, an integer stomp counter for the top input, and the server-side processing action. Its block-entity renderer displays the *exact input item* at the basin floor, including plank varieties, raw ore and mineral blocks; the old static inset wood geometry must never be restored. Insertion/removal and processing sync BE state to the client. Processed inputs yield the same independent stochastic byproducts as v0.31.55-b. The input costs exactly 1 hunger point on completion; see `BASIN_MATERIAL_YIELDS.md` for stomp tiers and yields. Stomp progress resets on removal or completion and persists across chunk reload.

The Hardwood Crafting Table uses a separate `projectflint:hardwood_crafting` recipe type with a 60-server-tick output gate. Its result slot must obtain remainders from the **actual matched Hardwood CraftingRecipe**, not from a `RecipeType.CRAFTING` lookup: the latter silently consumes Bone Knife, Flint File, and other reusable ingredients. Reinsert tools at +1 damage into their originating slot when possible; break at maximum damage. Apply these rules for normal clicks and shift-clicks, with the timer rearmed for each craft.
