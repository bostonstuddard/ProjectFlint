## v0.31.48-b — Field Guide screen crash hotfix
- Prevented the Field Guide from invoking Minecraft 1.21.11's blur renderer more than once per frame. Opening the handbook previously crashed with `IllegalStateException: Can only blur once per frame` at `ProjectFlintFieldGuideScreen.java:154`.
- Replaced the extra `renderBackground(...)` call with a lightweight translucent screen dimmer; the leather/parchment panel and search, chapter navigation, items, tooltips and recipe-link behavior are unchanged.
- Preserved the K-key Recipe Guide implementation and all existing tools, crafting mechanics, mining speed limits and progression rules.

## v0.31.47-b — Hardwood balance and the Flint Field Guide
- Corrected Hardwood Hammer natural-rock and ore breaking to a fixed, capped **21-second** process. The tool can still mine stone-tier-and-below pickaxe blocks, but cannot outperform Wooden or Hardwood Pickaxes on stone. Mining speed is evaluated before Utility Vest recovery preview, preventing a shortcut.
- Reduced Hardwood Hammer melee damage (item attack modifier 8.0 → 2.0, material attack damage bonus 3.0 → 1.0) and base mining speed bonus (4.0 → 2.0). Added explicit "can injure and kill fish" tooltip and intentional slow-stone warning.
- Hardwood Pickaxe stone/coal/crushed-stone mining improved to **9 seconds** (Wooden Pickaxe: 13.5 seconds); masonry improved to 4.5 seconds (Wooden Pickaxe: 6 seconds), while preserving vanilla higher-tier restrictions. Hardwood tool material speed 2.5 → 3.5; durability remains 92.
- Registered Hardwood Axe as an actual vanilla-compatible `AxeItem` so right-click stripping Logs and Wood works correctly. Preserved Hardwood Axe attack/mining material.
- Added **Project Flint Field Guide**, a leather-and-parchment-style in-game handbook with **25 progression chapters**, multiple explanatory pages, searchable chapter descriptions, item icons, chapter scrolling, and an automatically filled, searchable, paginated Item Atlas for every registered Project Flint item/block-item.
- Press **G** (configurable) to open the Field Guide; **K** still opens the workstation-specific Recipe Guide. The field guide includes a button opening the recipe browser, and the recipe browser returns to the manual when closed. Does not require owning an item, having a Vest, or installing Flint Quests/EMI.
- No AI generated art; guide UI uses vanilla drawing and registered item sprites. All pre-existing gameplay recipes and external integration boundaries are preserved.

## v0.31.46-b — Minecraft 1.21.11 build hotfix
- Fixed Java compilation in `FoodBalanceSystem` by removing fish-bucket `FOOD` defaults with the supported component-builder `set(..., null)` call. The previous `.remove(...)` call does not exist on the 1.21.11 `DataComponentMap.Builder`.
- No changes to v0.31.45-b recipes, durability, progression, or game mechanics.

## v0.31.45-b — Mining enforcement, torch/fish repairs, and Hardwood progression
- Fixed the non-tool mining loophole: unsupported held items or bare hands cannot finish restricted dirt, stone, or ores through vanilla destroyBlock. Existing intentional hand-scraping and approved tool harvesting remain.
- Forage bushes require Refined Rock, an appropriate plant cutter, or Tanned Utility Vest hand clearance; bare-hand/irrelevant-item Stick farming is removed.
- Wooden Pickaxe mines regular Coal Ore into Coal (no torch progression unlocks). Fish are vulnerable to damage, including Creative attacks; fish buckets no longer display food stats.
- Fat-Burning Torch mounts on floors and walls, consumes Fat charges to ignite and relight, and preserves existing weather/burn behavior.
- Finished Clay Kiln body/top use the existing Brick Shingles texture, replacing raw clay visuals.
- Basin processes any plank to one Wood Dust with the same half-hunger cost as existing Tannin recipes. Two planks craft two Sticks.
- Two Wood Dust + Tannin + Clay Water Bucket craft Hardwood, returning the empty Clay Bucket. One Hardwood + charcoal-powered Fireclay Forge fire for five minutes yields one Hardwood Alloy.
- Two Hardwood Alloy + Cobblestone + Lumber Processor craft a movable Hardwood Crafting Table. Its 3x3 vanilla-style crafting requires three seconds per completed craft.
- Added the full Hardwood tool set and Wooden Tool vanilla-shaped recipes to the table. Hardwood tools have slightly better material statistics than wood; Hardwood Hammer uses six Hardwood Alloy, one Leather, one Stick, and one Rope and can mine stone-tier/lower ores and damage fish.

