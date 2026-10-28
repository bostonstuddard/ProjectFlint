# Architecture

## Package boundaries

```text
dev.projectflint
├── ProjectFlint                 bootstrap only
├── client                       client-only presentation/HUD
├── config                       balancing / tuning constants
├── item                         item behavior
├── mixin                        narrow vanilla behavior interception
├── network                      typed client/server payloads
├── registry                     registry and tag definitions
├── system                       gameplay systems spanning objects
└── world                        biome/world-generation wiring
```

The entrypoint should remain boring. Feature logic belongs in a system or domain class.

## Primitive interaction pipeline

The opening world uses capability-first destruction rather than vanilla tool tiers:

1. client/server attack handling identifies whether a primitive operation is valid;
2. block destroy progress is overridden for valid primitive operations or forced to zero when the action is impossible;
3. `ServerPlayerGameModeMixin` replaces completed primitive operations before vanilla destruction/drops occur;
4. rewards use primitive-capacity insertion rather than assuming the full vanilla inventory exists.

Current primitive targets are expressed through tags:

- `projectflint:forage_bushes`
- `projectflint:grass_scrape_targets`
- `projectflint:slow_hand_clear_targets`
- `projectflint:breakable_by_hand`

Tool inheritance now resolves through `ToolCapabilities`; new Hammer, Cutter, Multitool, or Firestarter behavior should extend that table before adding a narrow site-specific check.

## Client presentation boundary

Gameplay state remains server-authoritative. `dev.projectflint.client.PrimitiveHud` renders only presentation state and synchronized progress. Alpha.3 removed the blocked crosshair marker and mouse-button instruction icon. Alpha.4 additionally keeps inadequate-tool feedback client-only: hand motion can look deliberately slow without applying a gameplay status effect.

Alpha.22.4 adds two presentation-only responsibilities: filing progress icons for the existing server-owned `StoneWorkingSystem` session, and condition labels derived from synchronized vanilla health/food values. Condition effects and jump denial remain server/common-authoritative; the HUD is never the source of gameplay truth.

## Primitive inventory boundary (alpha.3)

`PrimitiveInventorySystem` is the authoritative opening-game carrying rule:

- inventory indices `0..4` are legal storage;
- indices `5..35` are locked in survival;
- armor/equipment and offhand are outside that storage restriction;
- illegal stacks are repaired into legal slots or dropped rather than silently destroyed;
- Project Flint reward insertion uses the same five-slot policy;
- ordinary survival item pickup uses vanilla item-entity behavior while inventory destination selection is constrained to the five legal slots.

`InventoryMixin` constrains vanilla free-slot and stack-merge destination selection to the primitive inventory boundary. `SlotMixin` blocks normal menu interaction with locked storage. `InventoryMenuMixin` marks personal crafting slots unavailable. `InventoryScreenMixin` hides the recipe-book entry point and visually covers primitive-unavailable inventory areas.

Future carrying upgrades should extend this central capacity model, not patch individual menus independently.

## Stone working

`RockItem` recognizes the input gesture: a Rock in each hand. `StoneWorkingSystem` owns progress, tuning, feedback, and output. Sessions are intentionally transient during these early alphas; persisting a few seconds of partial work is not important enough to choose the long-term player-data format yet.

## Animal systems

### PassiveMobTuningSystem

Applies a stable named permanent max-health modifier to vanilla natural passive spawn categories (creature, ambient, axolotl, and aquatic groups). The modifier ID prevents stacking across reloads/chunk loads.

### AnimalRetaliationSystem

Damage to one `Animal` seeds retaliation state to nearby living animals of the exact same entity type. The tick system performs generic pursuit/contact attacks. This is infrastructure for pressure, not final ecology.

### WildWolfAggressionSystem

Untamed wolves use proactive target acquisition rather than the retaliation system. It is separate because predator aggression is a different design concept from herd defense.

Future work should introduce species profiles instead of growing these generic systems indefinitely.

## Primitive world rules (alpha.3)

`PrimitiveWorldRulesSystem` owns broad temporary ecology gates:

- fish damage immunity;
- removal of selected vanilla civilization entities;
- removal of surface/near-surface Overworld `Monster` entities while leaving underground hostiles available.

The current hostile rule runs at entity load. If profiling shows repeated surface spawn attempts are costly, it should move to an earlier spawn-validity interception.


## World bootstrap rules

`PrimitiveWorldSetupSystem` owns world-level opening constraints: Hard difficulty is forced/locked and world spawn is relocated, when necessary, to Forest, Flower Forest, or Meadow before players join. Plains was removed from the starter-spawn set in alpha.8.

`PrimaryLevelDataMixin` exposes Hardcore semantics through world data without intercepting normal Creative/Spectator runtime switching. World-creation UI constraints remain client-side presentation; they do not replace Minecraft's core gamemode system.

These rules are intentionally centralized so later world presets can replace bootstrap enforcement without scattering difficulty/gamemode logic through unrelated systems.

## In-world primitive processing (alpha.8)

`TwigPileBlock` is the first stateful primitive processing block. The normal `projectflint:twig` item is a `BlockItem` backed by this block, so placing a Twig creates a one-count pile while right-clicking an existing pile increments its `count` property up to 12.

A completed pile can temporarily enter `lit=true` for 200 ticks. The block itself owns the short-lived processing window; the current alpha.17+ behavior converts a clicked Twig into `Lightly Burnt Twig`, while the later permanent campfire is the first source of `Burnt Twig`. When the Twig Pile's scheduled tick fires, the lit pile is removed. This remains intentionally local state, not a general heat simulation.

Future heat/fuel systems should consume this behavior into a reusable processing abstraction rather than expanding `TwigPileBlock` into a universal furnace.


## Physical ground assembly (alpha.13)

The first post-fire material progression uses small stateful ground blocks instead of re-enabling vanilla crafting. Three deliberately narrow assembly blocks now represent loose components in the world:

- `FiberWorkPileBlock` — one or two Plant Fibers; a completed two-fiber pile can be struck with a Refined Rock to produce Crude Plant String.
- `HammerAssemblyBlock` — one or two Lightly Burnt Twigs plus an optional Rock; a complete arrangement can be struck with a Refined Rock to produce the Crude Stone Hammer.
- `BindingAssemblyBlock` — one to four Crude Reeds plus zero to two Crude Plant Strings; the completed arrangement is worked with the Crude Stone Hammer into a Plant Tool Binding.

These blocks are intentionally **physical process prototypes**, not the final generic assembly engine. They establish the player-facing interaction language first: place components, visually inspect the arrangement, then apply the required operation/tool. Once several successful processes establish the common rules, the repeated state/recipe logic should be consolidated into the roadmap's future `ProcessingRecipe` / tool-operation layer rather than prematurely designing an abstraction around one recipe.

All three blocks use no loot table; recovery is handled explicitly so the five-slot primitive carrying rules remain authoritative.

## Movement exhaustion and falling attacks (alpha.8)

`PrimitiveExhaustionContext` scopes exhaustion multipliers to the movement source currently executing on the server. `ServerPlayerMovementMixin` marks ground sprint movement and sprint jumps; `FoodDataMixin` applies 0.5x and 2.0x vanilla respectively while all unmarked exhaustion retains the established 0.72x primitive multiplier.

`PlayerAttackMixin` blocks noncreative `Player.attack` calls only while the player is airborne and descending. It does not alter Creative testing or unrelated damage sources.

## World generation

Bushes and Loose Rocks remain registered blocks backed by JSON configured/placed features. Fabric biome modifications attach those features to selected biomes. Spawn frequency and placement remain data rather than hardcoded feature construction.

`ModBlocks.ForageBushDefinition` is the source of truth connecting biome key, unique bush ID, and block. Inactive shore, arid, snowy, and swamp IDs remain registered only for old-world compatibility. Active bush placement uses `MOTION_BLOCKING_NO_LEAVES`, requires air at the target and dirt-tagged support below, and rejects water.

### Vanilla surface-structure suppression

`ChunkGeneratorMixin` intercepts the 1.21.11 `tryGenerateStructure` decision and rejects a centralized set of vanilla Overworld surface/civilization structures. This affects new generation only. Underground structures are intentionally untouched until mining/cave progression is designed.

## Progression tracking

