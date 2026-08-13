# Project Flint Primitive Progression Matrix — 0.26-a

This document is the developer-facing source of truth for the currently implemented primitive opening. It records acquisition routes, tool inheritance, processing boundaries, workstation behavior, and regression expectations. If code and this matrix disagree, the disagreement should be treated as a bug or an intentional change that must update this document in the same release.

## Material progression

| Material / object | Current acquisition route | Stack | Notes |
|---|---|---:|---|
| Rock | Gather a generated Loose Rock; Stone crushing also yields four | 1 | Base stone-working object |
| Refined Rock | 8-strike Rock-on-Rock work, 68% success | 2 | Early forage/fiber working tool |
| Twig | Refined Rock forage bushes: 3–4; Cutters: exactly 4 | 16 | Early construction/fuel |
| Plant Fiber | Grass scraping chance; Leaf Litter later yields guaranteed fiber | 8 | Fiber-processing input |
| Crude Plant String | 2 Plant Fiber worked with Refined Rock; Cutters later harvest it directly | 4 | Binding/assembly material |
| Lightly Burnt Twig | Completed primitive Twig Pile fire | 4 | Bootstrap heat-treated twig |
| Crude Stone Hammer | Physical assembly: 2 Lightly Burnt Twigs + 1 Rock | 1 | 24 durability |
| Crude Reed | Hammer bootstrap from Sugar Cane; later hand-worked with cutter-like tools | 4 | Binding input |
| Plant Tool Binding | 4 Crude Reeds + 2 Crude Plant Strings | 32 | Tool assembly component |
| Very Refined Stone | Rock-on-Crushed-Stone refinement | 8 | Cutters/campfire/grinder progression |
| Crude Stone Cutters | 2 Very Refined Stone + 1 Plant Tool Binding + 1 Lightly Burnt Twig | 1 | 48 durability |
| Campfire Stone | 16-strike Very Refined Stone work, 100% | 12 | Permanent campfire frame |
| Burnt Twig | Instant permanent-campfire/kiln processing of Twig | 16 | Later fire material; no cook timer |
| Branch | Multitool harvesting of leaves; Clay Grinder: 2 Twigs → 1 | 16 | Flint-tier construction/fuel |
| Burnt Branch | Instant permanent-campfire/kiln processing of Branch | 32 | Flint multitool assembly; no cook timer |
| Crude Multitool | 2 Burnt Twig + 4 Plant Tool Binding + 4 Very Refined Stone | 1 | 128 durability |
| Dirt Pile | Multitool digging of Dirt/Grass | 4 | 4 compact into Loose Dirt |
| Loose Dirt | 4 Dirt Piles hammered together | 16 | Falling material / kiln-top shaping |
| Loose Dirt Slab | Right-click placed Loose Dirt with either Multitool | 16 | Leaves one slab placed, world-drops a second, and costs 1 durability |
| Flint Chip | Multitool Gravel harvesting | 16 | 4 hammer-work into vanilla Flint |
| Flint Firestarter | 8 Branches + 2 vanilla Flint | 1 | 32 durability |
| Flint Multitool | 2 Burnt Branches + 2 vanilla Flint | 1 | 256 durability |
| Crudely Woven Bowl | 12 Burnt Twigs + 32 Dirt Piles | 1 | Clay collection chain |
| Unfired Clay | Reusable Fired Clay Bowl filled from four Clay blocks | 4 | Registry ID remains `unfired_clay_ball` for save compatibility |
| Fired Clay | Open-fire Unfired Clay Ball with 4 Twigs for 30s → 2 | 4 | Four compact into Crude Clay Block |
| Crude Clay Block | 4 Fired Clay, hammer-compacted | 8 | Cut into Crude Clay Kiln |
| Crude Clay Kiln | Cutter-shaped Crude Clay Block | 1 | Player-facing; requires support |
| Baked Clay Kiln | Crude kiln + 8 Branches + 30s firing | 1 | Facing is preserved |
| Super Heated Clay | Timed Baked Kiln processing of Unfired Clay, 4s | 16 | Grinder construction material |
| Clay Grinding Base | Place 8 Super Heated Clay on supported ground | 1 | Add Very Refined Stone to build Grinder |
| Clay Grinder | Clay Grinding Base + 12 Very Refined Stone | 1 | Custom Project Flint menu/recipe system |
| Reed Roll | Hand-work 4 Crude Reeds with either Multitool | 16 | Wash against a body of water |
| Sugarcane Roll | Right-click water with 1 Reed Roll | 16 | Grinder input |
| Sanding Paper | Clay Grinder: 4 Sugarcane Rolls → 1 | 16 | Reusable filing tool; not consumed by hand-working |
| Roasted Seeds | Timed heat-processing of Plant Fiber | 16 | Registry ID remains `plant_seeds` |
| Wool | Sheep wool loot/shearing conversion | 16 | Custom Project Flint wool item |
| Animal Bones | Chicken/Cow/Horse/Pig/Wolf/Ocelot/Squid death loot | 16 | One added per supported animal death |
| Raw Calamari | Squid death loot | 16 | Tagged raw meat; heat-processes into Cooked Calamari |
| Cooked Calamari | Campfire/Kiln processing of Raw Calamari | 16 | Cooked food |
| Filed Bone | File 1 Animal Bones or vanilla Bone with Sanding Paper | 16 | Bone technology input |
| Bone Firestarter | Hand-work 1 Filed Bone + 1 vanilla Flint | 1 | 96 durability; shares centralized Firestarter capability |
| Bone Club | Clay Grinder: 4 Filed Bones → 1 | 1 | 160 durability; 4.2 total attack damage / 1 attack speed |
| Woven Fan | File 1 Plant Tool Binding with reusable Sanding Paper | 16 | Woven equipment/storage component |
| Reed Backpack | Physical assembly: 8 Woven Fans + 2 Plant Tool Bindings + 2 Woven Wool | 1 | Chest equipment; infinite durability; 0.5 armor; unlocks all 9 hotbar slots |
| Bone Brush | Physical assembly: 4 Filed Bones + 4 Woven Fans | 1 | 160 durability; brushes Sand/Gravel for 3.5s and shears sheep for 3–4 Project Flint Wool |
| Sand Flake | Bone Brush on Sand; Sand becomes Clay | 16 | Four form one Sand Ball |
| Sand Ball | Hand-work 4 Sand Flakes with a Multitool | 16 | Kiln-only Glass Shard input |
| Glass Shard | Kiln-fire 1 Sand Ball | 16 | Renewable glass-chain output |
| Woven Wool | File 2 Wool with Sanding Paper | 16 | 8 place one Crude Sleeping Bag |
| Crude Sleeping Bag | Place 8 Woven Wool across supported foot/head positions | 1 | Two-block bed-tagged sleep target; dyeable; breaks into 2 Woven Wool |
| Clay Mortar | Clay Grinder: 9 Super Heated Clay → 1 | 32 | Water-mixing and Mortar Kiln construction input |
| Unfired Mortar Bricks | Right-click water with 1 Clay Mortar → 4 | 16 | Kiln-only firing input |
| Mortar Bricks | Kiln-fire 1 Unfired Mortar Brick → 1 | 64 | First non-gravity building block; hammer harvest |
| Mortar Chest | Use 1 Woven Fan on placed Mortar Bricks | 64 | Seven slots; Flint-tier tool required for intact recovery |
| Raw Leather | Replaces Cow Leather drops | 16 | Cow resource substitution; Animal Bones still apply |
| Unfired / Fired Clay Block Mold | Clay Grinder from mold base → kiln firing | 8 | Fired form is reusable in the special Advanced Kiln fuel-compression recipe |
| Burnt Branch Block | Advanced Kiln: 4 Branches + 4 Flint + Fired Clay Block Mold, 15s | 64 | Gravity-affected compressed fuel; grinder converts one block to two slabs |
| Burnt Branch Slab | Clay Grinder: 1 Burnt Branch Block → 2 | 64 | 1 slab → 8 Burnt Branches; 2 slabs → 1 block |
| Mortar Kiln Base | Clay Grinder: 32 Mortar Bricks → 1 | 64 | Finish with Advanced Kiln Glass |
| Mortar Kiln | Mortar Kiln Base + Advanced Kiln Glass | 64 | Clay-kiln processing with a 15-minute burn; Advanced Flint Multitool recovery only |
| Wooden Grinder Base | Clay Grinder: 16 Woven Wood → 1 | 64 | Finish with Stone Grindsaw |
| Stone Grindsaw | 8 Very Refined Stone → Crushed Stone; 8 Crushed Stone → Grindsaw | 1 | Wooden Grinder construction component |
| Wooden Grinder | Wooden Grinder Base + Stone Grindsaw | 64 | Two inputs; inherits all Clay Grinder single-input recipes |
| Ground Leather | Wooden Grinder: 2 Raw Leather + 1 Sanding Paper | 16 | Right-click water to convert into vanilla Leather |
| Woven Chest Frame | Wooden Grinder: 8 Leather + 8 Woven Wood | 16 | Right-click a Mortar Chest to upgrade it |
| Woven Chest | Mortar Chest + Woven Chest Frame | 64 | Twelve slots; Flint Axe recovery |
| Leather Backpack | Wooden Grinder: 4 Leather + Reed Backpack | 1 | Unbreakable; 2 armor; unlocks inventory slots 0–12; 1.05× food exhaustion |

