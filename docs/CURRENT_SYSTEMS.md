## Current rules — v0.31.65-b (supersedes conflicting Forge and Mud notes below)
- The Fireclay Forge **auto-starts both valid 4-log charcoal batches and valid charcoal-fueled Hardwood/MBSSIG cycles** after its previous cycle ends or when the required inputs are available. `ignite()` owns the common safety preflight. For wood: at least 4 burnable Logs, output space for Charcoal, formed Forge, usable Firestarter. For charcoal fuel: valid process material and primary output space. Never expend charcoal without a processable material. 4 Logs produce 4 Charcoal in a 60-second cycle; hardwood/alloy and glass chains retain 5-minute cycles.
- Wooden Grinder: 4 Fired Clay + 8 Mud Piles → 1 Loose Mud Bricks; 2 Loose Mud Bricks + 2 Wooden Screws → 1 Bricks. These are **Wooden Grinder** recipes, not default vanilla furnace or personal-grid recipes.
- Hardwood Table (60-tick / 3-second craft): 8 Dirt surrounding a Clay Water Bucket or vanilla Water Bucket → 8 Mud, returning an empty bucket; 1 Mud → 4 Mud Piles; Mud + Wheat → Packed Mud; 4 Packed Mud → 4 Mud Bricks; Mud Bricks → Loose Mud Bricks (irreversible). Vanilla-style Mud Brick Slabs/Stairs/Walls, Dirt Slabs/Stairs, Loose Dirt Slabs, Loose Mud Brick Slabs, and Dirt ↔ Loose Soil are available there. Newly added earth structures require a Wooden/Hardwood Shovel or Multitool to mine; a Vest is insufficient.
- Standing on vanilla Packed Mud applies `getSpeedFactor() = 1.10`, a 10% speed increase. The item tooltip tells players this; do not add a permanent player attribute bonus.
- Personal Leather Sheet + Bone Knife → **2 Leather Straps**, versus **3** at the Hardwood Table. Personal 2 String + Leather Strap → **1 Repair Kit**, versus **2** at the Hardwood Table. Personal Clay Block needs four Clay Balls, versus three at the Hardwood Table. Do not accidentally move the better output to personal crafting.
- Both guides must be updated for all new crafting changes; historic notes below do not override this section.
## Current patch — v0.31.64-b (supersedes older recipe/Forge notes below)
- **Personal 2×2 Wooden Tools:** Wooden Axe (Tannin + Leather Sheet + Stick + Wooden Sheet), Wooden Pickaxe (Stick + String + Leather Strap + Wooden Sheet), Wooden Shovel (any supported surface-world Plank + Stick + Leather Strap), Wooden Sword (2 Wooden Sheets + Stick), and Wooden Hoe (2 Wooden Sheets + Stick + Plant Tool Binding). The Hardwood Table retains regular 3×3 wooden tools.
- **Early Log-to-Planks bridge:** Flint Axe is included in the `projectflint:plank_cutting_axes` tag and can make 2 Planks from a Log, with normal durable tool wear. It enables the first Planks, Wooden Sheets and Wooden Axe without a circular prerequisite.
- **Personal early Oak cuts:** Oak Plank + Bone Knife → 3 Wooden Sheets; Oak Plank + Flint File → 1 Wooden Board. The Hardwood Table's generic any-Planks variants are instead 4 Sheets or 2 Boards. Durable tools wear by one point. Other wood types keep their existing personal and Hardwood recipes.
- **Utility Vest foliage:** explicit vanilla Bush, Firefly Bush, Short/Tall Dry Grass coverage alongside existing flora/forage bushes. Never unlocks stone/ore or structure mining; forage bushes still need the original progression tools without a vest.
- **Fireclay Forge:** Wood-to-Charcoal ignition stays manual. Hardwood and MBSSIG Dust processing can auto-ignite with Charcoal *only* with valid input, free output space, and a usable Firestarter; each uninterrupted Charcoal cycle still lasts 6000 ticks. A full output/absent input/unlit structure/no firestarter prevents consuming fuel. Earlier statements requiring manual ignition for every charcoal cycle are superseded.
- **Compatibility:** Keep `RecipeManagerMixin` filtering **vanilla Minecraft crafting IDs only**, not ProjectFlint personal recipes, and preserve CraftingRecipe placement in the 2×2 menu.

## Current contract — v0.31.61-b
- Pure Mica Insulated Glass is `TransparentBlock` with copied `Blocks.GLASS` properties, translucent model and approved blue-gray texture. It transmits skylight and block light like normal Glass; intact recovery still needs the Tanned Utility Vest.
- PMI Sheet is a non-placeable item. The **Hardwood Table alone** accepts 1 Pure Mica Insulated Glass + 1 Bone Knife and produces 2 PMI Sheets. Knife durability decreases by one; standard personal 2×2 crafting has no recipe.
- Delete old `data/projectflint/recipe/personal_pmi_sheets.json` when applying patches from v0.31.60-b. `build.bat` invokes the versioned cleanup before compiling. Gradle remains no-daemon; only successful release JARs go to `dist/`.
- Earlier v0.31.60-b wording below that permits personal crafting is historical and superseded.