Minecraft advancements are intentionally disabled in Project Flint as of v0.31.20-b. `ServerAdvancementManagerMixin` replaces the complete advancement reload map with an empty map, and Project Flint ships no advancement JSONs. The old `ModAdvancements` bridge remains only as a temporary no-op for source compatibility while bespoke progression events move to Flint Quests.

Flint Quests is the forward progression/quest layer. Generic obtain/break/use/interact tasks are handled by Flint Quests itself, while Project Flint exposes custom workstation/process events through `FlintQuestsBridge`.

## Mixins

Mixins are now used where Fabric events do not cleanly express the required vanilla behavior:

- destroy-speed/capability interception;
- completed primitive block transformations;
- inventory slot/crafting UI restrictions;
- vanilla structure-generation rejection;
- complete Minecraft advancement suppression while Flint Quests replaces progression tracking.

The rule remains: use public Fabric/Minecraft APIs for normal extension points; use a focused mixin when the vanilla behavior itself must change.

## Expected future systems

```text
ToolOperationRegistry
MaterialDefinition / MaterialState
ProcessingRecipe / in-world transformation
CarryingCapacity / portable storage upgrades
SpeciesBehaviorProfile
HeatNetwork / HeatContainer
DryingSystem
Construction/Multiblock validation
MechanicalNetwork
PlayerKnowledge (only if knowledge gating is actually needed)
```


## Primitive stockpiles and refinement (alpha.14)

Alpha.14 extends physical ground interaction with recoverable stockpiles and a second stone-processing state.

- `RockItem` is a `BlockItem` backed by `RockPileBlock`, allowing ordinary Rocks to be physically stockpiled without changing their inventory presentation.
- `RockPileBlock` stores a 1–12 count in block state and returns one Rock at a time only when it can fit in the five-slot primitive inventory.
- Right-clicking `Crushed Stone` with a Rock transforms the held Rock into `Very Refined Stone`; the exhausted Crushed Stone remains in place.
- `VeryRefinedStonePileBlock` provides the same reversible physical-storage pattern for 1–8 Very Refined Stone.
- `HarvestingToolAssemblyBlock` stages two Very Refined Stone, one Plant Tool Binding, and one Lightly Burnt Twig. The binding used to begin the assembly is the full binding cost; a second binding is no longer consumed. A completed assembly is worked with the Crude Stone Hammer.
- The player-facing result is **Crude Stone Cutters**. The internal registry ID remains `projectflint:crude_harvesting_tool` and the Java class remains `CrudeHarvestingToolItem` so the alpha.14 hardening patch does not needlessly churn save/resource identifiers.
- The hammer and cutters use standard Minecraft item attribute components rather than a custom combat subsystem. The hammer resolves to 2.0 total attack damage / 0.8 attack speed; the cutters resolve to 1.0 total attack damage / 1.2 attack speed.
- Durability is native item durability: 24 uses for the hammer and 48 for the cutters. `ServerPlayerGameModeMixin` charges cutter durability for Short Grass/flower harvesting and hammer durability for its existing processing operations.
- The cutters convert Short Grass directly into Crude Plant String and flower-tag blocks into Flower Petals without restoring vanilla drops.
- All alpha.13/alpha.14 ground assemblies route non-matching clicks and primitive attack clicks into component recovery, preventing placed progression materials from becoming trapped.

The same principle remains in force: a placed process should be inspectable, reversible before completion, and should never require vanilla crafting-grid semantics.


## Interaction consistency and stability (alpha.15)

Alpha.15 supersedes several alpha.14 implementation details without changing persistent IDs.

- `RockItem` is now a normal `Item`; ordinary Rocks no longer delegate to `RockPileBlock` placement. `RockPileBlock` remains registered only for compatibility with existing test worlds.
- `BlockStateBaseMixin#canSurvive` is the shared support boundary for primitive ground objects. New piles/assemblies should join that predicate rather than inventing independent floating-placement checks.
- `PrimitiveHud` mirrors the server destroy-progress rules for hammer and cutters targets so long tool operations use the same readable meter as hand scraping/clearing.
- `InventoryMixin` preserves vanilla item-entity pickup semantics while restricting free-slot and stack-merge destinations to the five-slot primitive inventory.
- `ServerExplosionMixin` suppresses Creeper block loot by replacing the `BlockState#onExplosionHit` drop consumer with a no-op consumer. It no longer touches `ServerExplosion`'s private `StackCollector` helper.
- `ServerPlayerGameModeMixin` treats Short Grass, Leaf Litter, and flowers as the first consistent cutters target family; two-block flowers are removed as a pair before the custom Flower Petals reward is granted.
- Advancement layout is intentionally branched by capability family and descriptions are treated as player instructions, not flavor-only labels.


## Heat, hand-working, and earthworks expansion (alpha.16)

Alpha.16 deliberately extends existing interaction systems rather than introducing vanilla-grid recipes.

### Generalized hand-working progress

`StoneWorkingSystem` now tracks a small `Process` family rather than only Rock → Refined Rock. The existing per-player progress channel is reused for:

- Rock → Refined Rock: 8 strikes / 68% success;
- Very Refined Stone → Campfire Stone: 16 strikes / 100% success;
- Crude Stone Hammer block-harvest of Sugar Cane remains the bootstrap Crude Reed route required before Cutters can be assembled;
- Sugar Cane + offhand Crude Stone Cutters → 3 Crude Reeds: 8 strikes / 100%;
- Burnt Twig + offhand Crude Stone Hammer → 2 Lightly Burnt Twigs: 8 strikes / 100%;
- 4 Flint Chips + offhand Crude Stone Hammer → vanilla Flint: 8 strikes / 100%.

`PrimitiveProgressPayload` carries the process kind and `PrimitiveHud` resolves it to the correct two item icons. Switching to a different hand-working process resets the active progress instead of allowing progress from one recipe to leak into another. This is still intentionally small and code-defined; a data-driven processing registry remains a future architecture target once more processes establish the stable schema.

### Permanent primitive campfire

`PrimitiveCampfireBlock` is a persistent frame, not a disposable furnace:

- the block begins as one placed Campfire Stone;
- block state tracks 1–12 stones, 0–12 Burnt Twigs, and `lit`;
- a complete 12/12 frame lights for 600 ticks when the player uses exactly 8 Flower Petals in the main hand with at least 4 Twigs in the offhand; only the petals are consumed;
- the scheduled tick clears `lit` but does **not** remove the frame, making relighting part of normal use;
- a lit frame performs explicit single-item transformations (Twig → Burnt Twig; Branch → Burnt Branch);
- unlit components are recoverable through the five-slot primitive inventory policy.

The original `TwigPileBlock` remains the bootstrap fire source. Its 10-second disposable flame now produces Burnt Twig rather than Lightly Burnt Twig; the hammer-mediated Burnt Twig → Lightly Burnt Twig process keeps the earlier hammer recipe reachable while supplying the material needed to build the permanent campfire.

This is still not a general heat network. Temperature, fuel duration per material, queued recipes, and heat propagation should remain outside `PrimitiveCampfireBlock` until later technology actually needs them.

### Crude Multitool assembly and operations

`MultitoolAssemblyBlock` stages exactly 2 Burnt Twigs, 4 Plant Tool Bindings, and 4 Very Refined Stone. The first component is placed only by sneak-right-clicking the top of supported ground; later ingredients mutate the same block state. Before completion, components are reversible. Completion removes the assembly and grants `Crude Multitool`.

The multitool is currently an operation carrier rather than a vanilla tiered mining tool. `BlockStateBaseMixin#getDestroyProgress` and `ServerPlayerGameModeMixin#destroyBlock` together define its capabilities:

- `#minecraft:leaves`: normal vanilla destroy progress, custom output 4 Twigs + 1 Branch;
- Dirt / Grass Block: 25% of vanilla destroy progress, custom output 1 Dirt Pile;
- Gravel: 25% of vanilla destroy progress, custom output 1 Flint Chip.

Each completed operation consumes one multitool durability. Vanilla drops are suppressed for these transformed targets.

### Earth material state

`DirtPileBlock` is a physical 1–9 count pile. A full nine-count pile struck with the Crude Stone Hammer becomes a `Loose Dirt` item. `Loose Dirt` is registered as a `ColoredFallingBlock`, giving it Sand/Gravel-style gravity instead of behaving like an ordinary static terrain cube. It has no vanilla loot table; Project Flint remains responsible for progression outputs rather than allowing accidental vanilla-drop shortcuts.