## v0.31.44-b — Wooden Pickaxe tier, torch support, and Wooden Sheet restoration
- Wooden Pickaxes can no longer break blocks requiring Stone/Iron/Diamond mining tiers, including Iron Ore, even while a Tanned Utility Vest is worn. The Vest still works normally for its vegetation and player-placed recovery rules.
- Fat-Burning Torches now require a supported block beneath them and remove/drop themselves when that support is lost, matching normal standing-torch support behavior instead of floating.
- Restored the original Project Flint Wooden Sheet texture from before v0.31.43-b; the separately supplied `woodsheet.png` is no longer used as the Wooden Sheet item art.

## v0.31.43-b — Forge validation, vegetation, and hardwood staging
- Fixed remaining Utility Vest vegetation gaps, including Project Flint forage bushes and berry bushes.
- Fireclay Forge now requires air only through its actual internal funnel/chimney; unspecified exterior gaps are ignored.
- Darkened Tanned Leather Armor/Utility Vest and enlarged the vest badge.
- Added owner-supplied Wood Dust/Hardwood/Hardwood Alloy/Hardwood tool assets as Creative-only staged items.
- Replaced the existing Wooden Sheet sprite with the supplied Wood Sheet artwork without changing its gameplay.

## v0.31.42-b — Well access and Utility Vest frame recovery
- The Well Pulley GUI opens regardless of Well formation or aquifer alignment, with immediately refreshed inactive/active status. Production still requires a valid Well and aquifer.
- Running or idle Well Pulleys can be dismantled with Flint Axe, Flint Plant Cutters, or Tanned Utility Vest: all stored Rope/output and the Pulley itself drop once, with production stopping as the controller is removed.
- Removed the obsolete Rope boost caption and moved the progress bar up without changing production rate or Rope behavior.
- Vest dismantling covers every Wooden Beam wood variant and returns the Wooden Screw as well as the Beam; the existing dedicated safe handling of block inventories remains in place.
- Specialized Well frame parts bypass generic player-placement salvage so no components or inventory are lost.

## v0.31.41-b
- Disabled Tool Belt crafting and passive bonuses (Creative-only item retained); corrected Wooden Axe log permission, hand-only Grass scraping, Grass Path Dirt drops, and instant leaf clearing.

## v0.31.40-b — Masonry mining and forage recovery
- Wooden Pickaxe now works on fired brick masonry, Fireclay brick blocks/slabs/stairs, Mortar and other brick variants, plus the brick machines' existing recoverable dismantle paths. Advanced Flint Multitool compatibility is retained.
- Stone and Crushed Stone share the deliberate 13.5-second Wooden Pickaxe mining pace; the later Vest preview cannot shortcut that speed.
- All active and legacy Project Flint forage bushes can now be hand-cleared into one vanilla Stick. Existing Refined Rock/Cutters twig yields remain unchanged.
- Gameplay recipes, machine processing, recipes guide and external PF-EMI ownership are unchanged.

## v0.31.38-b — Leatherworking and wooden tool progression
- Added sheet/strap processing, leather armor repair kits, 256-use Tool Belt, and Tannin processing in a craftable Basin.
- Added Wooden Pickaxe/Axe/Shovel progression, improved wood/earth/stone resource processing, wood door hinges, and common block conversions through the personal crafting grid.
- Introduced the charcoal-fueled, roof-protectable 30-minute Fat-Burning Torch, plus Brick Furnace-exclusive glass processing.
- Fixed tanned leather armor item appearance and shortened carrying-equipment tooltips with explicit slot counts.
- Added Personal Crafting and Basin to the recipe guide; gameplay crafting remains ProjectFlint-owned with PF-EMI external.

## v0.31.37-b — source build hotfix (patch 02)
- Fixed Minecraft 1.21.11 recipe ResourceKey identifier calls in the vanilla crafting filter and personal crafting recipe discovery.
- Fixed the missing vanilla `Items` import used by Tanned Leather armor registration.
- No gameplay, balance, assets, or recipes changed.

## v0.31.37-b — Fireclay Forge balance and player-facing hints
- Four logs/wood are now required and consumed for one 60-second Forge ignition, producing four Charcoal (one every 15 seconds). Charcoal fuel remains one per five-minute ignition with no return.
- Removed internal/development-only Forge tooltip text. Fuel, Firestarter, and Brick Furnace guidance use shorter, separate tooltip lines.
- Preserved recipe input/primary output independence, saves, auto-ignition, tool durability, and all other processing rules.

# Project Flint v0.31.32-b

