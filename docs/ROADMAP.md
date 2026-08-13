# Project Flint — Development Roadmap

> **Roadmap philosophy:** Project Flint is developed by technological capability, not by version number.
>
> Releases may occur at any point for bug fixes, balancing, visual cleanup, refactors, or playtest corrections. A stage is considered complete only when its **player-facing progression loop is coherent and difficult to bypass**, not when a particular version number is reached.

---

## Core Progression Principle

Every major stage should answer:

> **What can the player physically do now that they could not do before?**

Project Flint should avoid arbitrary tier gates wherever possible. Progression should emerge from new operations, materials, processes, and infrastructure.

Examples:

- a sharp edge allows cutting;
- cordage allows binding;
- binding allows composite tools;
- composite tools allow meaningful woodworking;
- fire allows thermal processing;
- containers allow storage and transport;
- ceramics allow controlled heating;
- airflow allows higher temperatures;
- metallurgy allows stronger tools and mechanisms;
- rotary power allows sustained mechanical work.

A new technology should ideally unlock **several downstream possibilities**, rather than exist only to craft the next item.

---

# Current Foundation

### v0.28.7-a stability repair

v0.28.7-a keeps the existing capability stage unchanged while repairing three infrastructure issues: the 1.21.11 true-darkness implementation now uses the lightmap shader pipeline instead of an invalid GPU texture upload, both grinders retain a still-valid selected recipe after crafting, and obsolete inactive source/root patch artifacts are removed. No progression or balance gate changes are introduced by this repair.

### v0.28.7.2-a environment/balance follow-up

v0.28.7.2-a keeps the same progression stage but hardens survival pressure and workstation pacing: full outdoor night targets about 8-block unaided visibility, exposed primitive fires are rain-sensitive, prolonged wetness escalates to Drenched, Mortar Kiln becomes the faster heat station, and the permanent primitive campfire becomes slightly slower than the Baked Kiln. Loose Soil, actual worldgen Loose Rock rendering, bear spawn eggs, and Brown Bear UV mapping are repaired alongside those balance changes.

The primitive opening now establishes the intended baseline.

## World and survival rules

- The world is largely read-only without an appropriate operation or tool.
- Vanilla surface civilization and major surface progression shortcuts are suppressed.
- Surface hostile-mob pressure is reduced while animals remain dangerous food sources.
- Project Flint worlds use Hardcore semantics and forced Hard difficulty.
- Early carrying capacity is limited to five usable hotbar slots.
- Vanilla personal crafting and vanilla advancement progression are removed/replaced.
- Creeper explosions cannot be exploited for progression-locked block drops.

## Primitive materials

The player can currently obtain:

- Loose Rock
- Rock
- Refined Rock
- Plant Fiber
- Twig
- Lightly Burnt Twig
- Burnt Twig
- Campfire Stone
- Flower Petals
- Crude Reed
- Plant Tool Binding
- Branch / Burnt Branch
- Dirt Pile / Loose Dirt
- Flint Chip / vanilla Flint

## Primitive environmental interactions

The player can currently:

- gather loose surface stone;
- strip grass for Plant Fiber;
- clear some vegetation poorly by hand;
- harvest biome forage bushes using a Refined Rock;
- work stones together to produce a Refined Rock;
- place Twigs into a physical Twig Pile;
- attempt primitive fire-starting;
- briefly burn a Twig Pile;
- char Twigs into Lightly Burnt Twigs in the disposable fire;
- assemble and relight a persistent primitive campfire, then char Twigs into later-tier Burnt Twigs;
- use the Crude Multitool for leaves, Branches, Dirt/Grass, and Gravel processing.

## Existing design commitments

- Final art does not block development; temporary assets are explicitly tracked for replacement.
- Primitive carrying limitations are intended to become progression through baskets, packs, containers, etc.
- Two-handed interactions are allowed and important.
- Physical/world interactions are preferred over arbitrary crafting-grid recipes.
- Existing accepted tuning should not be casually reworked during unrelated stages.
- Reusable systems should be introduced before content begins depending on dozens of one-off special cases.

---




### Alpha.18 forage/render reliability pass

Alpha.18 is intentionally narrow and does not add a new capability tier:

- Refined Rock forage-bush breaking is explicitly permitted by the primitive survival break gate, removing callback-order dependence while keeping the existing 3–4 Twig output.
- The same gate now lists the already-supported primitive break operations rather than blanket-denying every survival break before custom handlers can complete.
- Bare placed Campfire Stones use an opaque derivative of the approved Campfire Stone art so the stone-only frame is visible before Lightly Burnt Twigs are added.

The next playtest should verify Refined Rock and Cutters against several forage-bush biome variants, then build the permanent campfire one component at a time from 1 through 12 stones before adding twigs. No balance decisions should be inferred from this repair pass.

### Alpha.17 playtest repair pass

Alpha.17 repairs alpha.16's early-fire regression and narrows several systems back toward vanilla behavior:

- temporary fire again creates Lightly Burnt Twigs; Burnt Twigs are reserved for the later permanent-fire tier;
- permanent campfire construction uses Lightly Burnt Twigs, eliminating the accidental circular requirement;
- dropped items return to vanilla `ItemEntity` behavior while primitive carrying is enforced at inventory insertion;
- Sugar Cane receives roughly 50% more generation attempts in new terrain;
- Sweet Berry Bush breaking/renewable harvesting is separated into normal breaking versus cutters-only right-click harvest;
- swimming gains additional survival pressure through 3× swimming exhaustion and territorial Squids.

The next playtest should specifically verify Q-dropping/repickup, partial-stack pickup, existing-world versus new-chunk Sugar Cane density, Berry Bush interaction with every early tool tier, Squid water-only aggro, and the restored Lightly Burnt Twig route before adding more progression content.

### Alpha.16 capability expansion

Alpha.16 pushes the opening from a single disposable fire and one-purpose cutters into the first reusable processing infrastructure:

- Refined Rock bush harvesting now yields 3–4 Twigs; cutters guarantee 4.
- Cutters harvest normal Sugar Cane, while deliberate cane hand-working produces Crude Reeds.
- Very Refined Stone can be worked into Campfire Stones at 100% success using a deliberately slower 16-strike process.
- Twelve Campfire Stones + twelve Burnt Twigs build a persistent, relightable campfire frame with a 30-second flame window.
- The disposable Twig Pile fire bootstraps Burnt Twigs; permanent fire additionally chars Branches.
- Burnt Twigs can be hammer-split into Lightly Burnt Twigs, preserving the earlier hammer/cutters recipe chain.
- The Crude Multitool introduces full leaves-tag harvesting, Branch acquisition, primitive Dirt/Grass excavation, and primitive Gravel processing.
- Nine Dirt Piles compact into falling Loose Dirt; Gravel produces Flint Chips that can be worked four-at-a-time into vanilla Flint.

This materially advances **Cordage / First Real Tools** and begins the **Earthworks / Controlled Fire** bridge, but it does not complete either stage. The next architectural pressure is to stop growing hard-coded target/recipe branches and formalize operation/processing definitions once the alpha.16 playtest establishes which interactions are worth keeping.

### Alpha.15 playtest correction pass

Alpha.15 keeps the alpha.14 tool/material capabilities but corrects their interaction language:

- ordinary Loose Rock placement/starting Rock stockpiles is removed; Very Refined Stone remains the intentional physical stone stockpile;
- primitive ground objects require support below placement;
- hammer/cutters hold-to-break actions share the primitive progress HUD;
- cutters now process Leaf Litter into 2 Plant Fiber and remove tall flowers without vanilla duplicate drops;
- dropped world stacks partially enter available primitive slots instead of using all-or-nothing pickup;
- Creeper no-loot terrain destruction is retained through a safer explosion callback boundary;
- Rock-on-Crushed-Stone refinement uses an explicit consume/output transaction;
- advancement descriptions/layout are expanded into an actual progression guide.

### Alpha.14 capability progress

Implemented toward the Cordage / First Tools stage:

- physical Rock stockpiles;
- Very Refined Stone produced from Rock + Crushed Stone;
- Very Refined Stone stockpiles;
- reversible physical assemblies;
- first higher-order tool assembly using Very Refined Stone + Plant Tool Binding + Lightly Burnt Twig;
- Crude Stone Cutters that convert Short Grass directly into Crude Plant String and flower-tag blocks into Flower Petals;
- native durability/combat-item components for the first crude tools: Hammer 24 durability / 2.0 damage / 0.8 speed, Cutters 48 durability / 1.0 damage / 1.2 speed;
- world-item pickup aligned with the five-slot primitive inventory boundary.

Still ahead in this stage:

- formal data-driven tool-operation capabilities (`CUT`, `SCRAPE`, `DIG`, `SPLIT`, `HEW`);
- broader plant/fiber ecology;
- deliberate wood access;
- a primitive digging tool;
- replacing temporary art with final Project Flint assets.

# NEXT STAGE — Cordage, Binding, and the First Real Tools

This is the next major development target.

The purpose of this stage is to move the player from **gathering and improvising with loose objects** to deliberately constructing the first composite technologies.

At the end of this stage, the player should possess their first genuine tools and gain access to entirely new classes of environmental interaction.

## Current stage progress

The first material-processing slice is now functional:

- two Plant Fibers can be physically staged and worked into **Crude Plant String**;
- Lightly Burnt Twigs and a Rock can be staged and worked into the first reusable **Crude Stone Hammer**;
- the hammer provides the first deliberate tool-driven gathering operations: slow Reed harvesting and Stone crushing;
- Stone crushing leaves a persistent **Crushed Stone** world state instead of converting terrain directly into an inventory block;
- Crude Reeds and Crude Plant String can be physically assembled into **Plant Tool Binding**;
- Very Refined Stone can be physically stockpiled and recovered; ordinary Loose Rock placement was removed in alpha.15 after playtesting showed it was frustrating rather than useful;
- Crushed Stone acts as a refinement surface that turns a Rock into **Very Refined Stone**;
- two Very Refined Stone, one Plant Tool Binding, and one Lightly Burnt Twig assemble into **Crude Stone Cutters**;
- the cutters turn Short Grass into **Crude Plant String**, Leaf Litter into **2 Plant Fiber**, and flowers into **Flower Petals** while consuming native item durability;
- advancements teach each major capability as it is achieved.