### Placed component texture rule

Inventory item textures and block-atlas textures are treated as separate render domains. The alpha.16 Plant Tool Binding fix gives `HarvestingToolAssemblyBlock` a dedicated `projectflint:block/plant_tool_binding_ground` texture reference rather than pointing a block model at an item-atlas-only path. New ground assemblies should follow this pattern.


## Primitive break permission gate and campfire stone rendering (alpha.18)

`PlayerBlockBreakEvents.BEFORE` remains the survival deny-by-default boundary, but it no longer returns a blanket `false` for every non-Creative break. It now explicitly permits only the already-supported primitive break operations (forage bushes with Refined Rock/Cutters, primitive grass/foliage work, Hammer Stone/Sugar Cane work, Cutters vegetation/Sugar Cane work, Multitool leaves/earth/gravel work, and normal Sweet Berry Bush breaking). The corresponding `ServerPlayerGameModeMixin` still owns custom outputs and durability. This removes dependence on injection/callback ordering while preserving the terrain capability lock.

The permanent campfire remains a single `PrimitiveCampfireBlock` with multipart state-driven geometry. The small Campfire Stone cuboids now sample `textures/block/campfire_stone_block.png`, an opaque block-atlas derivative of the already-approved Campfire Stone sprite. The item icon is unchanged. This keeps the multipart geometry visible at stone-only states without changing campfire state, collision, recipes, or fuel behavior.

## Early-fire restoration and vanilla item-drop boundary (alpha.17)

Alpha.17 repairs two alpha.16 boundaries that had become architecture problems rather than isolated content bugs.

### Fire-material progression

`TwigPileBlock` once again owns the **early** heat transformation: its disposable 10-second flame converts Twig into `LIGHTLY_BURNT_TWIG`. `PrimitiveCampfireBlock` owns the **later** heat tier: its persistent flame is the first source of `BURNT_TWIG` and can also char Branches. The permanent frame therefore accepts 12 Lightly Burnt Twigs rather than requiring its own later output. Burnt Twig → 2 Lightly Burnt Twigs remains a hammer hand-working route, but is now a later conversion rather than the only way to recover the early-game material.

This restores a one-way capability graph: temporary fire → Lightly Burnt Twig → early tools/permanent campfire → Burnt Twig → later assemblies. New fire content should preserve that tier boundary.

### Vanilla dropped-item boundary

Project Flint no longer replaces `ItemEntity#playerTouch`. Vanilla owns dropped-item spawning, ground rendering, velocity, pickup delay, merging, sounds, statistics, and the pickup transaction. `InventoryMixin` instead constrains `Inventory#getFreeSlot` and `Inventory#getSlotWithRemainingSpace` for noncreative Project Flint players so vanilla insertion can choose only the first five hotbar slots (plus offhand merge where vanilla permits it).

This is the preferred boundary going forward: **limit storage at inventory-slot selection, not by replacing world-item behavior**. It keeps the primitive carrying rule while avoiding divergence from vanilla item entities.

### Sugar Cane frequency supplement

`ModWorldgen` detects biomes that already contain each vanilla Sugar Cane placed feature and adds a Project Flint copy at half the vanilla attempt frequency. The custom features still reference `minecraft:patch_sugar_cane`; they do not create a second configured plant implementation or add cane to biome families that lacked it. Because this is chunk generation, the increase applies only to newly generated terrain.

### Water pressure

`ServerPlayerMovementMixin` marks vanilla swimming movement as its own exhaustion context, using a 3× multiplier over Project Flint's prior swimming movement rate. `SquidMixin` adds a narrow territorial behavior: server-side Squids acquire only survival, non-spectator players currently in water, chase within the configured range, and attack only while both target and Squid remain in water. This does not replace the Squid entity type or create a general hostile-water-mob framework.

### Sweet Berry Bush interaction

Sweet Berry Bush destruction is explicitly allowed through the primitive deny-by-default boundary. Right-click harvesting is separately gated: Crude Stone Cutters pass through to vanilla berry harvesting; empty hand and other items/tools are denied. This keeps “can break” and “can harvest renewably” as distinct capabilities.


## Alpha.19 — Tiered primitive tools and clay heat infrastructure

Alpha.19 introduces shared capability predicates in `ModItems`: `isHammerLike`, `isCutterLike`, `isMultitool`, and Flint-tier checks. Existing assembly/harvest systems consume those predicates so the Crude and Flint Multitools inherit supported earlier-tool operations instead of duplicating every call site. `BlockStateBaseMixin` remains responsible for tier-specific work progress; `ServerPlayerGameModeMixin` owns completion, output, and durability transactions.

`FlintToolAssemblyBlock` is a reversible shared physical recipe surface for both Flint Firestarter (8 Branches + 2 Flint) and Flint Multitool (2 Burnt Branches + 2 Flint). Native item durability backs the Firestarter's 32-use ignition budget.

Clay processing is a state chain rather than a crafting-grid tree: `WovenBowlAssemblyBlock` creates the first vessel; bowl item stages record clay fill; `ClayFiringPileBlock` performs the four-Twig open firing; `FiredClayPileBlock` compacts four Fired Clay; and `CrudeClayKilnBlock`/`BakedClayKilnBlock` model the movable-to-permanent kiln transition. The finished kiln is a real two-block structure using `KilnTopFormBlock` and `BakedKilnTopBlock`.

As of alpha.21.7, the baked kiln is no longer deny-break. The kiln base uses normal survival breaking and a fixed salvage loot table yielding 2 Fired Clay; the Clay Grinder similarly salvages 4 Super Heated Clay. The upper baked-kiln block remains structural and disappears without duplicate salvage when the lower kiln is removed.


## Alpha.21.7 — World-drop and salvage boundary

Completed primitive **block-breaking** operations no longer route their outputs through `PrimitiveInteractionSystem.giveOrDrop`. `ServerPlayerGameModeMixin` now uses `Block.popResource(...)` at the operation's block position, restoring the normal Minecraft expectation that mined/harvested material appears in-world and is then picked up through vanilla item-entity behavior. Interaction/crafting completions can still intentionally use direct primitive-inventory insertion where that is part of the interaction rather than block breaking.

`BakedClayKilnBlock` and `ClayGrinderBlock` use fixed loot tables for salvage instead of returning their workstation block items. This makes support-loss destruction and ordinary vanilla destruction share the same 2 Fired Clay / 4 Super Heated Clay salvage behavior. The existing Creeper drop-suppression redirect contains an explicit exception for these two blocks so their requested salvage remains consistent even through that destruction path.

The baked-kiln block entity ejects finished cooking output using the lower kiln's `FACING` property: the result spawns just outside the front opening and receives a small outward motion vector.

## Alpha.22.6 — Wearable capacity, woven assembly, and five-slot storage

`PrimitiveInventorySystem.allowedHotbarSlots(Player)` is the single carrying-capacity query used by server enforcement, inventory insertion, selection clamping, and the client lock overlay. It returns nine only while a Reed Backpack is equipped in the chest slot. `WovenGearAssemblyBlock` owns the two physical fan/binding/bone assembly formulas. `MortarChestBlockEntity` owns a persistent five-slot inventory and reuses the vanilla five-slot hopper menu while retaining Project Flint's independent block identity.

`SleepingBagBlock` now extends vanilla `BedBlock`, retaining Project Flint color/salvage rules while delegating sleep occupancy, animation, wake-up, and night advancement to Minecraft's bed lifecycle. `BoneBrushItem` owns the sustained 70-tick Sand brush transaction; `PrimitiveProcessingRegistry` remains the sole Campfire/Kiln boundary for Cooked Calamari and kiln-only Glass Shards.


## Alpha.22 — Primitive systems consolidation boundary

### Clay Grinder
The Clay Grinder is now a Project Flint-owned workstation from end to end. `ClayGrinderMenu` extends `AbstractContainerMenu`, `ClayGrinderScreen` renders the current placeholder interface without adding a new texture asset, `ModMenus` owns the menu type, and `ModRecipes` owns `projectflint:clay_grinding`. `ClayGrinderRecipe` is independent of `StonecutterRecipe`. No alpha.22 recipe JSONs are intentionally present.