## Tool capability inheritance

`ToolCapabilities` is the authoritative code-level table. Multitools inherit earlier Hammer/Cutters capabilities rather than reimplementing them ad hoc.

| Capability | Refined Rock | Crude Hammer | Crude Cutters | Crude Multitool | Flint Multitool |
|---|:---:|:---:|:---:|:---:|:---:|
| Refined-rock fiber/forage work | ✓ | — | — | — | — |
| Hammer operations | — | ✓ | — | ✓ | ✓ |
| Cutter operations | — | — | ✓ | ✓ | ✓ |
| Break leaves for Twig + Branch | — | — | — | ✓ | ✓ |
| Dig Dirt / Grass | — | — | — | ✓ | ✓ |
| Harvest Gravel → Flint Chips | — | — | — | ✓ | ✓ |
| Break Loose Dirt | — | — | — | ✓ | ✓ |
| Shape / harvest Loose Dirt Slabs | — | — | — | ✓ | ✓ |
| Dismantle Baked Kiln / Clay Grinder | — | — | — | — | ✓ |
| Flint-tier yield bonuses | — | — | — | — | ✓ |
| Direct whole flower/bush harvest | — | — | — | — | ✓ |

## Tool stats and speed inheritance

| Tool | Durability | Total attack damage | Attack speed | Primitive work speed |
|---|---:|---:|---:|---|
| Crude Stone Hammer | 24 | 2 | 0.8 | 1.0× Hammer baseline |
| Crude Stone Cutters | 48 | 1 | 1.2 | 1.0× Cutter baseline |
| Crude Multitool | 128 | 2 | 1.2 | 1.25× inherited Hammer/Cutter work |
| Flint Multitool | 256 | 3.5 | 1.2 | 2.0× inherited Hammer/Cutter work; direct flowers, Project Flint forage bushes, Dead Bushes, Bushes, and Firefly Bushes are immediate whole-plant harvests |
| Bone Firestarter | 96 | 1 | 4 | Firestarter capability only |
| Bone Club | 160 | 4.2 | 1.0 | Weapon only |

