## v0.31.68-b — Weak Dynamite Balancing
### Explosives
- Weak Dynamite now breaks at most 2 ordinary Coal/Iron/Copper Ores within 1.75 blocks of the charge.
- Stone, Cobblestone, Andesite, Diorite and Granite now have a 50% chance to be destroyed per nearby block, within 1.25 blocks, capped at 1 stone-family block total.
- Reduced the effective Basin yield improvement from 50% to **20%**; all outputs remain independent, and bonuses over 100% still grant guaranteed items plus extra fractional rolls.
- Fixed Dynamite crafting: top PFP, middle PRP, bottom CCC (P=Black Powder, F=Fuse, R=Red Dye, C=Copper Dust).
### Mining
- Early Wooden/Hardwood Pickaxes and the Hardwood Hammer no longer harvest Dripstone, Pointed Dripstone, Tuff, Calcite, Basalt, Blackstone, Amethyst or other later-stage geology.
- The existing Crude Stone Hammer Stone-to-Crushed-Stone progression and ordinary Stone/Cobblestone/Andesite/Diorite/Granite tool permissions are preserved.

# Project Flint v0.31.67-b
- Removed the unintended personal crafting recipe that turned a Stripped Log and Bone Knife into one Wooden Sheet.
- The Wooden Grinder remains the intended route: one Wooden Beam and a Flint File produce four Wooden Sheets; the tool loses durability.
- Corrected the Field Guide and Recipe Guide so they no longer advertise the removed recipe.
- Flint Axes still cannot turn Logs into Planks.

## v0.31.66-b — Stone Blasting, Fuse and Carbon Processing
- Fixed a launch crash when Packed Mud movement speed tried to modify a nonexistent Minecraft 1.21.11 block-state method.
- Removed Flint Axe from Log-to-Planks recipes; Wooden and Hardwood Axes remain usable. Added an early Bone Knife + Stripped Log → 1 Wooden Sheet personal recipe so the first Wooden Axe remains craftable.
- Added Weak Dynamite: place with Dynamite in the main hand and an offhand firestarter, then get clear before the 4.5-second blast. The blast affects only Stone, Cobblestone, Andesite, Diorite, Granite, and regular Coal/Iron/Copper Ore. It can deal up to 12 damage.
- Stone and ore explosions use Basin byproduct tables with each independent chance multiplied by 1.5, including partial rolls above 100%; Deepslate is unaffected.
- Basin processing: Charcoal can produce Sulfur Dust (20%) and Wood Dust (15%); Coal can produce Sulfur (45%), Stone Dust (43% + separate 23%) and Carbon Dust (2.4%).
- Added Fuse-Ready Dust, Fine Fuse Powder, Fuse, Black Powder (two recipes), Dynamite, Sulfur Dust, Carbon Dust and Pure Carbon Dust, with Hardwood Table recipes and the supplied Fuse/Dynamite artwork.
- Brick Furnace now makes Fine Fuse Powder from Fuse-Ready Dust in 200 seconds. Fireclay Forge can refine Carbon Dust to Pure Carbon Dust in 400 seconds of charcoal heat, spanning two fuel cycles.
- Updated Field Guide, Recipe Guide, and source/release documentation.