### Timed processing
`AbstractPrimitiveProcessorBlockEntity` owns the one-input/timer/one-output state machine and persistence. Campfire and Kiln block entities provide only station identity, active-state detection, and ejection geometry. `PrimitiveProcessingRegistry` is the sole recipe/capability table for these two heat processors.

### Tool capabilities
`ToolCapabilities` is the authoritative Hammer/Cutter/Multitool inheritance boundary. Compatibility helpers in `ModItems` delegate to it so older call sites can be migrated without changing behavior. Yield-specific Flint/Crude tuning remains in `GameplayTuning` and completion handlers.

### Developer contract
`docs/PRIMITIVE_PROGRESSION_MATRIX.md` records the current progression and is required to change whenever a release modifies tool inheritance, workstation processing, salvage, stack sizes, timings, or material acquisition.

## Alpha.22.3 — Animal loot and first grinder-recipe follow-up

`PrimitiveAnimalDropSystem` is the narrow ownership boundary for primitive animal-resource substitutions. It operates at loot-generation time rather than replacing entity classes: Sheep wool stacks are converted to Project Flint Wool, while approved livestock/predator deaths add the generic Animal Bones resource. Behavior reactions remain separate: `AnimalRetaliationSystem` excludes Horses so vanilla panic remains authoritative, and `SquidMixin` removes only the native flee goal while retaining Project Flint's territorial pursuit.

The custom Clay Grinder recipe format now includes an explicit `input_count`. This extends the alpha.22 Project Flint-owned menu/recipe boundary without reintroducing Stonecutter inheritance. The first recipe exercises that count with 4 Sugarcane Rolls → 1 Sanding Paper.

Visible item renames remain localization-only when an existing registry ID already exists in test worlds. `plant_seeds`, `unfired_woven_bowl`, and `unfired_clay_ball` therefore remain stable IDs even though their current names are Roasted Seeds, Unfired Clay Bowl, and Unfired Clay.

Clay Grinding Base salvage is now part of the same finished-workstation capability boundary as the Grinder: Flint Multitool is required, world salvage is 4 Super Heated Clay, and manual dismantling additionally returns already-inserted Very Refined Stone.

## Alpha.22.4 — Filing, placement, rest, and survival boundaries

`StoneWorkingSystem.Process` remains the single hand-working progress owner. Flower/Sanding Paper, Bone/Sanding Paper, Filed Bone/Flint, and Wool/Sanding Paper are explicit process entries with synchronized HUD kinds; `PrimitiveInteractionSystem` owns the atomic input/output transaction. Reusable tools are represented by deliberately not consuming the offhand Sanding Paper. Fire ignition resolves through `ToolCapabilities.FIRESTARTER`, so the Bone Firestarter inherits existing ignition sites without copied checks.

`PlacedItemSystem` owns the new general world-placement interaction. The client only sends a unit `PlaceInWorldPayload` from a configurable key. The server repeats the raycast/range/support/space checks, removes one selected item, and creates a tagged vanilla `Interaction` holder with a flat `ItemDisplay` passenger. Only right-click reclaim is accepted; attacks fail and a periodic support audit converts unsupported displays into normal `ItemEntity` drops. Placed displays are intentionally runtime entities rather than new per-item blocks.

The Crude Sleeping Bag is a narrow supported block assembled by `WovenWoolItem`. Its block state owns horizontal facing and vanilla `DyeColor`; the models reference vanilla wool textures instead of creating modified texture files. Both explicit break handling and support-loss handling return the fixed 2-Woven-Wool salvage. `ServerPlayerSleepMixin` rewards only a fully rested player being woken into daytime.

`PrimitiveConditionSystem` refreshes short server-side effects from health and food thresholds without removing unrelated potion effects. `PlayerJumpMixin` enforces the Hungry no-jump rule on the shared `LivingEntity#jumpFromGround` boundary. Dying and Hungry are counted as independent Weakness sources, allowing the combined state to reach Weakness II while the HUD simply mirrors the conditions.

Mortar uses existing station boundaries: the Clay Grinder owns Super Heated Clay → Clay Mortar, water interaction owns Clay Mortar → Unfired Mortar Bricks, and `PrimitiveProcessingRegistry` marks Unfired Mortar Brick → Mortar Bricks as kiln-only. `ModBlocks.MORTAR_BRICKS` is a normal non-falling block; the primitive break gate requires Hammer capability and completion produces a world item entity.
## Alpha.22.4.1 — Compile compatibility boundary

The completed-night reward continues to live in `ServerPlayerSleepMixin`, but its daytime predicate uses the Minecraft 1.21.11 mapped `Level.isBrightOutside()` API. Sleeping Bag and Mortar Bricks breaking authorization remains exclusively in the block-breaking gate; the generic block-use handler does not duplicate those guards.

## Alpha.22.5 — Linked rest, plant harvest, and raw-food boundaries

`SleepingBagBlock` now owns a true two-position lifecycle using vanilla `BedBlock.PART` and `BedBlock.OCCUPIED` properties. `BedItem` placement and the Woven Wool construction route both validate supported foot/head positions. Pair-aware dye, removal, support loss, sleep position, and fixed salvage prevent independent-half state. The block tag `minecraft:beds` lets vanilla sleep bookkeeping treat it as a bed target while Project Flint keeps its custom geometry and recovery rule.

`ToolCapabilities.canDirectHarvestPlant` is the one Flint whole-plant predicate. Destroy speed, the survival break authorization callback, and completion/drop handling all call it; held-flower hand-working deliberately remains in `StoneWorkingSystem` as the separate petal process.

Raw food uses a data boundary instead of item-by-item consume hooks. `projectflint:raw_meats` lists vanilla raw meats/fish plus Raw Calamari, and `RawMeatConsumptionMixin` applies one shared server-authoritative effect profile on completed consumption. `PrimitiveAnimalDropSystem` extends its existing loot boundary to Squid meat and bones without modifying the entity class.

Ground assembly models do not reference item-atlas textures. Burnt Branch and Flint layers now use dedicated byte-identical block-atlas texture targets, matching the existing placed-component texture rule.
## Alpha.22.6.1 — Block-entity codec and earth shaping

`MortarChestBlock` now declares the required Minecraft 1.21.11 `MapCodec`, keeping its block-entity behavior compatible with the mapped `BaseEntityBlock` contract. Loose Dirt slab shaping remains in `PrimitiveInteractionSystem`, queries the centralized Multitool capability, and performs its block replacement and extra world drop only on the logical server. Custom slab harvesting remains in the established block-breaking mixins so primitive authorization, tool durability, and world-drop behavior stay consistent with full Loose Dirt.

## Alpha.23: Advanced kiln and cast-tool progression

The Advanced Kiln is intentionally a small multiblock rather than another single-block processor. `MortarBricksBlock` recognizes a front-facing 2×2×2 mortar cube and converts it into seven casing blocks plus one `AdvancedKilnControllerBlock`. Only the controller owns `AdvancedKilnBlockEntity` state. The entity stores the reusable fired mold, branch/flint/clay counts, and a 200-tick process timer. This keeps multiblock persistence and recipe state centralized rather than duplicating inventories across eight blocks.

`AdvancedToolAssemblyBlock` follows the existing physical-ground-assembly decision: the cast piece chooses the assembly type, two Burnt Branches complete it, and empty-hand interaction reverses an unfinished assembly.

The former `PrimitiveItemAdvancementSystem` reconciliation pass was removed in v0.31.20-b. Legacy process-specific `ModAdvancements` calls are temporary no-ops; equivalent progression should be authored through Flint Quests generic tasks or explicit Project Flint custom-event hooks.

On the client, `ReedBackpackLayer` renders the equipped backpack item model from `AvatarRenderState.chestEquipment`, so the gameplay item remains a normal equippable item instead of introducing a second armor/equipment asset pipeline.
## Alpha.23.2: Stateful kiln casing and vanilla-skinned container UI

`AdvancedKilnControllerBlock` is the only block entity owner in the 2×2×2 kiln. `AdvancedKilnCasingBlock.Part` stores each non-controller block's offset from that controller, allowing every casing position to resolve the same inventory and allowing `AdvancedKilnMultiblock` to dismantle the structure from any of its eight blocks. `AdvancedKilnBlockEntity` now extends `BaseContainerBlockEntity`; `AdvancedKilnMenu` exposes the four material slots plus output and synchronizes the 200-tick process state to `AdvancedKilnScreen`.