Alpha.16 now adds a persistent primitive campfire, deliberate reed processing, the Crude Multitool, Branch/Burnt Branch, Dirt Pile/Loose Dirt, and Flint Chip processing. The stage is **not complete**: formal data-driven tool-operation/processing capabilities, deeper cordage and woodworking, containers/carrying upgrades, repair/maintenance, and broader heat/earth material systems remain ahead.

---

## 1. Plant Fiber Processing

Plant Fiber should stop being merely an inventory ingredient.

The player needs a physical process for turning gathered fibers into useful binding material.

### Intended progression

```text
Plant Fiber
    ↓
Fiber preparation
    ↓
Twisted Fiber / Fiber Strand
    ↓
Cordage
```

### Design goals

- No 2×2 crafting-grid recipe.
- The process should visually communicate that fibers are being combined and twisted.
- It should be simple enough to perform repeatedly without becoming a click-spam minigame.
- The process should eventually support better fiber sources and better cordage without needing to be rewritten.

### Likely implementation direction

Introduce a reusable **hand-processing interaction**.

A process definition should be able to describe:

- required input;
- required count;
- optional second-hand item;
- number/duration of work steps;
- resulting item;
- failure/waste behavior, if any;
- sound/particle/animation feedback.

This same infrastructure could later support:

- fiber twisting;
- bark processing;
- hide scraping;
- kneading clay mixtures;
- primitive food preparation;
- other hand-worked materials.

### Initial balance target

Do not finalize exact numbers until the interaction itself feels good.

Cordage should be costly enough that:

- the first tool feels valuable;
- the player cannot immediately manufacture a full toolset;
- gathering better fiber later remains meaningful.

It should not require so much grass stripping that progression becomes repetitive before the first tool exists.

---

## 2. Cordage

Cordage becomes the first **structural material** the player creates.

Its purpose is not merely to be another crafting component. Cordage introduces the idea that multiple objects can be permanently assembled into a new tool.

### Cordage should enable

- hafting stone onto wood;
- tying tool heads;
- simple bundles;
- early construction bindings;
- later traps;
- baskets and carrying technology;
- primitive handles and frames.

This should become a long-lived material category rather than a one-stage ingredient.

---

## 3. Formal Tool-Operation System

Before adding several tools, Project Flint should introduce a reusable operation system.

Blocks and interactions should ask:

> **What operation can this held tool perform?**

not:

> **Is the player holding exactly `projectflint:stone_knife`?**

### Initial operation vocabulary

At minimum:

- `CUT`
- `SCRAPE`
- `DIG`
- `SPLIT`
- `HEW`

Likely later:

- `CHOP`
- `SAW`
- `DRILL`
- `CRUSH`
- `GRIND`
- `POUND`
- `PRY`
- `CARVE`

Thermal/material systems will eventually introduce operations such as:

- `DRY`
- `CHAR`
- `FIRE`
- `SMELT`

but they do not need to be implemented yet.

### Tool capability data

A tool should be able to define:

- operations it provides;
- effectiveness per operation;
- valid material classes;
- durability/work capacity;
- optional quality;
- optional edge condition;
- interaction speed modifier.

Example conceptual data:

```json
{
  "operations": {
    "cut": 1.0,
    "scrape": 0.7
  },
  "materials": [
    "plant",
    "fiber",
    "bark"
  ]
}
```

Exact format can evolve, but the architecture should support data-driven tools instead of hardcoding every block/tool pair.

---

## 4. Primitive Assembly

Composite tools should not simply appear from shapeless recipes.

The player should physically assemble components.

### First assembly concept

```text
Handle
+ Worked Stone Component
+ Cordage
        ↓
Composite Tool
```

The exact interaction can be refined through testing, but the system should support:

- a base component;
- one or more attached components;
- binding material;
- required operation/process;
- final assembled item.

### Architecture goal

Create a generic **primitive assembly definition** rather than separate Java logic for every tool.

Future examples:

```text
stick + stone edge + cordage → cutting tool
stick + stone head + cordage → axe
long branch + point + cordage → spear
frame + fiber + binding → basket
wood pieces + binding → primitive construction component
```

---

## 5. First Tool: Primitive Cutting Tool

The first proper tool should probably be a small hafted cutting edge rather than jumping immediately to an axe.

Working name only:

- Stone Knife
- Primitive Knife
- Hafted Flake
- Stone Cutting Tool

Final naming can wait.

### Capability

Primary operation:

- `CUT`

Secondary possible operation:

- `SCRAPE`

### Immediate unlocks

The tool should make previously inaccessible vegetation useful.

Potential targets:

- Short Grass
- selected ground vegetation
- fibrous plants
- vines
- bark from appropriate small wood
- foliage materials later

### Short Grass progression

Current:

```text
Bare hand
→ painfully slow clearing
→ no resource
```

After cutting technology:

```text
Cutting Tool
→ deliberate harvest
→ usable plant material/fiber
```

This makes the existing "wrong tool" interaction pay off rather than being a dead-end annoyance.

---

## 6. Better Fiber Acquisition

The first cutting tool should dramatically improve the player's relationship with fiber.

The player should move from:

```text
damage/strip terrain
→ hope for Plant Fiber
```

toward:

```text
identify useful vegetation
→ cut it deliberately
→ obtain fiber-bearing material
→ process it
```

This is important because Cordage will become a recurring requirement.

### Direction

Different vegetation may eventually produce different fibers:

- grass fiber;
- bark fiber;
- reed fiber;
- vine fiber;
- other biome-specific fibers.

The next stage does not need all of these, but the system should not assume there will forever be one universal `plant_fiber`.

---

## 7. Primitive Scraper

A scraper should become the first example of a tool whose purpose is **processing**, not gathering.

Potential early functions:

- strip bark;
- clean plant material;
- prepare fibers;
- scrape surfaces;
- later process hides.

This begins separating:

```text
resource acquisition
```

from:

```text
resource preparation
```

which becomes increasingly important later.

---

## 8. Digging Stick / Primitive Digging Tool

The player should gain their first meaningful ability to manipulate earth without granting immediate Minecraft-style shovel behavior.

### Intended capability

- disturb soil;
- loosen soft earth;
- collect small quantities of earth;
- access clay-bearing material later;
- prepare future planting areas.

### Important restriction

This should **not** mean:

```text
tool → dirt block drops
```

The player should still not casually pick up cubic-meter blocks of terrain.

Instead:

```text
ground
↓ DIG
disturbed/loosened ground
↓ collect/process
earth material
```

That transition lays groundwork for the later Shelter & Earth stage.

---

## 9. Early Wood Access

This stage should introduce woodworking carefully.

The first composite tools should **not immediately enable full tree harvesting**.

### Initial wood sources

Prefer:

- fallen/dead wood;
- branches;
- saplings/small growth;
- bush wood;
- exposed small limbs;
- deliberately added deadwood world features.

### First wood operations

Possible progression:

```text
branch
↓ CUT
stick / handle blank

small wood
↓ SCRAPE
debarked wood

small wood
↓ SPLIT
split wood / kindling
```

Full tree felling belongs later in the woodworking progression.

### Why

A whole mature tree represents an enormous amount of material. Letting the player's first stone knife convert one into neat logs would immediately collapse the primitive scale Project Flint is trying to establish.

---

## 10. First Tool Durability / Wear Model

This stage is the right point to determine how primitive tools deteriorate.

Avoid blindly copying vanilla durability if a better system fits.

Possible concepts:

- edge wear;
- binding wear;
- head loosening;
- reduced operation effectiveness;
- repair/rebinding instead of instant disappearance.

The first implementation can remain simple, but it should be architected so primitive tools do not necessarily have to behave like vanilla pickaxes with a green durability bar.

---

## 11. Progression Feedback

Add Project Flint advancements for the new technological breakthroughs.

Potential advancement beats:

```text
Cordage
First Binding
First Composite Tool
A Cutting Edge
Working the Earth
First Worked Wood
```

Advancements should celebrate meaningful capability unlocks, not every minor item pickup.

---

# NEXT-STAGE COMPLETION GATE

The Cordage / First Tools stage is complete when a fresh player can naturally experience this progression:

```text
Loose Rock
    ↓
Refined Rock
    ↓
Bushes / Twigs
    ↓
Plant Fiber
    ↓
Primitive Fire
    ↓
Fiber Processing
    ↓
Cordage
    ↓
Primitive Assembly
    ↓
First Cutting Tool
    ↓
Useful Vegetation Harvesting
    ↓
Better Fiber Supply
    ↓
Scraping / Digging Tools
    ↓
First Deliberate Wood and Earth Processing
```

And, critically:

- vanilla crafting does not bypass it;
- punching blocks does not bypass it;
- explosions do not bypass it;
- mob drops do not trivially bypass it;
- village/structure loot does not bypass it;
- the five-slot inventory remains playable during the loop;
- every new operation has clear visual/audio feedback;
- the player can understand *why* an interaction fails without constant text spam;
- resources are available enough that progression is exploration/work, not spawn-RNG grinding.

Only after this loop is fun and hardened should development move deeper into shelter/material processing.

---

# FOLLOWING STAGE — Woodworking, Shelter, and Persistent Fire

This stage expands from "I can make tools" into "I can deliberately alter my environment."

## Woodworking

Progress toward:

- deliberate branch harvesting;
- handles;
- stakes;
- split wood;
- debarked wood;
- hewn wood;
- structural timber;
- eventually controlled tree felling.

A whole log should become a material that can be processed in multiple ways rather than instantly becoming four planks.

Conceptually:

```text
Tree
↓ fell
Whole Log

Whole Log
├─ strip → Debarked Log
├─ split → Split Timber
├─ hew   → Structural Timber
└─ later saw → Boards
```

---

## Persistent Fire

The current Twig Pile is the **discovery of fire**, not the final fire system.

Progress toward:

```text
brief flame
↓
embers
↓
maintained fire
↓
hearth
```

Add:

- fuel classes;
- burn time;
- ember persistence;
- ignition temperature;
- tending;
- extinguishing from rain/water;
- heat output;
- combustible-material behavior.

The player should eventually be able to preserve fire instead of repeatedly gambling on ignition.

---

## Primitive Food Preparation

Once persistent fire exists:

- roasting;
- basic cooking;
- drying;
- boiling only after containers exist;
- raw/cooked food differences;
- early preservation pressure.