## v0.31.65-b — Forge, Mud, Soil and Crafting
- Fixed the Fireclay Forge automatically continuing Log → Charcoal batches when four Logs, a usable Firestarter and empty output space are available; it never auto-uses Charcoal on an empty/blocked recipe.
- Reduced the Wooden Grinder Loose Mud Brick cost from 16 to 8 Mud Piles and the Bricks material cost from 4 to 2 Loose Mud Bricks.
- Added Hardwood Table recipes for Dirt + a water bucket → Mud, Mud → Mud Piles, Packed Mud, Mud Bricks, Dirt/Loose Dirt/Mud Brick slabs and stairs, and Loose Soil/Dirt conversion.
- Packed Mud now gives 10% faster walking speed, with a tooltip explaining the benefit.
- Fixed personal Leather Straps (2 instead of 3) and Repair Kits (1 instead of 2); Hardwood Table keeps the improved 3 Strap and 2 Repair Kit yields.
- Hardwood Table now forms Clay Blocks from 3 Clay Balls; personal crafting still takes 4.
## v0.31.64-b — Personal Wooden Tools and Workstation Fixes
- Restored the personal 2×2 recipes for Wooden Axes, Pickaxes, and all Wooden Shovel wood variants; added personal Wooden Sword and Hoe options. Earlier Wooden Tool costs stay unchanged.
- Flint Axes can now process Logs into Planks in personal crafting, providing a pre-Wooden-Axe route that prevents a circular Wooden Sheet prerequisite.
- Fixed Oak Wooden Sheets and Boards incorrectly being restricted to the Hardwood Table. Personal Oak Sheets yield 3 and Boards yield 1; the Hardwood Table's all-plank variants yield 4 and 2. Removed the duplicate Hardwood recipes shown for Oak.
- Tanned Utility Vests can now clear vanilla Bushes, Firefly Bushes, and dry grass, in addition to existing foliage. This does not allow stone/ore mining or bypass the early Twig-harvesting rules.
- Fireclay Forge now automatically starts a new Charcoal heating cycle only while a valid Hardwood/MBSSIG input, output room, available Charcoal and working Firestarter are all present. No idle Charcoal use. Log-to-Charcoal ignition remains manual.
- Updated G/K guides, progression checks and release notes. Existing mining, Quest events, Basin yields and equipment costs remain unchanged.

## v0.31.63-b — Custom Block Breaks Count Toward Quests
- Fixed Flint Quests block-breaking tasks not progressing when Project Flint transforms Stone into Crushed Stone with a Crude Stone Hammer or early Multitool.
- Other successful Project Flint manual block removals and transformations now count as breaking the original block, just like ordinary mining.
- Blocked mining attempts and tool-ineligible ore removal still cannot advance block-breaking quests. Normal vanilla mining does not count twice.
- No mining speeds, item drops, tool durability, recipes, or progression unlocks changed.

## v0.31.62-b — Early Stone Hammering Restored
- **Fixed** the Crude Stone Hammer being unable to work natural Stone: it now turns Stone into Crushed Stone and gives four Rocks again.
- Early Multitools retain the same Stone-crushing ability at their own working speeds.
- Wooden/Hardwood Pickaxes and the Hardwood Hammer retain normal stone mining. Hands, unrelated items and the Utility Vest still cannot mine Stone, Cobblestone or ore.
- Other stone-family blocks, Deepslate and ore mining restrictions are unchanged. No crafting recipes or textures changed.

## v0.31.61-b — Glass compile fix and Hardwood-only PMI Sheets
- Fixed the Pure Mica Insulated Glass compile error on Minecraft 1.21.11: the Mojang-mapped vanilla glass implementation is `TransparentBlock`, not the nonexistent `GlassBlock`. Preserve ordinary glass light transmission, the approved blue-gray texture, and Tanned Utility Vest recovery.
- PMI Sheets now craft **only** at the Hardwood Crafting Table: 1 Pure Mica Insulated Glass + 1 Bone Knife → 2 PMI Sheets, consuming one Bone Knife durability. Removed the personal 2×2 recipe and its guide entry. The cleanup script deletes the old recipe JSON from previously patched installations.
- Updated build/release numbers, application script, current player-facing guides, regression checks, and documentation. No other mechanics changed.

## v0.31.60-b — Normal Glass, PMI Sheets and distribution improvements

- Pure Mica Insulated Glass now uses vanilla `GlassBlock` mechanics and its existing blue-gray texture has been made lighter and more transparent. Sunlight passes through it like regular Glass; existing vest-only intact collection remains.
- Added non-placeable PMI Sheet items (stacks of 64). 1 Pure Mica Insulated Glass with a Bone Knife yields 2 Sheets in both personal and Hardwood crafting. The Bone Knife wears by one durability rather than being consumed.
- Updated the Field Guide, personal and Hardwood recipe references, creative inventory, item texture/model and translations.
- `build.bat`: force nonpersistent Gradle via `--no-daemon` and `org.gradle.daemon=false`, copy only the release JAR into `dist`, retain only the latest Project Flint release JAR there, and open Explorer on the finished output. Builds that fail do not publish JARs.
- Updated technical guides and ADR 0019. No Basin, mining, forge or other unrelated gameplay rules changed.