`MortarChestMenu` remains Project Flint-owned because its inventory must contain exactly seven slots, which does not match vanilla ChestMenu's 9-slot row contract. `MortarChestScreen` no longer paints a custom Project Flint interface; it blits Minecraft's generic-container texture and lays the seven real slots into one centered row.

Equipped Reed Backpacks continue to avoid a second gameplay item. `ReedBackpackLayer` copies the equipped stack only for rendering, overrides that render copy's `ITEM_MODEL` component with `projectflint:reed_backpack_worn`, and submits the dedicated 3D model against the avatar body transform.



## Alpha.23.5: grinder shaping, pottery, and recoverable progression blocks

`AdvancedKilnMenu` treats the block entity's synchronized process value as remaining ticks, while `AdvancedKilnScreen` renders elapsed progress as a conventional left-to-right bar. The display reaches full on the final processing tick and returns to empty when the kiln becomes idle.

Clay Grinder shaping now owns slab production that previously lived in hand-working: a stripped log becomes two matching wooden slabs, one Mortar Bricks block becomes two Mortar Brick Slabs, and one Crude Clay Block becomes two Crude Clay Slabs. The old axe-slab hand-working process and HUD payload path are removed. Flint Axe break authorization covers its supported stripped logs and all wooden slabs so those outputs remain recoverable.

The pottery extension follows the existing mold boundary: Unfired Clay Mold Base -> Unfired Clay Pot Mold in the grinder -> Fired Clay Pot Mold in the baked kiln -> Clay Pot in the Advanced Kiln. The fired mold remains installed like the other Advanced Kiln molds. `Clay Pot` uses a two-step fill state, so two vanilla Clay blocks complete one Unfired Clay Ball instead of the bowl's four-block cycle.

Sleeping Bag color persistence is item-component based. Advanced Flint Multitool recovery copies the block's `DyeColor` into the returned stack's `DataComponents.BASE_COLOR`, and placement reads the same component back into the two-block sleeping-bag state. Advanced Flint Multitool recovery also returns the Baked Kiln and Clay Grinder as intact workstation items; earlier Flint-tier dismantling retains the established salvage outputs.

## 0.25.0 processed-wood path

Processed wood deliberately spans both world interaction and Clay Grinder recipes. `PrimitiveInteractionSystem` owns the placed-slab Sanding Paper and Woven Fan transformations and preserves `SlabBlock.TYPE`. `ClayGrinderRecipe` remains single-input/count-based, so the count-compression and reverse steps are data recipes rather than hard-coded menu branches. The three processed slab blocks and final Woven Wood block are registry-owned Project Flint blocks with manual Flint Axe retrieval enforced by the same deny-by-default mining layer as the rest of the primitive progression.


## 0.26-a: upgraded primitive infrastructure

- `AdvancedKilnBlockEntity` now owns recipe-duration state. Established casting recipes use 1200 ticks; the reusable Fired Clay Block Mold selects a separate 300-tick Branch/Flint recipe, and Fired Clay Pot Mold completion consumes the mold.
- `MortarKilnBlock` intentionally reuses `BakedClayKilnBlockEntity` and the shared `PrimitiveProcessor.KILN` recipe registry so it behaves like the existing completed clay kiln while extending a single fuel cycle to 15 minutes. Intact recovery is gated to `ADVANCED_FLINT_MULTITOOL`.
- `WoodenGrinderMenu` is a Project Flint-owned two-input menu. Single-input operation discovers the same `projectflint:clay_grinding` recipes as the Clay Grinder; dual-input recipes remain explicitly owned by the Wooden Grinder menu.
- `MortarChestBlockEntity` now owns 3 slots and `WovenChestBlockEntity` owns 9 slots. Mortar Chest → Woven Chest copies the existing three stored stacks into the first three Woven Chest slots before replacing the block entity.
- `PrimitiveInventorySystem` treats the Leather Backpack as a carrying-capacity tier: slots 0–12 are usable while equipped. `PlayerExhaustionMixin` applies the requested 1.05 multiplier to food exhaustion only while that chest item is worn.
- New workstation/utility recovery remains capability-gated in both destroy-progress and server-break interception: Mortar Kiln/Wooden Grinder require the Advanced Flint Multitool, while Woven Chest requires the Flint Axe.


## 0.27.0-a: storage, ground stability, and workstation orientation

- Mortar Chest capacity is 3 slots; Woven Chest capacity is 9 slots.
- Raw Hide/Ground Hide registry ids were renamed to Raw Leather/Ground Leather.
- Ground crafting/assembly blocks use `SupportedGroundBlock` and break when their supporting block disappears.
- Loose Dirt, Crude Clay, and Burnt Branch slab variants use `FallingSlabBlock` so their gravity matches the parent material.
- Clay Grinder and Wooden Grinder persist a horizontal facing state and render in all four cardinal orientations.
- Every Advanced Kiln casing block forwards interaction to the shared controller GUI.
- Ground assembly models reuse canonical item textures instead of maintaining duplicate placed-only PNGs.


## Client lightmap boundary (v0.28.7-a)

Minecraft 1.21.11 renders its lightmap through the `LIGHTMAP` render pipeline and a uniform buffer. Project Flint therefore owns a resource-level lightmap fragment shader override rather than uploading pixels into Minecraft's GPU texture after the render pass. The override preserves normal sky/block-light shaping and Night Vision/Conduit visibility while removing the artificial minimum-light floor, so genuinely unlit texels can remain black.

The retired `ProjectFlintHardcoreDarkness`/`LightTextureMixin` upload path must not be restored: the vanilla lightmap texture is not created as a `COPY_DST` destination in 1.21.11.

## Animated kiln face resources (v0.28.7.1-a)

Kiln animation remains data/resource driven. `MortarKilnBlock.LIT` chooses between the normal and active one-block models. The Advanced Kiln already synchronizes `ACTIVE` across the controller and casing blocks; active blockstate variants now select dedicated models that reuse the existing geometry while swapping only the visible front and/or top texture references to the animated strips.

The animation PNGs are exact user-provided assets. Minecraft's `.png.mcmeta` animation metadata supplies frame timing; Project Flint does not generate, edit, recolor, crop, resize, or composite the texture pixels.


## v0.28.7.2-a environmental presentation and processor tuning

Project Flint keeps hardcore-night presentation in the lightmap rather than view-distance fog. The lightmap fragment-shader override controls zero-light and night sky contribution while preserving block-light sources, and Terrified owns no fog/Blindness presentation at all. There is intentionally no Project Flint atmospheric fog environment: campfires, torches, lit kilns, and illuminated structures must remain visible at distance for navigation.

Wet-state escalation remains server authoritative. `PrimitiveEnvironmentEffectSystem` tracks continuous exposure and swaps Soaked to Drenched after 1800 ticks. The custom Drenched effect contains Soaked-equivalent movement/exhaustion plus poison-equivalent periodic damage; `FoodDataMixin` blocks natural healing while Drenched is active.

Primitive cooking recipes keep one base duration. `AbstractPrimitiveProcessorBlockEntity` divides that base by a station-specific speed multiplier: Baked Kiln 1.0x, Mortar Kiln 1.25x, permanent Campfire 0.90x. This avoids parallel recipe tables for identical transformations.

Worldgen Loose Rock and Rock Stockpile are distinct rendering paths. World generation resolves `blockstates/loose_rock.json` -> `models/block/loose_rock.json`; stockpiled player Rocks resolve the separate `rock_pile_*.json` family. Fixes to one must not be assumed to affect the other.

Brown Bear continues to use the canonical user texture unchanged. A dedicated `BrownBearModel` copies vanilla Polar Bear geometry/animation structure but corrects the muzzle UV origin for that existing atlas.

## v0.28.7.3-a rendering/authority corrections

Brown Bear rendering is intentionally identical in geometry/layer ownership to vanilla Polar Bear rendering. `BrownBearRenderer` uses `ModelLayers.POLAR_BEAR` and `ModelLayers.POLAR_BEAR_BABY`; Project Flint no longer maintains a parallel `BrownBearModel` UV definition.

Outdoor hardcore-night presentation is lightmap-only. Unlit terrain falls toward near-blackness as sky contribution hardens at night, but Project Flint does not impose a short artificial fog cutoff. This preserves the gameplay role of distant block-light landmarks while still making travel through unlit terrain dangerous.