Avoid simply restoring the vanilla Furnace food loop.

---

## Shelter Construction

Begin physical construction rather than crafting finished building blocks.

Potential first system:

```text
stakes / timber frame
        ↓
woven branches
        ↓
earth infill
        ↓
drying
        ↓
finished primitive wall
```

Possible construction families:

- lean-to / temporary shelter;
- timber frames;
- wattle;
- daub;
- thatch;
- packed-earth surfaces.

Shelter should eventually matter mechanically, but the exact survival penalties for exposure should not be added until the player has fair tools to solve them.

---

# LATER STAGE — Earth, Clay, and Ceramics

The player gains controlled access to mineral earth materials.

## Earth material system

Distinguish materials such as:

- loose earth;
- clay-bearing earth;
- sand;
- gravel;
- silt;
- organic material.

Avoid returning immediately to vanilla dirt-block inventory logic.

## Mixtures

Introduce physical recipes such as:

```text
earth
+ water
+ fiber/aggregate
→ workable mixture
```

Possible outputs:

- mud;
- daub;
- adobe;
- formed clay.

## Drying

Environmental drying becomes a reusable system affected by:

- time;
- rain;
- exposure;
- possibly temperature/humidity later.

## Ceramics

Progress:

```text
clay-bearing material
↓ preparation
workable clay
↓ forming
raw vessel / raw brick
↓ drying
dry vessel / brick
↓ firing
ceramic
```

Ceramics unlock:

- durable containers;
- cooking vessels;
- better storage;
- controlled thermal processing;
- higher-temperature infrastructure.

---

# LATER STAGE — Food Security, Agriculture, and Storage

Once basic tools and containers exist, food can become a genuine system.

## Agriculture

Rework:

- soil preparation;
- seed acquisition;
- planting;
- crop lifecycle;
- water requirements where appropriate;
- harvesting tools;
- crop processing.

Agriculture should represent a transition from opportunistic survival to planned food production.

## Storage

Progress from the five-slot primitive inventory into technologies such as:

```text
hands
↓
five-slot carrying
↓
bundle
↓
basket
↓
primitive pack
↓
containers
↓
dedicated storage
```

Capacity upgrades should feel like technological breakthroughs.

## Preservation

Potential systems:

- drying;
- smoking;
- salting later;
- sealed ceramic storage;
- cool storage later.

---

# LATER STAGE — High-Temperature Fire and Early Metallurgy

Metallurgy should not appear until the player already understands:

- fuel;
- heat;
- airflow;
- ceramics;
- construction;
- material preparation.

## Heat infrastructure

Progress:

```text
open fire
↓
hearth
↓
pit firing
↓
earthen kiln
↓
higher-temperature furnace
```

## Airflow

Progress:

```text
natural draft
↓
manual airflow
↓
primitive bellows
↓
improved bellows
↓
mechanically powered airflow later
```

## First metals

Likely progression:

- naturally accessible/native metal where appropriate;
- copper;
- improved copper working;
- alloying;
- bronze;
- eventually iron.

Ore should not become useful merely because the player owns a magic smelting block.

---

# LATER STAGE — Advanced Woodworking and Construction

Technologies may include:

- saws;
- boring/drilling;
- joinery;
- dimensional lumber;
- stronger frames;
- doors and fittings;
- carts;
- improved storage;
- larger permanent structures.

This stage creates the material precision needed for mechanical technology.

---

# LATER STAGE — Mechanical Power

The player moves from human-powered work to sustained external power.

## First sources

Potentially:

- water wheels;
- wind where suitable;
- animal power;
- hand/crank power for small mechanisms.

## Mechanical network

The underlying engine should eventually support:

- rotation;
- direction;
- speed;
- torque/load;
- shafts;
- gearing;
- transmission losses or limits;
- mechanical failure where useful.

Machines should consume physical capability from this network rather than merely checking for abstract energy points.

---

# LATER STAGE — Mechanized Processing

Mechanical power enables processes previously limited by human effort.

Examples:

- milling;
- grinding;
- sawing;
- pumping;
- powered airflow;
- crushing;
- material separation;
- textile processing;
- agricultural machinery.

A machine should improve throughput or enable a previously impossible operation rather than exist only as a faster crafting table.

---

# LATER STAGE — Ironworking and Industrial Expansion

Iron should represent an enormous improvement in:

- cutting edges;
- structural strength;
- durable mechanisms;
- fasteners;
- high-load machines;
- construction.

This should open the path toward increasingly sophisticated technological systems without abandoning the physical-process philosophy established at the beginning.

---

# Long-Term Design Horizon

Project Flint's full arc is approximately:

```text
Foraging
↓
Stone working
↓
Fire
↓
Cordage
↓
Composite tools
↓
Woodworking
↓
Shelter
↓
Earth processing
↓
Ceramics
↓
Agriculture & storage
↓
High-temperature fire
↓
Metallurgy
↓
Advanced construction
↓
Mechanical power
↓
Mechanized processing
↓
Industrial technology
```

The historical order is a design guide, not a rigid reenactment requirement.

When historical accuracy and enjoyable progression disagree:

1. preserve the physical logic of the technology;
2. preserve meaningful dependencies;
3. avoid arbitrary gamey unlocks;
4. adjust the exact order or scale where necessary to keep the game playable.

---

# Roadmap Maintenance Rules

This document should remain a **design roadmap**, not a changelog.

## Do

- update it when a technological dependency changes;
- treat advancement coverage as part of a stage's player-facing completion, so new capability milestones remain discoverable;
- mark large completed capability groups;
- expand the next one or two stages in detail;
- keep distant stages intentionally broader;
- document why a stage exists;
- define completion gates for major stages.

## Do not

- assign specific alpha version numbers to stages;
- predict how many bugfix releases a stage will take;
- list every small balance adjustment;
- rewrite accepted systems simply because development has moved forward;
- treat the roadmap as a promise that features must ship in an exact historical order.

Release-specific implementation details belong in:

```text
docs/releases/
```

Architectural decisions belong in:

```text
docs/decisions/
```

The roadmap answers:

> **Where is Project Flint technologically going, and what has to exist before we move there?**


### Alpha.19 Flint and clay-infrastructure expansion

Alpha.19 crosses the first meaningful boundary beyond crude stone tooling:

- Flint Firestarter gives reliable 32-use ignition without deleting the earlier petal/Twig campfire method.
- Crude Stone Multitool is promoted to a true 128-durability Hammer/Cutters consolidation tool; Flint Multitool becomes the faster 3-damage next tier.
- Flower, reed, fiber, dirt, and Flint Chip outputs now expose clear tier benefits.
- Dirt compaction is reduced to 4:1, making Loose Dirt practical enough to become a kiln construction material.
- Woven/clay bowls introduce staged material collection rather than direct Clay-block conversion.
- Open-fired clay leads into the first movable clay block/kiln and then a permanently baked two-block kiln.
- The completed kiln establishes a five-minute controlled-heat infrastructure state but intentionally has no invented post-alpha.19 recipes yet.

Next pressure: playtest the full no-skip chain and then define a reusable kiln-processing interface before adding multiple kiln recipes. Tool capabilities should likewise move toward data-driven operation definitions if another major tool tier is added.


### Alpha.22 Primitive Systems Consolidation

- [x] Apply alpha.21.7.1 dismantling hotfix to the latest user-uploaded source baseline.
- [x] Remove Clay Grinder dependence on vanilla Stonecutter menu/recipe classes.
- [x] Establish Project Flint grinder menu/screen/recipe registries with zero recipes until the clayworking expansion.
- [x] Unify permanent Campfire/Kiln timer and recipe state.
- [x] Centralize primitive tool capability inheritance/speed rules.
- [x] Add developer progression/capability/processing matrix and validator gates.
- [x] Import matching redone user-provided textures without pixel modification.
- [ ] Alpha.23: populate `projectflint:clay_grinding` with deliberate clayworking recipes after recipe design is approved.

### Alpha.22.3 consolidation follow-up

- [x] Exercise the independent Clay Grinder recipe system with its first explicit multi-count recipe: 4 Sugarcane Rolls → Sanding Paper.
- [x] Add Reed Roll → washed Sugarcane Roll as the first material-finishing chain feeding the Grinder.
- [x] Bring Clay Grinding Base dismantling/salvage behavior into parity with the completed Grinder tier.
- [x] Establish custom Wool and Animal Bones as primitive animal-resource outputs without replacing vanilla entity types.
- [x] Separate Horse flee behavior and Squid aggression from the generic retaliation path.
- [x] Refresh the active texture set and mod metadata icon from exact project-owner-supplied PNGs.
- [ ] Next pressure: define the first Sanding Paper use and its woodworking/material-finishing unlock before expanding the Grinder recipe list further.

### Alpha.22.5 corrective follow-up

- [x] Convert the portable Sleeping Bag into a linked two-block bed-style structure that advances a valid night.
- [x] Separate Flint whole-plant harvesting from held-flower petal processing and cover vanilla bush variants.
- [x] Add 2 Twigs → 1 Branch to the independent Clay Grinder recipe set.
- [x] Establish Squid meat/bone loot and a tag-driven consequence shared by all raw meats and fish.
- [x] Repair filled-bowl and placed Flint-assembly rendering using exact supplied human-made textures.
- [ ] Next pressure: playtest the repaired rest/food loops before adding further food preservation or cooked seafood progression.

### Alpha.22.6 woven carrying and renewable material follow-up

- [x] Turn the five-slot opening into an explicit wearable-storage progression with the Reed Backpack.
- [x] Add the first five-slot constructed container through Mortar Bricks + Woven Fan.
- [x] Extend reusable Sanding Paper into black pigment and woven-component production.
- [x] Add Bone Brush renewable Clay, Sand Ball, kiln-fired Glass Shard, and Cooked Calamari chains.
- [x] Delegate Sleeping Bag occupancy/night advancement to vanilla BedBlock while retaining custom connected presentation and salvage.
- [x] Establish the new owner-supplied master asset directory as the first-choice source for future active textures.
- [ ] Next pressure: playtest equipment transitions, container persistence, sleep synchronization, and renewable-material pacing before expanding portable storage again.
### Alpha.22.6.1 compile and earth-shaping follow-up