## v0.31.59-b — Basin inventory, Mod Menu, guide, glass recovery

- Basin is a four-item FIFO workstation: accepts up to four separate materials, displays them all on the basin floor, shows contents through the standard Minecraft Container interface for overlay integrations (Jade behavior requires in-game confirmation), retains current-item stomps across saves, and gives crushing-stage sounds/particles. Empty-hand retrieval removes the newest input; processing consumes the oldest input. The one-hunger-point-per-stomp and 2–3 stomp tiers are unchanged.
- Pure Mica Insulated Glass can be recovered intact without Silk Touch while wearing the Tanned Utility Vest. It cannot be mined bare-handed without the vest; this is **only** a glass exception, not a stone/structural mining exception.
- G-key Field Guide is rewritten into 34 concise, progression-ordered chapters, with dynamic pagination and clipped chapter scrolling. No release history or implementation notes appear in the in-game book. K-key recipe Basin notes mention four-item capacity and hunger cost.
- Mod Menu: imkas first author; ProjectFlint Contributors second; official GitHub Issues URL; optional configuration page offering Field Guide / Recipes links and a placeholder message. Project Modrinth listing is the website link until the exact Discord invite URL can be verified; its official listing offers a Join Discord server link.
- No new textures or imported assets. Existing Basin item renderer now shows each real item model.

## v0.31.58-b — Forge, Mica Glass, Fat Torches, Basin and Crafting

### Gameplay
- Fireclay Forge does not automatically consume more Charcoal after a cycle. Manually reignite each batch; do not waste Charcoal without a valid cooking input/output.
- Fat-Burning Torch burns for 50 minutes per Fat charge; no new ignition below Y=0.
- Pure Mica Insulated Glass is now a placeable, translucent, blue-gray tinted-glass block that blocks skylight like vanilla Tinted Glass and drops itself.
- Basin chance adjustments, keeping all rolls independent: Clay / Sand / Gravel 4% Rare Earth Dust; Slag 1.8% Rare Earth Dust; Raw Copper 12.5% Slag.
- Flint Plant Cutters 3×3 flint curve corrected (sticks in left column, flint on top/bottom middle and center right).
- Personal crafting: 1 Wooden Sheet + Bone Knife → 3 Wooden Cups; 2 Wooden Cups + Clay Milk Bucket → 5 Raw Milk cups.
- Hardwood Crafting Table: same cup recipes output 4 and 6 respectively; 8 Flax Seeds + filled Clay Water Bucket → 6 Wheat Seeds; 8 Fertilizer + 1 Potassium Dust → 12 Potassium-Rich Fertilizer.
- Potassium-Rich Fertilizer uses Bone Meal behavior on crops and eligible plants, but cannot instantly grow saplings or tree fungi. Clay Milk Bucket returns an empty Clay Bucket after crafting.
- G/K guide data, progression documentation, asset records and release validator updated.

**Verification:** Source/resource validation and isolated Java compilation of the new fertilizer item passed. Full Gradle build and actual client/server playtest remain pending.

## v0.31.57-b — Per-Stomp Basin Costs, Material Yields & Insulated Glass

- Basin charges one hunger point **per stomp**; every material takes 2 or 3 stomps. Fixed Basin hand/inventory/dropped item-model transforms.
- Added independent Iron Gold (3%), Copper Slag (5%), Sand Tin (15%), Andesite/Granite Stone Dust (33%) and Stone/Cobblestone 66%/44%/13.33% Stone Dust chances.
- Clay Block and Rare Earth Dust processing adds Mica, Silver, Gold, Lithium, Iron, Aluminum and Gallium with documented independent probabilities.
- Added Hardwood Table crafts for the requested leather, woodworking, bricks, bone/flint tools, windows, torches, woven fans and glass-dust items; 2x table Fat-Burning Torches, Leather Strap 3-per-cut and Repair Kit 2-per-recipe.
- Added Glass Dust → Borosilicate Dust → BSIG → BSSIG → MBSSIG Dust and five-minute charcoal-forged Pure Mica Insulated Glass.
- First Hardwood Crafting Table can now be made in the player's **2x2** grid (no workstation prerequisite loop).
- Updated G Field Guide, new K Hardwood Table category, technical docs, yields table, ADR 0016, release files, and model/texture provenance.