Earth digging uses the separate intended progress multipliers: Crude Multitool `0.30×` vanilla progress and Flint Multitool `0.50×` vanilla progress.

## Timed primitive processing

All permanent Campfire/Kiln processing uses `AbstractPrimitiveProcessorBlockEntity` and `PrimitiveProcessingRegistry`:

**one input → recipe lookup → timer → one output**.

Only one item may be processing at a time. If the station stops burning before the timer finishes, progress and the held item remain in the block entity and resume when the station is active again.

| Input | Output | Campfire | Baked Kiln | Time |
|---|---|:---:|:---:|---:|
| Unfired Clay | 1 Super Heated Clay | — | ✓ | 4s |
| Egg | 2 Scrambled Egg | ✓ | ✓ | 15s |
| Plant Fiber | 1 Roasted Seeds | ✓ | ✓ | 15s |
| Unfired Clay Bowl | 1 Fired Clay Bowl | ✓ | ✓ | 15s |
| Twig | 1 Burnt Twig | ✓ | ✓ | Instant (0 ticks) |
| Branch | 1 Burnt Branch | ✓ | ✓ | Instant (0 ticks) |
| Raw Beef | Cooked Beef | ✓ | ✓ | 15s |
| Raw Porkchop | Cooked Porkchop | ✓ | ✓ | 15s |
| Raw Chicken | Cooked Chicken | ✓ | ✓ | 15s |
| Raw Mutton | Cooked Mutton | ✓ | ✓ | 15s |
| Raw Rabbit | Cooked Rabbit | ✓ | ✓ | 15s |
| Raw Cod | Cooked Cod | ✓ | ✓ | 15s |
| Raw Salmon | Cooked Salmon | ✓ | ✓ | 15s |
| Potato | Baked Potato | ✓ | ✓ | 15s |
| Kelp | Dried Kelp | ✓ | ✓ | 15s |
| Unfired Mortar Brick | Mortar Bricks | — | ✓ | 15s |
| Raw Calamari | Cooked Calamari | ✓ | ✓ | 15s |
| Sand Ball | Glass Shard | — | ✓ | 15s |
| Unfired Kiln Glass | Advanced Kiln Glass | — | ✓ | 15s |
| Crude Clay Block | Unfired Clay Mold Base | — | ✓ | 15s |
| Unfired Clay Axe Mold | Fired Clay Axe Mold | — | ✓ | 15s |
| Unfired Clay Brick Mold | Fired Clay Brick Mold | — | ✓ | 15s |
| Unfired Clay Cutters Mold | Fired Clay Cutters Mold | — | ✓ | 15s |
| Unfired Clay Multitool Mold | Fired Clay Multitool Mold | — | ✓ | 15s |

