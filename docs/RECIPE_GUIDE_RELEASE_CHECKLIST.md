# Mandatory workstation and recipe-guide release check

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

For v0.31.39-b and later: The global Guide contains **recipe inputs and outputs only**. Put fuel durations, machine assembly steps, and control instructions in machine-specific UI help or a dedicated tutorial, not above guide rows. Condense equivalent recipe outputs into alternative variants and expose all accepted ingredient examples in hover tooltips. When an accepted tag contains many items, split its tooltip into short cycling pages. For the Basin, include every supported stripped Oak/Birch log and wood input, preserve its stored input in BlockEntity NBT, and verify the inserted item is visible through blockstate variants.