## v0.31.56-b — Physical Basin inputs and durable crafting safeguards
- Basin now renders its **actual stored item** on the basin floor, using synchronized block-entity inventory data instead of a fixed wood-only geometry. Input remains visible across relog, chunk reload and item type changes.
- Basin processing now requires material-specific landings: Planks 2; stripped Oak/Birch 3; Raw Iron 5; Raw Copper 4; Andesite/Granite/Stone/Diorite 4; Slag 3; Gravel/Stone Dust 2; Sand 2. A single hunger point is spent only on completion, and existing independent yield rolls stay unchanged. Removing one input cancels that item's stomp progress. Progress persists while stored.
- Fixed Hardwood Crafting Table **eating Bone Knives, Flint Files and reusable items** by replacing the vanilla result-slot's crafting-only remainder lookup with the matched Hardwood recipe's remainder list. A valid tool loses one durability per craft and breaks at zero; non-tool ingredients still get consumed normally. Shift-click output follows the same one-craft-per-60-ticks rule.
- G/K player guides and technical docs now describe physical Basin inputs, exact stomp counts, hunger timing and reusable tool wear. Added architectural decision 0015 and regression checklist.
- Target Minecraft 1.21.11 Fabric; unchanged all other mining/ore/Basin probabilities and Hardwood crafting recipes.

## v0.31.55-b — Depth risks, material Basin, Axles and progression corrections
- Recover small Torches, Chicken Nests and Axles by hand without a Vest; structural blocks remain tool-gated and Nest eggs eject properly.
- Deepslate and Deepslate Ores are inaccessible to any current survival tool. Hardwood Hammer tooltip clarifies Iron/Copper Ore; ordinary Iron/Copper Stone Ore drops exactly 1 Raw Ore with Hammer, Coal drops 2.
- Added Claustrophobia below Y=-40 (minor slowness/damage reduction) and stacking Speluncaphobia below Y=0 (more hunger, slower healing). Daytime direct sky exposure or block light >=13 removes both.
- Added orientable placeable Axle (8 Hardwood Sticks + Rope). Hardwood Sticks restored to Creative tab, have a dedicated stick sprite and are required by all Hardwood tools.
- Basin material washing supports Raw Iron/Copper, Stone/Diorite/Andesite/Granite, Stone Dust, Gravel, Sand and Slag with independent output chances and 1 hunger point per batch.
- Brick Furnace gains Cobblestone -> Stone; Hardwood Table gets an alternate Basin, Cobblestone/Andesite/Granite/Diorite slabs and stairs; restored earlier hinged-door crafting. Hardwood slab/stick patterns no longer conflict.
- Updated Field Guide, K-key machine recipe reference, current-system documentation, material probability matrix, architecture decisions and release notes.

## v0.31.54-b — Stone mining consistency, vest scope, and documentation
- Fixed the Tanned Utility Vest allowing fast client-side stone/cobblestone/andesite/diorite/granite break animations followed by server cancellation. The Vest now grants vegetation hand-clearing only; it cannot replace tools or dismantle Well structures.
- Unified vanilla stone-family and ore permissions across break progress, attack, BEFORE-break, and server loot handling. Bare hands, unrelated items and armor cannot mine ordinary rock. Wooden/Hardwood Pickaxes take 13.5s; Hardwood Hammer is slightly faster on rock at 12s. Coal and higher-tier ore rules remain gated.
- Corrected player-facing Field Guide mining and Hardwood recipes, reviewed K-key Recipe Guide (no new recipes), and added current-systems and ADR documents to prevent future behavior drift.