The Baked Kiln ejects completed output directly out of its front face. The Campfire ejects completed output above its center.

## Workstation behavior audit

| Workstation | Support | Rotation | Break rule | Salvage / output rule |
|---|---|---|---|---|
| Primitive Campfire | Required | N/A | Existing component-disassembly behavior while unlit | Timed output above center |
| Crude Clay Kiln | Required | Faces player / shaping direction | Movable while unlit | Bakes into Baked Kiln and preserves facing |
| Baked Clay Kiln + top | Base requires support; top requires base | Base/top share facing | Flint-tier workstation tool | Advanced Flint Multitool retrieves the kiln base/top intact; earlier Flint-tier dismantling keeps the 2 Fired Clay salvage |
| Clay Grinding Base | Required | N/A | Flint Multitool only | World-drops 4 Super Heated Clay; manual dismantling also returns inserted Very Refined Stone |
| Clay Grinder | Required | Current model orientation only | Flint-tier workstation tool | Advanced Flint Multitool retrieves the grinder intact; earlier Flint-tier dismantling keeps the 4 Super Heated Clay salvage |
| Crude Sleeping Bag | Both halves require support | Faces player; linked foot/head, occupied, and color state | Either half removes the pair | Advanced Flint Multitool retrieves the Sleeping Bag with its dye color; other removal keeps the 2 Woven Wool salvage |
| Mortar Bricks | Normal block support rules | N/A | Hammer-like tools | World-drops 1 Mortar Bricks block; is not gravity-affected |
| Mortar Chest | Normal block support rules | Faces player | Flint tier or better | Seven persistent slots; drops contents and recovers the chest block |

## Clay Grinder architecture

Alpha.22 removes the Stonecutter architecture entirely:

- `ClayGrinderMenu` extends `AbstractContainerMenu` and owns its input, six selectable recipe-preview slots, and output slot.
- `ClayGrinderScreen` is a Project Flint-owned code-rendered screen.
- `ModMenus.CLAY_GRINDER` is a Project Flint menu type.
- `ClayGrinderRecipe` is a Project Flint single-input recipe class.
- `ModRecipes.CLAY_GRINDING` and its serializer are Project Flint registries.
- Current `projectflint:clay_grinding` recipes include the established Sanding Paper/Bone Club/Mortar/Branch/glass/slab/log/mold routes plus **1 Unfired Clay Mold Base → 1 Unfired Clay Block Mold**, **1 Burnt Branch Block → 2 Burnt Branch Slabs**, **1 Burnt Branch Slab → 8 Burnt Branches**, **32 Mortar Bricks → 1 Mortar Kiln Base**, **16 Woven Wood → 1 Wooden Grinder Base**, **8 Very Refined Stone → 1 Crushed Stone**, and **8 Crushed Stone → 1 Stone Grindsaw**. Wooden Grinder single-input mode consumes this same recipe set. Recipes carry an explicit input count, so the menu consumes the complete required quantity on output pickup.

## Regression gates

Before a release after alpha.22, verify all of the following:

1. Clay Grinder opens and remains open; no class in the Project Flint grinder path extends `StonecutterMenu` or `StonecutterRecipe`.
2. No vanilla stonecutting recipe appears in the Clay Grinder.
3. Primitive block-break rewards spawn as world ItemEntities instead of bypassing normal pickup into inventory.
4. Baked Kiln/Clay Grinding Base/Clay Grinder remain Flint-Multitool dismantle targets with their fixed salvage.
5. Crude → Baked kiln conversion preserves facing; top creation uses the same facing.
6. Campfire/Kiln process exactly one item at a time through the shared processor state machine.
7. Super Heated Clay is kiln-only; it must not be producible in the Campfire.
8. Flint direct flowers, Project Flint forage bushes, Dead Bushes, Bushes, and Firefly Bushes remain immediate whole-plant targets; held flowers remain the petal-processing route.
9. Any change to tool inheritance, processing, yields, durability, stack size, or workstation salvage updates this matrix in the same release.
10. Texture replacements remain exact unmodified user-approved human-made source files.
11. Reed Roll washing only succeeds when the targeted block/fluid is water, consumes one roll outside Creative, and yields exactly one Sugarcane Roll.
12. Clay Grinder Sanding Paper consumes exactly four Sugarcane Rolls per result and never exposes vanilla stonecutting recipes.
13. Sheep wool output is replaced with Project Flint Wool, while the seven approved animal types add Animal Bones only on death-context loot; Squids also add Raw Calamari.
14. Horses are excluded from custom retaliation; Squid vanilla flee-on-damage behavior remains removed.
15. Sanding Paper is never consumed by flower, Bone, or Wool filing; inputs and outputs remain exact, and flower outputs match vanilla dye recipes.
16. Bone Firestarter has exactly 96 durability and resolves through the same centralized Firestarter capability as the Flint Firestarter.
17. Sleeping Bags create linked supported foot/head halves, are included in `minecraft:beds`, accept all vanilla dye colors, advance a valid night, reward a completed night with 2 seconds of Regeneration I, and always salvage exactly 2 Woven Wool.
18. The Place in World payload is server-authoritative, removes exactly one held item, blocks attacks, restores on right-click, and world-drops on support loss.
19. Health thresholds use strict below-14/below-8 health checks, hunger uses food below 8, jumping is blocked while Hungry, and Dying + Hungry produces Weakness II.
20. Mortar Bricks are kiln-only, non-gravity, and remain behind the hammer break capability.
21. Eating any item in `projectflint:raw_meats` applies Poison I/Nausea I for 100 ticks and Hunger III for 60 ticks.
22. The Clay Grinder consumes exactly 2 Twigs for 1 Branch.

## Alpha.23 Advanced Kiln and cast-tool flow