## Farming, dairy, and cooking
- Flax Seeds + Clay Water Bucket now start a Wheat Seeds crop; mature Wheat processes into Flour with Clay Bucket water. Buckets return empty on use.
- Any raw meat (excluding raw milk) with a Clay Water Bucket makes Fat; eight Fat research a parchment-bound efficiency recipe yielding five Fat from four raw meats.
- Clay Buckets can milk adult cows and convert one Clay Milk Bucket + two empty Wooden Cups to four Milk Cups; Clay Bucket returns empty.
- Researched Primitive Oven additions: two Bread from three Flour and two Milk Cups, two Sushi Rolls from Calamari/Flour, and one Cheese from two Milk Cups and Fat.
- Turtles drop low-nutrition raw meat, cooked in primitive and vanilla heat stations.

## Fireclay, furnace and GUI
- Two Unfired Bricks plus one Sand Flake make two Unfired Fireclay Bricks; these cook to Fireclay Bricks. Clay Grinder recipes build the block, slab, and stairs.
- Wooden Grinder combines a Primitive Oven and eight Fireclay Brick Blocks into a Brick Furnace. Only the ordinary Brick Furnace converts vanilla Leather into Tanned Leather.
- Primitive Oven and Brick Furnace empty fuel slots show a wood indicator and hover help; empty Well rope slot shows a rope indicator and tooltip.

## World rules
- Surface Zombie Horses are removed separately from Monster-filtered riders.
- From night six, each surviving player in the Overworld has one five-percent nightly roll for a surface wolf pack of three to five wolves at 30–45 blocks.

## Assets
- Nine owner-supplied sprites imported without alteration, with recorded checksums. Clay Milk Bucket and Tanned Leather are palette derivatives of pre-existing approved vanilla/ProjectFlint source textures.
- PF-EMI remains a separate required runtime mod; ProjectFlint registers no EMI recipes.

# Project Flint v0.31.31-b

## Harvesting and farming
- Player-placed String/Tripwire and Place-in-World displays can be reclaimed without bringing back unwanted monster/world String drops.
- Strawberry Seeds now plant Strawberry Plants; fruit is food only, with saturation reduced from 0.15 to 0.05.
- Strawberry Jam and Strawberry Pie have parchment/Bookshelf/Primitive Oven recipes. Jam costs 12 Strawberries (4 more than Sweet Berry Jam), and Pie additionally consumes 2 Strawberries.

## Brick and bucket processing
- Bone Knife + 8 Wooden Sheets in the Wooden Grinder produces a reusable Unfired Brick Mold; using it with 8 Clay Balls yields 8 Unfired Bricks.
- Each Unfired Brick fires into a vanilla Brick; unfired bricks can be recycled to 2 Unfired Clay Balls; two finished vanilla Bricks can form 12 Wattled Daub Bricks. Vanilla Bricks blocks remain craftable, including a four-brick grinder conversion.
- Added 3 Clay Balls + Bone Knife as an alternative Unfired Clay Bucket grinder recipe. Bucket and Unfired Brick can fire in all current primitive heat stations and all vanilla furnace variants.

## Well balancing
- Aquifers: Scarce 13 (from 25), Normal 68 (from 60), Good 94 (from 85), Abundant 130 (from 120).
- Rope bonuses now scale as `3*n + floor(n*n/4)` for up to 16 ropes: single rope +3, 8 ropes +40, 16 ropes +112.
- Operating, non-blocked Wells emit a soft minecart-like rolling sound periodically.
- Well structure, water, output buffering, and item production rules are unchanged.

## Assets
- Imported the five owner-provided PNG files byte-for-byte; provenance hashes recorded.

## Integration
- PF-EMI remains external. No ProjectFlint EMI plugin, recipe interception, or API dependency was introduced.

# Project Flint v0.31.30-b

## Strawberries
- Added Strawberry as a new edible, plantable berry item using the supplied owner-authored texture.
- Added a four-stage Strawberry Plant using the supplied stage 0–3 textures.
- Strawberry Plants reuse Sweet Berry Bush-style random growth and bone-meal growth, but do not have thorn damage/slowdown.
- In Survival, age 2–3 plants require a Project Flint plant-cutting tool to harvest. Harvesting yields 1–2 Strawberries, resets the plant to age 1, and costs 1 tool durability.
- Flint Plant Cutters and the Advanced Flint Multitool can uproot Strawberry Plants and recover one Strawberry for replanting.
- Strawberry patches now have a chance to generate in Birch Forest, Old Growth Birch Forest, Flower Forest, Forest, and Meadow biomes. The patch geometry/frequency mirrors Minecraft's common Sweet Berry Bush patch style: 1-in-32 placement chance, 96 tries, 7-block horizontal spread, and 3-block vertical spread on Grass Blocks.