Worldgen Loose Rock, held Rock, and Rock Stockpile are separate resource domains. `models/block/loose_rock.json` must never depend on the held item's `block/placed/rock` mirror. The worldgen block currently uses `minecraft:block/stone` as a resource-pack-aware block surface; the held item continues through `models/item/rock.json`, while stockpiles keep the `rock_pile_*.json` family.

Primitive mining requires two permissions: the destroy-progress mixin and the attack/break authority gate in `PrimitiveInteractionSystem`. A custom earth block such as Loose Soil must be present in both or it can appear to have valid mining speed while attacks are still denied before completion.


## Forage ecology, Sweet Berry propagation, and species model layers (v0.28.7.4-a)

Project Flint forage bushes are survival resources, not collectible foliage. `ToolCapabilities.canDirectHarvestPlant` must therefore never include `projectflint:forage_bushes`; those blocks stay routed through the explicit twig-output path in `ServerPlayerGameModeMixin`, where later cutter tiers may improve yield without exposing the bush BlockItem.

Leaf Litter is segment-aware. Its vanilla `SegmentableBlock.AMOUNT` property is treated as the physical quantity present, and the full-pile tool yield is multiplied by 1/4, 2/4, 3/4, or 4/4 with ceiling rounding.

Sweet Berry interaction is split by intent: `PrimitiveInteractionSystem` owns nondestructive right-click berry harvesting for plant-cutter-capable tools, while `ServerPlayerGameModeMixin` owns the later destructive one-seed extraction operation. `SweetBerrySeedsItem` is the propagation item and establishes the vanilla bush on Grass Block without introducing a separate custom crop block.

Black Bear and Brown Bear intentionally have separate client model layers. Their 128×64 texture sheets were authored against different dimensions and UV offsets, so sharing `ModelLayers.POLAR_BEAR` is not a valid rendering abstraction. The Project Flint model classes are adapted from the Apache-2.0 Bear Minimum source and registered independently in `ProjectFlintClient`.


## Stack-safe workpieces and ephemeral primitive light (v0.28.7.5-a)

Primitive workpiece transformations must consume explicit counts rather than replace an entire held stack. `RockItem` is the reference for chance-based one-workpiece processing: after the attempt resolves, decrement one input and separately insert the result. This remains correct if stack-size tuning changes later.

`CrudePlantTorchItem` stores burn lifetime in the native item damage value. `CrudePlantTorchBlockEntity` receives the remaining lifetime at placement and owns countdown/environment checks thereafter. The placed block is unrecoverable, light level 6, open-sky-only, rain/water-sensitive, and may seed vanilla fire beside combustible vegetation/wood. This keeps the primitive torch intentionally temporary rather than introducing a reusable lighting network.


## Navigation-preserving hardcore darkness (v0.28.7.6-a)

The temporary 6–8 block atmospheric fog experiment was removed after playtesting showed it erased distant block-light landmarks. Hardcore darkness is now intentionally lightmap-only: moon/sky contribution is strongly suppressed, while block light remains spatially visible at normal render distance. This means a distant campfire or lit structure can guide the player even when the surrounding terrain is effectively black. `ProjectFlintNightFogEnvironment` and `FogRendererMixin` are retired and removed by the release cleanup BAT.


## Zero-floor navigation darkness (v0.28.7.7-a)

Night darkness is a lightmap concern, not a view-distance concern. The v0.28.7.7-a shader allows midnight sky illumination to reach true zero again while deliberately leaving Project Flint night fog disabled. This preserves the core navigation contract: unlit terrain can be black, but actual block-light sources remain visible at range. Future darkness changes must not reintroduce a short fog cutoff or a positive full-night sky-light floor.

Primitive harvesting now also treats Pumpkins as a Flint Axe operation. Permission (`PrimitiveInteractionSystem`), destroy progress (`BlockStateBaseMixin`), and completion/drop transformation (`ServerPlayerGameModeMixin`) must stay synchronized for primitive-gated blocks. Vanilla Sweet Berries are prevented from invoking their BlockItem planting path when a bush could be placed; the custom Sweet Berry Seeds remain the propagation authority.


## Gourd harvesting and carried flame rules (v0.28.7.8-a)

Pumpkin/Melon progression follows the existing primitive destruction pipeline. `BlockStateBaseMixin` supplies allowed destroy progress, `PrimitiveInteractionSystem` admits only the intended tool tiers, and `ServerPlayerGameModeMixin` atomically replaces the block with the requested single output. Flint Axe is the propagation path (one seed); Flint Plant Cutters and Advanced Flint Multitool are the food path (one edible slice).

Crude Plant Torch environmental rules are intentionally split by state. `CrudePlantTorchItem` owns carried lifetime, carrier damage, and water/rain extinguishing. `CrudePlantTorchBlockEntity` owns placed lifetime, direct-sky requirement, rain/water extinguishing, and adjacent ignition. A roof therefore does not destroy a torch merely because it is in inventory, but a placed torch still requires sky access.


## Wattle-and-Daub structural construction (v0.28.17-a)

`WattledDaubBricksBlock` is the first Project Flint building block with explicit structural support rather than ordinary Minecraft infinite stacking. A founded vertical column may contain at most four contiguous bricks. The top of an exact four-high column becomes a load-bearing anchor that can support connected Wattled Daub ceiling bricks up to two blocks horizontally in each cardinal direction. Unsupported bricks schedule a gravity check and fall; impossible loads that cannot fall cleanly crumble instead of remaining in an invalid structure.

The height constant is deliberately centralized as `MAX_COLUMN_HEIGHT = 4`, and the same value is used both for vertical survival and load-bearing ceiling anchors. This avoids the former design risk where a height change could update placement but leave roof support on the old requirement.

`PrimitiveInteractionSystem` also rejects unrelated BlockItem placement directly above Wattled Daub Bricks. This is part of the structural rule, not a cosmetic restriction: spacer blocks must not allow `brick -> dirt -> brick` towers to bypass the four-block cap.

The material-processing chain remains split by responsibility: deterministic handworking belongs to `StoneWorkingSystem`, water/leaf transformations belong to `PrimitiveInteractionSystem`, and heat conversion belongs to the shared primitive processing registry (plus the Advanced Kiln process path).

## Wattle/Daub visual hookup (v0.28.18-a)
The v0.28.17-a construction IDs remain the gameplay/API source of truth. v0.28.18-a only connects owner-supplied resource assets to those IDs. Inventory stages use generated item models, while `wattled_daub_bricks` uses a normal cube-all block model. This separation is intentional: later texture revisions should not require changing the structural support implementation.


## Wattle/Daub habitable construction expansion (v0.28.19-a)

The Wattle/Daub structure now distinguishes **structural wall cells** from **habitable flooring**. `WattledDaubBricksBlock.isWattledDaubWall` covers solid Bricks and Windows; both share gravity, four-cell vertical limits, two-block roof reach, crumble behavior, and Flint-tier intact recovery.

`WattledDaubFloorTileBlock` is deliberately not a structural wall foundation. It is a normal sturdy surface for beds, workstations, storage, and similar interior blocks, while player placement rules reject Bricks, Windows, and additional Floor Tiles directly above it. This prevents a floor layer from silently increasing the primitive wall-height budget.

`WattledDaubDoorBlock` is the one structural exception to that rule. A Door must be founded on a Floor Tile; its lower and upper halves count as two structural cells. Wall pieces above the doorway therefore remain subject to the same centralized `MAX_COLUMN_HEIGHT = 4` rule. The Door is creative-only until a later joinery technology supplies a legitimate recipe.

Project Flint BlockItem water placement is denied centrally in `PrimitiveInteractionSystem`. Resource-processing exceptions such as Dirt Pile -> Mud Pile are resolved before placement rather than allowing a temporary ground object to replace water and disappear.

## Wattle & Daub renovation and middle-wall openings (v0.28.20-a)

Wattled Daub remains a four-cell primitive structural tier. A complete four-high load-bearing column now carries connected same-height Wattled Daub roof cells up to three blocks horizontally. Structural rechecks use a short delayed tick so renovation has a practical replacement window rather than collapsing the supported section between a break and the next placement action.