## v0.31.60-b — Current Mica Glass and distribution contract

Pure Mica Insulated Glass is a `GlassBlock` clone of vanilla Glass behavior, not Tinted Glass: visible blue-gray but transparent, passes sunlight/block light normally, stays placeable and remains vest-gated for recovery without Silk Touch. Its Forge source remains MBSSIG Dust processed for five minutes with Charcoal. One intact Pure Mica Insulated Glass plus a Bone Knife produces two non-placeable PMI Sheets in either personal 2×2 crafting or the Hardwood Table; the Bone Knife receives one durability of wear. G and K guides must describe these outputs and permissions accurately. 

Build: `build.bat` uses a nonpersistent Gradle process (`--no-daemon`; `org.gradle.daemon=false`). On success it validates and copies the versioned release JAR into `dist/`, purges older Project Flint JARs from `dist/`, and selects the new JAR in Explorer. The build source, tests and sources JARs remain elsewhere. No JAR should be published on build failure.

## v0.31.59-b — Basin inventory, book and Mod Menu (current)

The Basin holds **four separately stored, single-item inputs**, renders all four actual item models, exposes its storage through Minecraft's `Container` interface, and processes the oldest material first. The active input tracks 0–2 completed stomps, depending on its 2–3 stomp requirement; it costs **one food point per successful stomp**, not once per item. Empty-hand recovery takes the newest input. New saves use `ContainerHelper` inventory encoding; previous single-stack Basin data is migrated, preserving excess as overflow to feed into free slots. Crushing triggers intermediate/finished sounds and particles; server owns all inventory and results. Jade inspection relies on its common Container support, still requiring in-game confirmation.

Pure Mica Insulated Glass is hand-recoverable **only when wearing the Tanned Utility Vest**, without Silk Touch; this exception is deliberately distinct from structural mining. The G-key Field Guide has 34 progression-ordered chapters and automatically fills each book page. K-key Basin recipe notes identify capacity and food cost. Mod Menu has an optional harmless config screen and verified GitHub Issues contact; Discord invite URL still needs official confirmation before direct-linking.

## v0.31.58-b — Current rules and additions

- Fireclay Forge: manual lighting required for every cycle; `serverTick` never auto-reignites. Charcoal cannot start unless it has Hardwood or MBSSIG Dust to process and the finished output has room. Wood fuel may still be manually used to make Charcoal. Each material needs 6000 ticks uninterrupted.
- Fat-Burning Torch: 60,000 ticks (**50 min**) per Fat charge, with charges supplied manually after placement. Ignition is forbidden if torch position Y < 0; at Y = 0 ignition remains permitted. Rain still extinguishes exposed flames.
- Superseded in v0.31.60-b: the tinted-light-blocking behavior below is no longer current. Pure Mica Insulated Glass now transmits sunlight like Glass. Mining/loot still returns the block itself with the Vest.
- Basin: each independent byproduct keeps its own random draw. Raw Copper Slag 12.5%; Clay/Gravel/Sand Rare Earth 4%; Slag Rare Earth 1.8%. Maximum three stomps, one food point each. Multiple different byproducts can spawn on the same final stomp.
- Personal crafting yields three Wooden Cups or five Raw Milk cups; the Hardwood Table yields four or six. Both milk recipes return the empty Clay Bucket. Bone Knife costs one durability and is not consumed.
- 8 Flax Seeds with one Clay Water Bucket in the Hardwood Table yields six Wheat Seeds; bucket returns empty. 8 Fertilizer surrounding Potassium Dust yields 12 Potassium-Rich Fertilizer, which behaves like Bone Meal except on saplings and tree fungi.
- Flint Plant Cutters use sticks on the left (`S` at x0 all three rows) and flint on top/bottom center plus middle right (`SF ` / `S F` / `SF `).

**Implementation boundaries:** Maintain server-side Forge controls, recipe remainders, Recipe Guide/Field Guide parity and no AI textures. `docs/BASIN_MATERIAL_YIELDS.md` supersedes old percentages elsewhere in historical release entries.

## v0.31.57-b — Current Basin & Glass Progression