## Assets
- Imported the Strawberry item and all four Strawberry Plant growth-stage PNGs directly from `new update stuff(6).zip` without modifying their pixels.

## Regression scope
- No existing Sweet Berry Bush behavior was replaced.
- No Well, Lumber Processor, kiln, grinder, Sanding Station, Chicken Nest, Clay Bucket, Flint Quests, inventory, darkness, PF-EMI ownership, or other unrelated gameplay behavior was changed.

## v0.31.33-b — Dough, breads, and early-food balance
- Raised Sushi Roll output to five with added Sugar; reduced researched Fat input to three raw meat, condensed research tooltip, and added researched efficient Oven eggs.
- Replaced the old flour/milk Bread step with Dough and Sweet Dough progression and ten specialty breads.
- Added Brick Furnace to the in-game recipe guide; introduced Creative-only Basin, Drying Rack, and Millstone prototypes without crafting recipes.
- Slightly reduced common vanilla food value while preserving ProjectFlint's researched meal values.

## v0.31.35-b — Forge structure, fuel, guide and station hitboxes
- Fireclay Forge shell now follows the actual six-layer structure: solid floor, controller ring, stair-corner ring, two hollow plus rings, and four single slab caps. Shared fireclay casing walls remain valid.
- New fuel slot independent of future recipe input: each wood/log burns 60 seconds and yields one charcoal at each 15-second interval; charcoal burns five minutes with no byproduct. Every fuel ignition costs one firestarter durability, including automated refuels. Fuel/byproducts/timers persist, full output pauses without waste.
- Forge GUI now includes the recipe guide and no longer labels the recipe-input slot as wood. Recipe guide shows the current byproduct and fuel rules.
- Basin, Drying Rack and Millstone collision/selection boxes match their low models rather than a full cube.
- Guide output names are clipped and horizontally auto-scrolled with pauses; hovering text shows full names. Added a mandatory guide-release checklist.

## v0.31.34-b — Forge prototype and workstation presentation
- Chicken Nest: 16 Plant Fiber + 2 String in the Wooden Grinder; Well Pulley: 16 of any stripped wood/log/stem + 1 Rope. A stone-edged/wood-centered pulley now has a dark rope socket on its underside.
- Clay Bucket (Water)/Clay Bucket (Milk) naming aligned. Milk-filled buckets are non-edible; the existing cup/milk crafting path still returns the empty vessel.
- Corrected malformed Basin and Millstone cube-atlas models into actual geometry, and preserved the supplied element-based Drying Rack model with the appropriate asset namespace. All three remain Creative-only prototypes.
- Creative-only Fireclay Forge controller, horizontal rotation, formed/lit status, 3x3 hollow shareable casing, button-triggered firestarter ignition and automatic relighting, persistence, chimney smoke, sound and light. It currently turns four normal/stripped overworld log/wood items into four Charcoal in 45 seconds. No controller crafting recipe.

### v0.31.36-b
Tanned Leather armor (+0.5 armor and +96 durability per piece), gold-marked infinite-durability Utility Vest with 22 usable slots and hand-vegetation/Bedroll recovery, vest-gated personal 2×2 crafting and custom-only recipe book, survival Fireclay Forge controller, and Charcoal-only 10-minute Brick Furnace fuel. The Forge now exposes independent main output and charcoal-byproduct slots; fuel is no longer misrepresented as a crafting recipe. Fixed Primitive Oven timer overflow and added Oven/Brick Furnace countdowns. See `docs/releases/0.31.36-b.md`.

### v0.31.39-b
- Player-facing workstation recipe pages now contain recipes only. Equivalent output variations are consolidated with cycling examples and accepted-input hover lists.
- Fireclay Forge controller requires 32 Wooden Sheets + 32 Fireclay Brick Blocks in the Wooden Grinder.
- Basin uses oak/cobblestone textures, displays its stored wood, and processes one stripped Oak/Birch wood or log per jump at a cost of one hunger point. Stored contents persist and drop when recovered.
- Wooden Pickaxe/Shovel mining permissions and drops now agree with mining progress. Wooden Axe combat damage becomes 4.5.
- Utility Vest hand-clearing works regardless of held item and gives vanilla vegetation drops, with a 2% flax seed bonus from grass. Qualifying player-placed blocks can also be recovered; Fireclay masonry requires Advanced Flint tier.