`WattledDaubWindowBlock` is deliberately more constrained than the ordinary structural brick. It must sit directly on a Wattled Daub Brick and may only resolve to vertical structural layer 2 or 3. Windows therefore behave as openings inside a wall rather than substitutes for foundations, lintels, or roof cells. Adjacent Window blocks cull their shared internal render face; this is a native render-face connection and does not alter the owner's texture pixels.

The Wooden Grinder now acts as the first efficiency upgrade for Wattle & Daub production: it can make Woven Fans, Tied Wattle, Daub, and a doubled Unfired Wattle/Daub output through dual-input processing. Flint/Flint Chip conversion remains a single-input recipe available to both grinder tiers.

## Reusable wall-light infrastructure (v0.28.21-a)

`FlintTorchBlock` and `FlintTorchBlockEntity` introduce the first persistent fueled light whose validity depends on shelter structure. The block owns `FACING` and `LIT`; the block entity owns the loaded-Flint flag and remaining 40-minute burn time. One block ID is reused for lit/unlit presentation, preventing state replacement from discarding fuel data.

Placement deliberately delegates structural height to `WattledDaubBricksBlock.verticalStructuralLayer`: the fixture must attach to a **normal Wattled Daub Brick** on layer 2 or 3, never a Window. Its installation is continuously checked rather than validated only once. The same-level 3×3 safety plane permits only Air/Wattled Daub Bricks/Wattled Daub Windows, and a complete 3×3 Wattled-Daub canopy one or two cells overhead is required so both middle wall layers can operate beneath a four-high shelter. Invalidated fixtures remove themselves with no recovery drop.

Fuel remains intentionally simple: one vanilla Flint is a single 48,000-tick charge. Lighting changes only the `LIT` block state; burnout clears the loaded fuel flag and returns to the reusable unlit state. This pattern should be preferred for future long-lived fueled fixtures where the block survives fuel exhaustion.



## Player guidance and recipe discovery (v0.28.22-a; corrected v0.28.23-a)

Project Flint now treats advancements as compact progression guidance rather than exhaustive mechanic documentation. Three independent root advancements create the **Tool Era**, **Clay Era**, and **Wood Era** tabs. Each visible advancement uses a short, one-sentence instruction and redundant item-by-item milestones are intentionally omitted. The Tool Era is always available; Clay Era appears once the player reaches the Flint Multitool; Wood Era appears once the Clay Grinder is obtained.

Recipe discovery is handled by `ProjectFlintRecipeGuideScreen` and never by Minecraft's survival recipe book. The survival inventory keeps vanilla recipe-book access hidden/disabled exactly as the primitive inventory design requires. Clay Grinder, Wooden Grinder, and Advanced Kiln screens expose a compact Project Flint-styled book icon, and the same guide is globally available through the default **K** keybind. The guide browses Baked Kiln, Mortar Kiln, Clay Grinder, Wooden Grinder, and Advanced Kiln processing; Permanent Campfire is intentionally omitted because advancement guidance already teaches that early mechanic. The guide reports itself as in-game UI so its backdrop remains transparent and never requests Minecraft's one-per-frame menu blur pass. Primitive heat-processing entries come from `PrimitiveProcessingRegistry`; Wooden Grinder dual-input entries come from `WoodenGrinderRecipeRegistry`; Clay Grinder entries use `ClayGrinderRecipeGuideRegistry`, a validated client-safe mirror of the data-driven `clay_grinding` recipes because Minecraft 1.21.11 does not synchronize arbitrary custom recipe types into `ClientRecipeContainer`.


## Groundwater / aquifer foundation (v0.28.27-a)

Aquifers are now a **virtual, seed-deterministic groundwater field**. Project Flint does not place water, clay markers, hidden blocks, entities, chunk attachments, or saved-data records to represent groundwater. A 32×32 logical cell is evaluated from the world seed and cell coordinates; roughly one in seven cells contains an aquifer, with its X/Z position and representative 20–26-block depth derived from the same deterministic hash. Each aquifer represents a five-column cross footprint rather than five literal water blocks.

This design is intentionally forward-port and old-world friendly. Existing worlds do not need aquifer generation or migration: querying the same world seed immediately produces the field, including in chunks created before aquifers existed. The physical `AquiferFeature`, feature registry, and configured/placed feature JSONs from v0.28.26-a are retired.

`AquiferSystem` remains the single groundwater authority. `findNearby(...)` evaluates only logical candidate cells around the caller; `findBelow(...)` is the future Well boundary and accepts a Well at any Y above the virtual groundwater depth when its intake is over the five-column footprint (or within a requested tolerance). Aquifer existence/location depends only on seed and coordinates; biome humidity affects **yield**, not whether the aquifer exists, so hydrology locations remain stable while deserts can remain scarce and jungles/rainforests abundant.

The creative-only Aquifer Dowsing Rod visualizes this field by marking the five surface columns of nearby virtual aquifers. The item is a debugging/discovery client of `AquiferSystem`, never a source of stored aquifer state.

## True-darkness bypass boundary (v0.28.26-a)

Normal survival uses the Project Flint hardened lightmap branch. Night Vision uses Minecraft-style lightmap behavior instead, and Creative mode is adapted into that existing `NightVisionFactor` path by `LightTextureMixin`. This keeps debugging/building visibility separate from survival darkness without returning to the removed post-upload GPU lightmap rewrite.


## Finite surface water and renewable groundwater (v0.28.28-a)

Vanilla water-source regeneration is disabled through Minecraft 1.21.11's native `GameRules.WATER_SOURCE_CONVERSION` rule. `FiniteWaterSystem` forces that rule off when a Project Flint server starts, including when an older save is opened after updating. Existing rivers, lakes, oceans, waterlogged blocks, and placed source blocks are not rewritten; the only behavioral change is that neighboring source blocks no longer manufacture a replacement source when water is removed.

This keeps the water model save-compatible and avoids custom fluid replacement. Surface water is finite and drainable, while renewable water is reserved for explicit technology such as the future Well. The Well should remain backed by the v0.28.27-a virtual `AquiferSystem`, so renewable production is a property of settlement infrastructure rather than vanilla fluid geometry.


## Granular era advancement guidance (v0.28.29-a)
Project Flint advancements are a player-facing mini-guide rather than a sparse achievement list. Each meaningful progression action should usually receive its own compact advancement, while duplicate milestones for the same action should be avoided. Era root IDs use numeric prefixes (`00_tool_era`, `01_clay_era`, `02_wood_era`) solely to keep the visible tabs in progression order.

## Wood → Water manufacturing boundary (v0.28.30-a)

`SandingStationBlockEntity` converts Sanding Paper into a persistent 0–256 workstation durability reserve: each sheet contributes 32 points and each successful `SandingStationMenu` craft spends exactly one. `SandingStationRecipeRegistry` is shared with the custom recipe guide and excludes Crimson/Warped slab inputs explicitly.

Finite water transport is represented without reintroducing infinite-water behavior. `WaterBearingPotItem` carries one half-bucket and drains only literal source-water blocks; `WaterPotBlockEntity` stores two half-bucket units. The placeable Water Pot can stand alone or store a controller link when incorporated into a Lumber Processor.

`LumberProcessorSystem` is authoritative for the three-cell Chopping Block → Water Pot → Crushed Stone machine. Formation requires sturdy support below and clear working cells above all three bases. The Chopping Block entity owns output choice and the ten-second wash timer; Project Flint's existing primitive-break authority supplies the two 7.5-second cutter/finishing phases. Structural invalidation tears down the assembly, while deliberate Advanced Flint-tier dismantling removes every constituent cell and returns one `Lumber Processor` placement item.

The machine accepts stripped Overworld logs only. Nether stems are deliberately excluded from both sanding and lumber-processing paths to preserve the technology/world progression boundary.

## Lumber Processor input authority — v0.28.31-a
Lumber processing must use the physical stripped-log block above the Chopping Block as the authoritative input check. `inputArmed` is cached block-entity state for persistence/ticking, not a prerequisite for beginning the 7.5-second player interaction. This prevents placement-to-tick timing from making a valid machine temporarily or permanently unresponsive. Output selection remains changeable while an input log is staged, but locks once washing begins.


## Lumber Processor client/server mining authority (v0.28.32-a)

The staged stripped-log mining animation is client-predicted, so its `getDestroyProgress` path must not depend on unsynchronized Water Pot/controller block-entity data. Holding Flint Plant Cutters over a supported staged log supplies the 7.5-second local progress, while the server remains authoritative for machine formation, water fullness, controller state, and `startWash()` completion. This prevents client desync from making a valid processor appear unmineable without weakening server-side validation.