- [x] Add the required 1.21.11 codec to the Mortar Chest block.
- [x] Shape one placed Loose Dirt block into two Loose Dirt Slabs with either Multitool.
- [x] Keep one slab in-world, eject the other as an item entity, and preserve correct double-slab recovery.
- [ ] Smoke-test Mortar Chest opening/persistence and Loose Dirt Slab placement, stacking, waterlogging, conversion, and survival harvesting under Java 21.

## Completed in 0.1.0-alpha.23
- [x] Seven-slot Mortar Chest and chest-shaped directional presentation.
- [x] Reliable advancement reconciliation for already-owned primitive items.
- [x] Unfired/advanced kiln glass chain.
- [x] Mold base plus Axe / Brick / Cutters / Multitool fired mold families.
- [x] 2×2×2 Mortar Brick Advanced Kiln with reusable mold casting.
- [x] Cast-piece ground assembly and the new Flint Axe / Flint Plant Cutters / Advanced Flint Multitool progression.
- [x] Exact uploaded Bone Firestarter and Sleeping Bag item textures.
- [x] Equipped Reed Backpack player-back rendering.

### Follow-up pressure
- Playtest Advanced Kiln multiblock replacement/recovery and decide whether later tiers need a formal multiblock integrity controller.
- Replace any remaining provisional alpha.23 sourced item icons only when a better human-made approved asset is supplied or found in the approved packs.
- Balance the new advanced multitool speed modifiers after survival timing data rather than changing the requested output counts.

### v0.28.7.3-a playtest correction

This hotfix does not advance the technology stage. It corrects current survival readability and threat consistency: black 6–8-block midnight fog, canonical Brown Bear model mapping, separation of worldgen Loose Rock art from held Rock art, larger bear melee threat/health/damage, and actual Loose Soil multitool authority. Final bespoke worldgen Loose Rock art remains a future asset replacement; no texture pixels were fabricated for this correction.


### v0.28.7.4-a ecology correction

- [x] Raise Black/Brown Bear natural spawn weights into a visible-but-still-uncommon 6/4 range.
- [x] Make Dirt, Grass Block, and Loose Soil share one 33% adjacent-loosening rule.
- [x] Make forage bushes twig-only in survival across all current cutter tiers.
- [x] Scale Leaf Litter yield by the block's actual 1–4 segment amount.
- [x] Gate renewable Sweet Berry picking behind plant cutters and bush relocation behind Flint Plant Cutters tier.
- [x] Register owner-provided Sweet Berry Seeds/Jam/Pie art without inventing Jam/Pie recipes.
- [x] Replace shared Polar Bear rendering with the separate Black/Brown Bear geometry and UV layouts the supplied textures were authored for.
- [ ] Next pressure: survival-test berry food abundance, Loose Soil cascade frequency, and bear encounter density before further tuning.


### v0.28.7.5-a field-test pressure
- Verify the 6-8 block midnight envelope remains readable at the 0.06 moonlight floor across common brightness/display settings.
- Test Crude Plant Torch pressure: five-minute lifetime, carried burn damage, open-sky requirement, rain/water extinguishing, fire risk, and light level 6.
- Continue auditing future stack-size changes against explicit-count consumption; do not reintroduce whole-hand transformation semantics for one-workpiece processes.


### v0.28.7.6-a field-test pressure
- Verify midnight unlit terrain remains sufficiently dark without an artificial fog cutoff.
- Verify distant campfires, crude plant torches, lit kilns, and illuminated structures remain visible enough to serve as navigation landmarks.
- Tune only the lightmap/moonlight floor if nearby readability still needs adjustment; do not reintroduce short-range atmospheric fog for hardcore darkness.


### v0.28.7.7-a field-test pressure
- Verify midnight unlit terrain reaches true black without hiding distant campfires/torches/lit structures.
- Verify Flint Axe pumpkins always yield four Pumpkin Seeds and never the Pumpkin block.
- Verify vanilla Sweet Berries cannot plant bushes on any valid vanilla support while Project Flint Sweet Berry Seeds still can.


### v0.28.7.8-a food/gourd follow-up
- Pumpkin/Melon now have separate propagation-vs-food harvest outputs by tool role.
- Pumpkin Slice is registered as an edible primitive ingredient; future recipes can build from it.
- Raw-food value has been compressed to make cooking materially more important.


### v0.28.17-a — first structural shelter tier

Wattle and daub establishes the first intentionally limited construction system: a full reed/twig/string/mud/leaf/water progression, four-block load-bearing columns, two-block brick roof projections, destructive low-tech recovery, and Flint Multitool intact salvage. This creates a bridge from primitive ground crafting into architecture without making early shelter behave like unrestricted vanilla blocks. The next pressure is the owner's finalized texture pass, followed by openings/roofing/shelter-quality upgrades rather than immediately jumping to stronger masonry.

### v0.28.18-a — finalized Wattle/Daub visuals
- Connect the finalized owner-supplied art to the complete v0.28.17-a Wattle-and-Daub progression.
- Replace the temporary vanilla Crude Plant Torch presentation with owner-supplied art.
- Refresh the active Advanced/Mortar Kiln front animation without changing kiln processing behavior.


