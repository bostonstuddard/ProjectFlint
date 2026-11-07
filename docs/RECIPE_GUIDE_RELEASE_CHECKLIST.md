## v0.31.65-b guide consistency
- Check K-key Wooden Grinder shows 8 Mud Piles for Loose Mud Bricks and 2 Loose Mud Bricks for Bricks.
- Check K-key personal recipes show 2 Leather Straps and 1 Repair Kit; the Hardwood Table shows 3 Straps and 2 Repair Kits.
- Check K-key Hardwood Table shows both water-bucket variants, Mud Piles, Packed Mud, Mud Bricks, Loose Mud Bricks, Mud Brick slabs/stairs/walls, Dirt slab/stairs, Loose Dirt and Loose Mud Brick slabs, Dirt ↔ Loose Soil and cheaper Clay Block.
- Check G-key mud chapter is purely player-facing and mentions 10% Packed Mud walking speed and Forge charcoal auto-burning.
- Preserve actual Hardwood-only recipe types, not vanilla `minecraft:crafting_*` or EMI registration.
## v0.31.62-b — Primitive Stone operation is not a recipe
The G-key Field Guide must explain Crude Stone Hammer -> natural Stone -> Crushed Stone + 4 Rocks. The K-key recipe guide has **no new crafting recipe** for this physical mining/processing interaction. Test the mechanic through client progress, AttackBlock callback, pre-break guard, and server completion. Do not move this into a crafting category or imply that the Crude Hammer can mine ore/Cobblestone.


## v0.31.61-b — PMI Sheet station exclusivity
- Assert `data/projectflint/recipe/hardwood_pmi_sheets.json` exists with `projectflint:hardwood_durable`, 1 glass block + Bone Knife → 2 non-placeable sheets and one point of tool wear.
- Assert `data/projectflint/recipe/personal_pmi_sheets.json` is **absent** after the apply script runs and has no `PersonalCraftingGuideRegistry` entry. Keep the K-key Hardwood Table display and G-key player's description synchronized.
- Regression test: placing ingredients in the 2×2 personal grid gives no PMI Sheets; Hardwood Table takes three seconds and returns the knife with one damage per craft, including shift-click.
- Glass block must register `TransparentBlock`, not nonexistent `GlassBlock` or light-blocking `TintedGlassBlock`.

## v0.31.60-b — PMI Sheet recipe checks

- Add both `personal_pmi_sheets.json` and `hardwood_pmi_sheets.json` as actual durable recipes: one Pure Mica Insulated Glass + one Bone Knife → two non-placeable PMI Sheets. Both retain the Knife at +1 damage, including shift-click paths.
- Check the G Field Guide describes transparent Glass, vest recovery and the Sheet recipe; K crafting references show personal and Hardwood variants. Do not create a vanilla unrestricted crafting recipe by mistake.
- Confirm the final item texture is distinct from the placed block texture, appears in creative search, and is not registered as a BlockItem.

## v0.31.59-b — Basin / guide parity

The Basin K-key guide must reflect **four staged items**, **one hunger point per stomp**, and exact material-specific stomp thresholds. The G-key book describes basic gameplay and follows real progression, with no dev notes, source identifiers or historical changes. Chapter text must paginate by measured wrapped line height to avoid a mostly blank page after a short paragraph.

## v0.31.58-b — Crafting and Basin guide synchronization

- Personal: 1 Wooden Sheet + Bone Knife → 3 Wooden Cups; 2 Wooden Cups + Clay Milk Bucket → 5 Raw Milk cups.
- Hardwood: same combinations → 4 Wooden Cups, 6 Raw Milk cups; 8 Flax Seeds / Clay Water Bucket → 6 Wheat Seeds; 8 Fertilizer around Potassium Dust → 12 Potassium-Rich Fertilizer.
- Cutter pattern: sticks `S` in left column, flint `F` at upper-center, center-right and lower-center (`SF ` / `S F` / `SF `).
- Basin: Rare Earth from Clay/Sand/Gravel 4% each; Slag 1.8%; Raw Copper Slag 12.5%. Every chance independent. Validate K list includes each alternate drop, and G describes that all can succeed together.
- Forge: five-minute uninterrupted batch does **not** cause auto-relight. Fat Torch lasts 50 minutes and fails to ignite at Y < 0.

## v0.31.57-b — Required guide sync

Each new hardwood recipe must be added to `data/projectflint/recipe` and regenerated in `HardwoodCraftingGuideRegistry.java`, then checked in K under `Hardwood Table`. Every Basin yield belongs in `BASIN_MATERIAL_YIELDS.md` and the Basin section of `ProjectFlintRecipeGuideScreen`, plus a current-player G handbook chapter. Forge inputs/outputs must also be visible in the K guide. Bootstrap Table recipe is deliberately not Hardwood-only.

# Mandatory workstation and recipe-guide release check

## v0.31.55-b parity notes

- K/Basin now includes every material input with guaranteed/main output; output hover explains additional independent chances. `docs/BASIN_MATERIAL_YIELDS.md` is the numeric authority. A zero-output chance is not a broken recipe (Stone Dust, Slag).
- K/Brick Furnace now lists Cobblestone -> Stone. G Field Guide documents Axles, Hardwood Sticks, restored door path, depth conditions, exact material-yield odds and Deepslate locks.
- Hardwood-only recipe JSON adds an alternate Basin, stone-family slabs/stairs, and Axle. Retained earlier Hinged Door JSON is enabled through `RecipeManagerMixin`; do not remove or override these recipes without explicit design approval.
- Always validate both gameplay handling AND guide data, and test when no recipe is available (timed crafting screen cannot issue instant output).


Whenever a workstation or recipe is added, changed or removed, verify the following before shipping:

1. The **gameplay source** (Wooden/Clay Grinder, Sanding Station, Bookshelf, Primitive Oven/heat, Brick Furnace or any newly added system) implements the true materials, outputs, timing, and input retention. Never add a ProjectFlint-owned EMI plugin; PF-EMI handles discovery externally.
2. `ProjectFlintRecipeGuideScreen.Station` contains every newly usable workstation. Its `recipes()` switch must list the correct gameplay registry and show non-empty pages. If a new station has a static early prototype recipe, document exactly what its row represents.
3. The workstation's GUI has a `RecipeGuideButton` opening its own station, and the global guide can reach the station. Check both at normal and narrow UI sizes.
4. New parchment recipes appear in the Bookshelf registry **and** in the Primitive Oven gameplay registry where applicable. The guide reads these registries dynamically. Don't rely on PF-EMI to compensate for missing gameplay recipes.
5. Test the longest translated output names on each guide page. `renderRecipe` clips long names and scrolls them horizontally with a pause at each edge; hovering the scrolling label gives the full output tooltip. No label may paint outside its row.
6. Update `tools/validate_release.py` for the new workstation and/or recipe source; include explicit checks for guide routing and button placement. Update the release notes.

For v0.31.35-b: Fireclay Forge is Creative-only. Its guide illustrates the *fuel byproduct*, not a metallurgy recipe. The Basin, Drying Rack and Millstone remain decorative Creative-only prototypes with no gameplay recipes and are not presented as processing workstations yet.

For v0.31.36-b: Tanned Armor is registered through Clay Grinder gameplay JSON/guide definitions; the Vest and newly craftable Fireclay Forge controller are Wooden Grinder recipes. The Vest additionally has a ProjectFlint-namespaced 2×2 crafting recipe visible in the revived recipe book. The Brick Furnace fuel restriction is shown inside its machine UI rather than on its recipe page. No project-owned EMI plugin may be added.

- Fuel rules belong in station help/hover text, **never fake recipe rows** (e.g. Charcoal -> Brick Furnace).
- If a station has fuel/byproduct output and process output, they must be separately visible, synced, and persisted.

For v0.31.37-b and later: player-facing station labels and hover text must describe what players can do **now**. Do not expose developer notes such as "future recipes", "placeholder", "reserved output", or "not implemented" in a GUI. Keep such notes in source comments/docs. Use separate localized `Component` entries in `setComponentTooltipForNextFrame` for independent tooltip lines rather than one screen-wide sentence. Check that numeric help text agrees with the actual server-side input count and output rate (Forge: four logs/wood, 60 seconds, four Charcoal).

For v0.31.38-b and beyond: Newly added player inventory crafting recipes belong in `PersonalCraftingGuideRegistry` AND their actual `data/projectflint/recipe` JSON. On every release check both the crafting grid and the global Guide. Basin processing has its own Guide station; Brick Furnace Sand-to-Glass is a machine-only recipe in `BrickFurnaceRecipeRegistry`, not a datapack `minecraft:smelting` recipe.

For v0.31.39-b and later: The global Guide contains **recipe inputs and outputs only**. Put fuel durations, machine assembly steps, and control instructions in machine-specific UI help or a dedicated tutorial, not above guide rows. Condense equivalent recipe outputs into alternative variants and expose all accepted ingredient examples in hover tooltips. When an accepted tag contains many items, split its tooltip into short cycling pages. For the Basin, include every supported stripped Oak/Birch log and wood input, preserve its stored input in BlockEntity NBT, and verify the real inserted item is visible through a synced Basin block-entity item renderer (not blockstate wood-piece geometry).

## v0.31.54-b onward: mandatory guide and decision parity

For **every gameplay release**, compare player-facing Field Guide (`G`) chapters with current mechanics. For recipe/station/input/output changes, also check the K-key Recipe Guide and gameplay recipe registry and run the guide parity validator. For non-recipe mining/permission patches, verify that the recipe guide remains correct but do not add fictitious recipe rows.

Review `docs/CURRENT_SYSTEMS.md`, `docs/ARCHITECTURE.md`, `docs/PRIMITIVE_PROGRESSION_MATRIX.md`, `docs/ROADMAP.md` and relevant ADRs. When a gameplay decision supersedes a former mechanic, publish a new ADR and record the precise regression conditions. Historical release documents stay historical; in-game content remains up-to-date and free of dev notes.

## v0.31.56-b — physical Basin inputs and Hardwood tool remainders

- K/Basin must display the exact required stomp count dynamically alongside every primary or byproduct output. G/Basin describes the actual item model, repeated landings, removal reset, and single hunger point on completion.
- Verify client rendering uses real `BasinInput` with packet updates and no static inserted wood shape. Test multiple inputs, different wood types, iron/copper, bare Basin, chunk reload, and multiplayer join.
- Hardwood recipes live in a **different recipe type** from vanilla. Do not let the vanilla ResultSlot calculate remainders for Hardwood recipes: it filters to vanilla CRAFTING and deletes the knife/file. The matched Hardwood recipe supplies worn tool/container remainders; all clicks, shift-clicks and full inventories must be checked.

## v0.31.64-b checks
- [ ] Personal K guide shows live 2×2 Wooden Axe, Pickaxe, all Wooden Shovel variants, Wooden Sword and Wooden Hoe. Check recipe JSON and the RecipeManager filter, not the display alone.
- [ ] Oak Sheet and Board personal recipes use `durable_crafting` (outputs 3/1); Hardwood generic any-Planks recipes use `hardwood_durable` (outputs 4/2). No duplicate Oak entry in the Hardwood category.
- [ ] Verify Bone Knife/Flint File durability remainders at both stations and through shift-click.
- [ ] Fireclay Forge display matches guarded Charcoal auto-relight behavior.