## Specialized plant-tool inheritance (v0.28.33-a)

Flint Plant Cutters inherit **plant-facing** Flint Multitool behavior without being promoted to the global generic `CUTTER` capability. Vegetation gates use `ModItems.isPlantCutterLike(...)`, direct-uprouting uses `ToolCapabilities.canDirectHarvestPlant(...)`, and leaf harvesting uses `ToolCapabilities.canHarvestLeaves(...)`. This keeps Tall/Short Grass, flowers, bushes, leaves, Sugar Cane, Leaf Litter, forage bushes, Sweet Berries, and gourd harvesting coherent while preventing the plant-specific tool from accidentally gaining clay/workstation interactions that happen to query the generic cutter capability.

For plant interactions, Flint Plant Cutters originally inherited the regular Flint Multitool tier in v0.28.33-a; v0.28.34-a corrects that boundary. The dedicated Plant Cutters are a progression upgrade and therefore use the **Advanced Flint Multitool plant tier** for plant speed and general vegetation yields, while preserving cutter-specific outputs such as Short Grass -> Plant Tool Binding. They still do not gain earth, hammer, stone, or workstation capabilities.

## Sanding Station UI layout (v0.28.35-a)
The Sanding Station uses the standard 176x182 Project Flint workstation footprint. Its input/output slots occupy the upper processing row and the player inventory begins at GUI Y=100, matching the Clay/Wooden Grinder spacing. Sanding reserve is presented independently from the process slots: a text row displays the exact current/max reserve and an eight-cell meter maps directly to the eight 32-durability Sanding Paper refills needed to reach the 256 maximum. Do not draw reserve text over the fill bar again.


## Sugar Cane tool specialization (v0.28.36-a)

Sugarcane Roll production is intentionally exclusive to `FLINT_PLANT_CUTTERS`. The Advanced Flint Multitool keeps Advanced Flint plant-tier harvesting speed and yields, but Sugar Cane harvested with it falls through the general plant-cutter path and returns ordinary `minecraft:sugar_cane`. This preserves the dedicated Plant Cutters as a meaningful specialization upgrade instead of letting the general-purpose multitool duplicate its processed cane output.

## Wood -> Water workstation rendering (v0.28.37-a)

The Sanding Station, Water Pot, and Lumber Processor Chopping Block now use owner-finalized textures rather than pending/null models. The Sanding Station uses the supplied sanding-board texture on its work surface with vanilla oak planks retained for its supporting faces. The Lumber Processor Chopping Block maps its supplied top, side, and bottom sprites directly. The Water Pot has a dedicated front sprite, so `WaterPotBlock` now exposes a horizontal `FACING` state and its blockstate rotates the model accordingly. This rendering property does not alter reservoir capacity, Lumber Processor linking, or saved water contents.


## Unified detailed advancement guide (v0.28.39-a)

Project Flint advancements are once again one continuous `projectflint:root` progression tree. The temporary Tool/Clay/Wood tab split is retired. The advancement UI is intentionally closer to a quest book than a conventional achievement list: nearly every meaningful progression action receives its own visible milestone, and descriptions may explain exact counts, tools, timing, success rates, or workstation actions when that information prevents the player from becoming stuck.

The custom workstation recipe guide remains the authority for browsing bulk station recipes, but it does **not** replace advancement instructions for discovering progression. Advancements should tell the player what to do next and why an upgrade matters; the recipe guide answers “what can this machine process?”

Early-game pacing constants are centralized in `GameplayTuning`: Grass scraping is roughly seven seconds, hand-scraped Grass has a 58% Plant Fiber chance, Twig Pile ignition succeeds 65% of the time, and regular Rock knapping has a three-failure pity counter that guarantees the next attempt. `PrimitiveEnvironmentEffectSystem` now waits ten continuous seconds of qualifying darkness before applying Terrified while preserving the separate 45-second damage grace.


## v0.28.41-a foliage capability regression guard
`ToolCapabilities.canHarvestLeaves(...)` must include the Crude Multitool as well as Flint Multitool, Flint Plant Cutters, and Advanced Flint Multitool. The Crude Multitool is the first progression stage that turns leaves into a dependable Branch source; removing it from that capability dead-ends the primitive woodworking chain. Its intended leaf output remains 4 Twigs + 1 Branch.


## v0.31.0-b storage, knowledge, and movable-water boundary
`WaterPotItem` is the persistence bridge for movable vessel water: block fill units are serialized into item custom data on Survival break and restored to the placed block entity. The block itself is deliberately non-directional. A full pot also acts as a non-consuming washing surface for Dirt Piles and Reed Rolls.

`WoodenChestBlock` intentionally delegates spatial connection, obstruction checks, lid state, and double-chest combining to vanilla `ChestBlock`, while Project Flint supplies a 9-slot block entity/menu and custom chest rendering materials. Single/double capacity is therefore 9/18 rather than vanilla 27/54.

`BookshelfBlockEntity` owns the physical knowledge state (0-4 inserted Books) separately from its two crafting inputs. At four Books the blockstate switches to `filled=true`; the item preserves the count when the workstation is moved. The only current workstation recipe is Book production; future recipes can gate on the stored-book state rather than requiring a new station format.

Beam and Sawmill blocks use `CreativeOnlyBlockItem` as an explicit staging boundary: registration/assets can stabilize now without silently opening Survival construction or Sawmill progression.

## Limestone, recipe records, and long-duration cooking (v0.31.5-b)

Limestone is the first shoreline masonry chain with explicit physical-state changes. Natural Limestone is a submerged disk feature in river/beach-family biomes. Natural ore drops Cobbled Limestone; Cobbled and Smooth Limestone use falling-block behavior, while fired Limestone and the Brick/Chiseled/Polished finishes are stable. Advanced Flint Multitool harvesting is enforced in the same destroy-progress/server-drop boundary used by other Project Flint progression blocks, avoiding vanilla loot-table bypasses.

`RecipeParchmentItem` is the persistent recipe-record boundary. It stores one stable recipe id in custom item data and refuses reassignment after the first binding. `PrimitiveStoveRecipeRegistry` resolves those ids through a stable definition registry while retaining fallback compatibility with earlier `PrimitiveProcessingRegistry` kiln ids. Bookshelf research can therefore bind parchments without migrating Oven inventories or changing recipe ids.

`WoodenCabinetBlockEntity` is intentionally not generic storage: all 27 slots accept Recipe Parchment only. The stone-top/oak visual is presentation; the gameplay role is a dedicated recipe archive.

`PrimitiveStoveBlockEntity` consumes four stripped logs/wood per 30-minute ignition. Direct no-parchment operation is limited to ordinary food-like kiln recipes; bound parchment operation resolves the stored recipe definition. v0.31.7-b exposes the station to Survival as the player-facing **Primitive Oven** (legacy registry id `primitive_stove`) and sets its processing rate to 1.45x the shared kiln baseline.

## Persistent placement state and recipe-record authority (v0.31.7-b)

Crushed Stone now carries `CrushedStoneBlock.PLAYER_PLACED` as serialized blockstate data. Inventory placement is the only path that sets it true; machine/hammer/world creation inherits the false default. Advanced Flint Multitool recovery checks that state at destroy-progress, interaction-gate, and authoritative server-break layers. This avoids adding a block entity solely for one bit of state and survives falling because falling blocks carry their `BlockState`.

Recipe Parchment remains an immutable recipe-id token. `PrimitiveStoveRecipeRegistry` now owns the first prepared-food recipe definitions, and both `RecipeParchmentItem` lore and the Primitive Oven recipe guide read requirements from those same definitions. The Bookshelf only performs research/binding and can expose multiple selectable definitions for one research material. Do not duplicate ingredient text in language files or parchment-specific hardcoded lore.

The player-facing `Primitive Oven` retains the legacy registry id `projectflint:primitive_stove` for save compatibility. Its rate is 1.45x the shared kiln baseline; this is a direct baseline multiplier, not a multiplier compounded on top of Mortar Kiln speed.

Flax is the String-progression authority. Mature `FlaxCropBlock` drops String directly, while loot-table String is removed globally so mob/chest/block loot cannot bypass the agricultural gate.