Basin requires **2–3 landings**, **one hunger point per valid landing**. Clay Blocks, Rare Earth Dust and Cobblestone are accepted. See `docs/BASIN_MATERIAL_YIELDS.md` for all independent rolls. Hardwood Crafting Table must first be made in personal 2x2 with 2 Hardwood Alloy + 1 Cobblestone + 1 Bound Wood Processor. Hardwood-only recipes include the new dust/glass line; MBSSIG Dust becomes Pure Mica Insulated Glass in the Forge after **6000 ticks** of uninterrupted Charcoal fuel.

# Project Flint — Current implementation reference

**Authoritative through v0.31.63-b (Minecraft 1.21.11, Fabric, Mojang mappings).** This file summarizes current behavior, not the historical development path. More complete early-game pathways remain in `PRIMITIVE_PROGRESSION_MATRIX.md` and the corresponding gameplay source.

## World editing and mining

| World interaction | Accepted tool/condition | Target speed and results |
|---|---|---|
| Natural vanilla Stone (early Stone-working operation) | Crude Stone Hammer, Crude Multitool, Flint Multitool or Advanced Flint Multitool | 234 ticks for Crude Hammer (11.7s); Multitools follow their established Hammer multipliers. **Converts Stone into Crushed Stone in-world, drops 4 Rocks, costs 1 tool durability**; does **not** mine ores or Cobblestone. |
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
- Flint Quests owns tracked quests. Project Flint must not reintroduce its old advancement-based tree or EMI plugin. Successful Project Flint manual destruction and Stone-to-Crushed-Stone conversion emit Fabric's normal `PlayerBlockBreakEvents.AFTER` event with the **original broken block state** so `BREAK_BLOCK` tasks progress. Failed/forbidden mining never emits this event; unmodified vanilla block breaks are reported only by Fabric.
- Player-facing descriptions must not discuss dev implementation, release numbers, or outdated functionality.

## Other active gameplay boundaries (audit pointers)

- Hardwood Crafting Table: 3×3 crafting, separate recipe type, 60-server-tick per-result output; see `HardwoodCraftingMenu`.
- Fireclay Forge: uses Charcoal and time-gated heating for Hardwood Alloy; see forge entity and relevant recipe guide.
- Fat-Burning Torch: initially unfueled; wall and floor mount; each ignition consumes stored Fat, wall rotation follows vanilla Wall Torch orientation.
- Primitive inventory: five initial hotbar slots, expanded by progression; see `PrimitiveInventorySystem`.
- Full provenance / texture policy: `ASSET_SOURCES.md` and `TEXTURE_PROVENANCE.md`.
- Hardwood Sticks require two Hardwood; every Hardwood Tool, including Hammer, uses them; an Axle uses eight around Rope, and is a placeable axis-oriented shaft.
- Deep environment: Y below 0 applies stacking Speluncaphobia (one stack per 60s up to IV, increasing hunger above 6 food points and slowing healing); Y below -40 additionally applies Claustrophobia (-7% movement, -1 attack damage). Direct daytime sun exposure or block light >=13 clears both; they have no direct lethal damage.
- Basin stone/ore/gravel/sand/slag processing consumes 1 hunger point per valid stomp and rolls every possible byproduct independently. A guaranteed output is explicit where applicable; Slag can produce nothing.
- Brick Furnace processes Cobblestone to Stone; recipes for Cobblestone/Granite/Diorite/Andesite slabs and stairs use the Hardwood Table. Early Hinged Doors remain craftable.

## Checklist for future revisions

1. Before editing permissions, enumerate *both* natural and placed stone variants, and audit all four mining entry points.
2. Do not authorize based solely on hardness, equipped armor, block location or client-only hints.
3. Keep exact timing and drops consistent between guides, tests and source.
4. Update this document, appropriate ADR and release notes whenever a rule changes.
5. Record testing separately as source-static, Java compilation, client/server world test; never conflate them.

## v0.31.56-b: Basin and Hardwood crafting invariants

The Basin owns a stack of processable inputs, an integer stomp counter for the top input, and the server-side processing action. Its block-entity renderer displays the *exact input item* at the basin floor, including plank varieties, raw ore and mineral blocks; the old static inset wood geometry must never be restored. Insertion/removal and processing sync BE state to the client. Processed inputs yield the same independent stochastic byproducts as v0.31.55-b. The input costs exactly 1 hunger point per landed stomp; see `BASIN_MATERIAL_YIELDS.md` for stomp tiers and yields. Stomp progress resets on removal or completion and persists across chunk reload.

The Hardwood Crafting Table uses a separate `projectflint:hardwood_crafting` recipe type with a 60-server-tick output gate. Its result slot must obtain remainders from the **actual matched Hardwood CraftingRecipe**, not from a `RecipeType.CRAFTING` lookup: the latter silently consumes Bone Knife, Flint File, and other reusable ingredients. Reinsert tools at +1 damage into their originating slot when possible; break at maximum damage. Apply these rules for normal clicks and shift-clicks, with the timer rearmed for each craft.