## After v0.28.19-a — shelter technology follow-up
- Give the creative-only Wattled Daub Door a survival recipe only after precision woodworking/joinery exists.
- Explore dedicated primitive roofing and weatherproofing so Wattle/Daub shelters affect rain/wetness without becoming an all-purpose permanent building tier.
- Keep Floor Tiles as interior infrastructure rather than another structural stacking component.

## After v0.28.20-a — shelter manufacturing pressure
- Consider a dedicated connected-window art set if full border-level CTM is desired beyond same-block internal face culling.
- Give the creative-only Wattled Daub Door a legitimate woodworking/joinery production chain once hinges/latches become technologically available.
- Continue using Wooden Grinder recipes as efficiency upgrades for processes the player has already learned primitively, rather than skipping discovery stages.

## After v0.28.21-a — settlement-lighting pressure

The Flint Torch Holder establishes a new design lane: infrastructure can improve survival without becoming freely portable. Follow-up work should favor technologies that improve installation, fuel preparation, ignition, or protected settlement lighting rather than simply handing the player brighter generic torches. Potential later branches include ceramic lamp bodies, controlled oil/fat fuels, enclosed lanterns after suitable glass/metal technology, and workshop ignition systems.

The fixture's strict Wattled-Daub safety envelope should be playtested before adding more wall devices. If failed placement feels opaque, add clear in-world feedback rather than weakening the structural requirements.



## After v0.28.22-a — guidance pressure

- Keep advancement descriptions to one direct sentence whenever possible.
- Add new advancement tabs only when a genuinely new technological era begins; do not turn every item into a milestone.
- New station recipes should be exposed through the Project Flint recipe guide at the same time they become obtainable.
- Prefer shared recipe registries over duplicating recipe definitions between gameplay code and guide UI.
- A future discovery layer may hide recipes until the relevant era/workstation has been reached, but v0.28.22-a intentionally favors clarity over recipe secrecy.


### Recipe discovery rule — v0.28.24-a
- Keep Minecraft's survival recipe-book button hidden.
- Add new workstation processes to the Project Flint custom recipe guide when they become playable.
- Keep advancement blurbs short; recipe details belong in the guide rather than advancement descriptions.
- Prefer integrated workstation book icons and the global K shortcut over one-off recipe buttons.


## Groundwater and wells

The v0.28.27-a aquifer layer is a seed-deterministic virtual groundwater field for the future settlement Well system. No blocks or saved aquifer records are generated: old and new worlds derive groundwater from the same world-seed/XZ algorithm on demand. The future Well should be constructible at any Y above a matching virtual aquifer footprint, query `AquiferSystem.findBelow(...)`, and produce renewable water at a cadence scaled by biome-derived groundwater yield. The Well itself remains intentionally deferred until its structure, bucket/container interaction, and water-storage progression are designed.


## After v0.28.28-a — finite water infrastructure

- Vanilla water source conversion is disabled; two adjacent sources no longer create renewable water.
- Preserve natural rivers/lakes/oceans as finite reservoirs rather than replacing vanilla water with a custom fluid.
- The first intended renewable-water technology is the future Well, backed by the virtual aquifer field introduced in v0.28.27-a.
- Future pumps/cisterns/irrigation should move or store water; they should not silently restore vanilla infinite-source behavior.
- Balance Well production by aquifer yield and biome humidity rather than by nearby visible water blocks.


## After v0.28.29-a — advancement philosophy
Future technology should add concise step-by-step advancement guidance as it is introduced. If Wood Era becomes too dense, split it only when a genuine new civilization tier exists rather than pruning useful instructions.

## After v0.28.30-a — Water Age production pressure
- Supply finalized art for the Sanding Station, Water Bearing Pot, Water Pot, Bound Wood Processor, Chopping Block, workpiece, Wooden Boards, and Wooden Beam.
- Build the first renewable-water Well around the existing virtual `AquiferSystem`; do not restore vanilla infinite source behavior.
- Use Wooden Boards/Beams as standardized construction parts for the next joinery, pulley, lifting, and water-infrastructure technologies.
- Keep future Water Age machines dependent on transported/stored water so the new finite-water economy remains meaningful.

### v0.28.37-a — Wood -> Water workstation visual pass
- Finalized Sanding Station, Water Pot, and Lumber Processor Chopping Block rendering using owner-supplied textures.
- Remaining Wood -> Water placeholder assets stay intentionally pending until matching owner art is supplied.


## v0.28.39-a — player-guidance and opening-pacing reset
- Keep Project Flint progression in one detailed advancement tree unless a future UI solution can preserve the same step-by-step readability without scattering the guide across tabs.
- Treat advancements as a quest-heavy survival manual: counts, tools, intermediate materials, and important upgrade benefits belong there when they help players know exactly what to do next.
- Keep custom workstation recipe books for recipe browsing; do not use them as a substitute for progression guidance.
- Re-test the opening with the 58% Plant Fiber chance, 65% Twig Pile ignition chance, faster Grass scraping, and three-failure Rock-knapping pity rule before further scarcity changes.


### v0.28.41-a progression-critical correction
Crude Multitool leaf harvesting is explicitly protected as a progression gate: leaves must yield 4 Twigs + 1 Branch at this tier so Branch-based fire and woodworking progression remains reachable.