## v0.31.53-b — Fat-Burning Torch wall orientation hotfix
- Corrected all four Fat-Burning Torch wall orientations (lit and unlit) to match Minecraft's vanilla `template_torch_wall` geometry and Project Flint's Flint Torch.
- Fixed wall-mounted flame/smoke emission positions and selection hitboxes to track the correctly facing model.
- Preserved placement rules, Fat refueling and ignition, support checks, timer, recipes, and gameplay balance.

## v0.31.52-b — Hardwood crafting compilation hotfix
- Corrected the `RecipeDisplay` import in `HardwoodCraftingRecipe.java` for Minecraft 1.21.11 Mojang mappings (`net.minecraft.world.item.crafting.display`).
- No gameplay, crafting timer, ingredient, recipe registration, mining, or tool-balance changes.
- Updated version, build helper, cleanup script, release notes, and release validator.

## v0.31.51-b — Hardwood recipes, mining tiers, and durability
- Fixed generalized ore bypass: non-approved held items now take 45 seconds to mine an ore and yield nothing. Stone/Coal mining with Wooden Pickaxe, Hardwood Pickaxe and Hardwood Hammer takes 13.5 seconds; Hammer yields double Coal and is required for ordinary Iron/Copper Ore within the Hardwood tier.
- Increased Hardwood tool durability to 320 and Hammer durability to 560. Added Leather Straps to all Hardwood tool recipes except Shovel, including Hammer.
- Hardwood Table now makes three regular Sticks from two planks and two Hardwood Sticks from two Hardwood; added Hardwood Sticks item to the registry.
- Added 120+ dedicated Hardwood Table recipes and dye variants spanning tools, armor, masonry, books, paper, doors, cabinets, beams, chests, furnace, nest and staged Rope. Recipe type segregated from vanilla crafting.
- Removed outdated vanilla crafting paths for wooden tools/hinged doors and legacy kiln-ingredient furnace recipes from active recipe lists; preserved existing survival processing.
- Durable tools in Hardwood recipes are returned with one damage; empty Clay Bucket is returned by water recipes; Brick Mold is reusable.
- Updated README, build.bat, cleanup BAT, patch manifest, validation and player-facing release notes.

## v0.31.50-b — Hardwood Crafting Table timed-crafting repair
- Replaced the Hardwood Table's server-only result-slot pickup cooldown with a dedicated synchronized 3×3 crafting menu and GUI.
- Recipe outputs are now **unavailable for three full seconds (60 server ticks)** after a valid set of ingredients is placed. The output becomes visible and collectable only after that timer expires.
- Added a visible progress bar/countdown in the Hardwood Crafting Table interface. Both crafting time and output are driven by the server, not by a cosmetic client timer.
- Changing the crafting grid cancels and restarts the timer. Successful crafting starts a new three-second cycle for the next output, including stacked ingredients, shift-click, and recipes that return containers.
- Blocked premature pickup, hotbar swap and shift-click collection and removed the client-side instant-output/ghost-result mismatch.
- Retained vanilla recipe matching, normal 3×3 tool recipes, ingredient consumption, crafting remainders, table portability, and surrounding Project Flint progression.

## v0.31.49-b — Hardwood and Field Guide corrections
- Fat-Burning Torches now begin without Fat charges; re-placing them cannot replenish fuel. Players add Fat after placement before lighting. Torch crafting no longer consumes Fat prematurely.
- Removed the unsolicited mechanic lore tooltips from the Fat-Burning Torch and Hardwood tools/table; retained unrelated clothing/storage and food tooltips.
- Hardwood Crafting Tables now require a **Bound Wood Processor**, not an assembled/recovered Lumber Processor.
- All 18 supported Log/Stripped Log → 2 Planks durable recipes accept either Wooden Axe or Hardwood Axe; both retain the tool and apply durability loss.
- The K-key Basin recipes now include **Planks → Wood Dust**, and the Fireclay Forge recipes include **Hardwood + Charcoal → Hardwood Alloy** (one full five-minute burn).
- Replaced Field Guide release/development chatter with current, actionable, player-facing explanations and removed technical registry IDs from the Item Atlas.

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