1. Grind 4 Glass Shards into Unfired Kiln Glass and fire it into Advanced Kiln Glass.
2. Fire a Crude Clay Block into one Unfired Clay Mold Base.
3. Shape the base in the Clay Grinder into the desired mold and fire that mold.
4. Build a complete 2×2×2 Mortar Brick cube and click its intended front with Advanced Kiln Glass.
5. Load the controller with 8 Burnt Branches, 8 vanilla Flint, 4 Fired Clay, and one fired mold.
6. After 200 ticks / 10 seconds the consumable materials are spent and the mold remains installed.
7. Outputs are Flint Axe Pieces, 6 Mortar Bricks, Flint Cutter Piece, Refined Flint Multitool Piece, or the reusable Clay Pot from a Fired Clay Pot Mold.
8. A cast tool piece plus 2 Burnt Branches completes through a reversible ground assembly. The finished tools carry explicit main-hand combat attribute profiles alongside their harvesting/processing behavior: Flint Axe 5.75 damage / 0.60 speed, Flint Plant Cutters 4.25 / 1.40 with 320 durability, Advanced Flint Multitool 4.85 / 1.25.

The Advanced Flint Multitool changes the requested utility yields to Gravel → 1 vanilla Flint + 1 Flint Chip, Dirt/Grass → 3 Dirt Piles, and leaves → 3 Branches with a 5% Apple chance. It retrieves the Sleeping Bag item intact while retaining its dye color and can recover the Baked Kiln and Clay Grinder intact.

## 0.25.0 wood processing extension

| Input / action | Gate | Output | Reversible |
|---|---|---|---|
| Placed vanilla plank slab + Sanding Paper | Sanding Paper | Sanded Wood Slab | Source species intentionally collapses to one generic slab |
| 2 Sanded Wood Slabs in Clay Grinder | Clay Grinder | Grinded Wood Slab | Yes: 1 Grinded -> 2 Sanded |
| Placed Grinded Wood Slab + Woven Fan | Woven Fan | Woven Wood Slab | No fan recovery |
| 2 Woven Wood Slabs in Clay Grinder | Clay Grinder | Woven Wood | Yes: 1 Woven Wood -> 2 slabs |
| 2 Loose Dirt / Mortar Brick / Crude Clay slabs | Clay Grinder | Matching full block | Yes |
| 2 matching vanilla wood slabs | Clay Grinder | Matching stripped log/stem/block | Yes, for each existing stripped-log slab recipe |


## 0.26-a upgraded infrastructure flow

1. Standard Advanced Kiln mold recipes now process for 60 seconds. Fired Clay Pot Mold is consumed; established Axe/Brick/Cutters/Multitool molds remain reusable.
2. Shape an Unfired Clay Block Mold from an Unfired Clay Mold Base, kiln-fire it, then load the Advanced Kiln with the reusable fired mold + 4 Branches + 4 Flint. After 15 seconds it outputs one gravity-affected Burnt Branch Block.
3. Clay Grinder or Wooden Grinder converts 1 Burnt Branch Block → 2 Burnt Branch Slabs; 1 slab → 8 Burnt Branches; 2 slabs → 1 block.
4. Grind 32 Mortar Bricks into a Mortar Kiln Base and finish it with Advanced Kiln Glass. The Mortar Kiln uses ordinary Clay Kiln processing rules but one loaded fuel cycle lasts 15 minutes. Intact pickup requires the Advanced Flint Multitool.
5. Grind 16 Woven Wood into a Wooden Grinder Base. Separately grind 8 Very Refined Stone → 1 Crushed Stone, then 8 Crushed Stone → 1 Stone Grindsaw. Use the Grindsaw on the base to complete the Wooden Grinder; intact pickup requires the Advanced Flint Multitool.
6. Wooden Grinder single-input mode supports all Clay Grinder recipes. Its dual-input recipes are 2 Raw Leather + 1 Sanding Paper → Ground Leather, 8 Leather + 8 Woven Wood → Woven Chest Frame, and 4 Leather + Reed Backpack → Leather Backpack.
7. Wash Ground Leather against water to receive vanilla Leather. Right-click a Mortar Chest with a Woven Chest Frame to preserve its existing contents and upgrade it into a 9-slot Woven Chest.
8. Leather Backpack is unbreakable, grants 2 armor, unlocks the full hotbar plus four main-inventory slots, and raises food exhaustion to 1.05× while equipped.
9. Flint Axe supported-log break time is 6.5 seconds. Bone Brush has 160 durability. Maximum stacks are 16 Twig, 32 Plant Tool Binding, 16 Branch, and 32 Burnt Branch.
