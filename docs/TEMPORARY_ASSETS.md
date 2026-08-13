# Temporary Asset Ledger

Project Flint temporary art exists only to keep mechanics testable before final original art is supplied. A temporary asset must never silently become final.

## Alpha.9 active placeholders

| Asset | Current source | Status | Final replacement |
|---|---|---|---|
| Forage bush geometry | Project Flint-authored alpha.4 low shrub model | TEMPORARY | Original biome/ecology-specific bush models |
| Forage bush pixels | Minecraft's currently loaded biome-appropriate leaf textures | TEMPORARY REFERENCE | Original Project Flint textures supplied by the project artist |
| Loose Rock geometry | Restored Project Flint alpha.3 single low-volume rock model | TEMPORARY PROJECT ART | Original loose-rock model |
| Loose Rock pixels | `textures/block/loose_rock_placeholder.png`, restored from alpha.3 | TEMPORARY PROJECT ART | Original Project Flint loose-rock texture |
| Rock inventory texture | `textures/item/rock.png`, restored to the alpha.3 generated-item presentation | TEMPORARY PROJECT ART | Original hand-rock model/texture |
| Refined Rock item texture | `textures/item/refined_rock.png` | TEMPORARY PROJECT ART | Original replacement |
| Twig item texture | `textures/item/twig.png` | TEMPORARY PROJECT ART | Original replacement |
| Twig Pile geometry | Project Flint-authored low overlapping 1–12 sprite-card pile models | TEMPORARY PROJECT ART | Original pile geometry/model |
| Twig Pile pixels | `textures/block/twig_pile_twig.png`, a temporary Project Flint copy of the existing Twig sprite for block-atlas rendering | TEMPORARY PROJECT ART | Original Project Flint twig-pile texture |
| Lightly Burnt Twig texture | Darkened derivative of Project Flint's own temporary Twig sprite | TEMPORARY PROJECT ART | Original replacement |
| Plant Fiber item texture | `textures/item/plant_fiber.png` | TEMPORARY PROJECT ART | Original replacement |

## Deprecated but intentionally retained files
Older forage-bush family PNGs remain in patch-upgraded development trees for compatibility/cleanup simplicity, but active bush models use the native leaf-texture placeholder approach. Newly inactive snowy/swamp bush resources also remain because their registry IDs are deliberately retained for old alpha worlds.

## Rule
Third-party assets may only enter the repository when their license explicitly permits redistribution and the exact source/license is recorded here or in `ASSET_SOURCES.md`. Alpha.9 adds no third-party assets.

## Alpha.13 new placeholders

| Asset | Current source | Status | Final replacement |
|---|---|---|---|
| Crude Plant String item texture | Project Flint-authored 16x16 temporary twisted-fiber sprite | TEMPORARY PROJECT ART | Original Project Flint string/cordage texture |
| Crude Stone Hammer item texture | Project Flint-authored 16x16 temporary primitive-tool sprite | TEMPORARY PROJECT ART | Original Project Flint tool texture/model |
| Crude Reed item texture | Project Flint-authored 16x16 temporary reed-bundle sprite | TEMPORARY PROJECT ART | Original Project Flint reed texture |
| Plant Tool Binding item texture | Project Flint-authored 16x16 temporary wrapped-fiber sprite | TEMPORARY PROJECT ART | Original Project Flint binding texture |
| Crushed Stone block texture | Project Flint-authored 16x16 temporary stone/gravel hybrid | TEMPORARY PROJECT ART | Original Project Flint crushed-stone texture |
| Fiber ground-pile pixels | Project Flint block-atlas copy of the existing temporary Plant Fiber sprite | TEMPORARY PROJECT ART | Original ground-pile fiber presentation |
| Burnt Twig assembly pixels | Project Flint block-atlas copy of the existing temporary Lightly Burnt Twig sprite | TEMPORARY PROJECT ART | Original assembly presentation |
| Rock assembly pixels | Project Flint block-atlas copy of the existing temporary Rock sprite | TEMPORARY PROJECT ART | Original assembly presentation |
| Crude Reed assembly pixels | Project Flint block-atlas copy of the alpha.13 temporary Crude Reed sprite | TEMPORARY PROJECT ART | Original assembly presentation |
| Crude String assembly pixels | Project Flint block-atlas copy of the alpha.13 temporary Crude Plant String sprite | TEMPORARY PROJECT ART | Original assembly presentation |
| Fiber / hammer / binding ground geometry | Project Flint-authored low flat component models | TEMPORARY PROJECT ART | Final component-specific in-world models |

Alpha.13 adds no third-party assets.


## Alpha.14 new placeholders

| Asset | Current source | Status | Final replacement |
|---|---|---|---|
| Very Refined Stone item texture | Project Flint-authored 16x16 temporary sharpened-stone sprite | TEMPORARY PROJECT ART | Original Project Flint refined-stone texture |
| Very Refined Stone stockpile texture | Project Flint-authored block-atlas derivative for physical stockpiles | TEMPORARY PROJECT ART | Original stockpile presentation |
| Crude Harvesting Tool texture | Project Flint-authored 16x16 temporary handheld harvesting-tool sprite | TEMPORARY PROJECT ART | Original Project Flint harvesting-tool texture/model |
| Flower Petals texture | Project Flint-authored 16x16 temporary petal sprite | TEMPORARY PROJECT ART | Original Project Flint flower-processing texture |
| Rock stockpile geometry | Project Flint-authored 1–12 count low stone-pile geometry using the existing Rock presentation | TEMPORARY PROJECT ART | Original Project Flint stockpile geometry |
| Harvesting-tool assembly geometry | Project Flint-authored low ground assembly for two stones, two bindings, and one burnt twig | TEMPORARY PROJECT ART | Original Project Flint assembly presentation |

Alpha.14 adds no third-party assets.


## Alpha.16 asset status

Alpha.16 fixes the placed Plant Tool Binding null texture and introduces the campfire/multitool/earthwork visuals. Unlike earlier placeholder-only passes, several visuals come from the two resource packs explicitly supplied and approved by the project owner; their exact provenance is tracked in `ASSET_SOURCES.md`.

| Asset | Current source | Status | Final replacement |
|---|---|---|---|
| Plant Tool Binding ground pixels | Block-atlas copy of the existing Project Flint Plant Tool Binding item sprite | TEMPORARY PROJECT ART / RENDER FIX | May be replaced by final dedicated in-world binding art |
| Lightly Burnt Twig ground pixels | Block-atlas copy of existing Project Flint Lightly Burnt Twig sprite | TEMPORARY PROJECT ART | Final dedicated in-world twig art |
| Campfire Stone ground support pixels | Alpha.18 opaque block-atlas derivative of the approved Campfire Stone art, used by campfire multipart geometry | APPROVED DERIVATIVE / RENDER FIX | No replacement required unless final campfire art direction changes |
| Campfire Stone item | User-provided approved LegendsRPG asset | APPROVED USER ASSET | No replacement required unless art direction changes |
| Branch / Crude Multitool | User-provided approved LegendsRPG assets | APPROVED USER ASSET | No replacement required unless art direction changes |
| Burnt Branch | Darkened derivative of approved user-provided Branch asset | APPROVED DERIVATIVE | No replacement required unless art direction changes |
| Loose Dirt / Dirt Pile / Flint Chip | User-provided approved `textures.zip` assets or derivatives | APPROVED USER ASSET / DERIVATIVE | No replacement required unless art direction changes |

Alpha.16 therefore does **not** mark the approved imported pack assets as anonymous temporary placeholders. Their source is explicitly traceable in `ASSET_SOURCES.md`.

## Alpha.18 asset status

Alpha.18 replaces the ineffective mostly-transparent campfire-stone ground sampling with `textures/block/campfire_stone_block.png`. The new tile is derived from the already-approved Campfire Stone asset and exists only to make the small multipart stone cuboids render reliably. It is therefore tracked as an approved derivative/render fix rather than a new anonymous placeholder.


## Alpha.19 temporary art

| Asset | Current development source | Replacement target |
|---|---|---|
| Flint Multitool | approved `textures.zip` stone-axe sprite | Final purpose-built Flint Multitool silhouette matching its combined hammer/cutter/digging role |
| Flint Firestarter | approved vanilla-style flint-and-steel sprite | Primitive non-metal firestarter art if the final design should visually distinguish it from vanilla Flint and Steel |
| Woven/clay bowl stages | approved bowl/clay/brick sprites and local derivatives | Final visibly woven bowl, clay-lined bowl, fired bowl, and readable fill-stage sprites |
| Crude Clay Block/Kiln | approved clay texture | Final hand-formed wet/clay masonry art |
| Baked Clay Kiln/top | approved terracotta/dirt textures | Final fired-clay kiln exterior and shaped crown art with a consistent two-block silhouette |

The alpha.19 models/state logic is intended to survive a later texture replacement; none of these temporary visuals should be treated as a progression/API dependency.


## Alpha.21.4 status

All currently shipped Project Flint PNG files were replaced during the strict human-made texture sweep. Earlier assistant-authored/generated/derived PNG artwork is no longer present in the current texture tree. Any semantic mismatch that remains should be resolved later by importing another unchanged human-made source texture or by receiving human-authored replacement art; it must not be fixed by assistant-side pixel editing.


## Alpha.22 user-redone replacements

Twenty-six active item/ground texture targets were replaced with exact copies of the redone 16x textures supplied by the project owner alongside the alpha.22 source upload. These replacements supersede the older temporary/pack-substitute entries for those exact targets. The remaining temporary-asset rows are historical context unless a later section marks them current. No assistant-edited texture pixels were introduced.

## 0.1.0-alpha.23 imported human-made assets

No alpha.23 texture pixels were generated, recolored, resized, composited, cropped, or otherwise edited. The two user-attached PNGs were copied byte-for-byte. The remaining new item icons were copied byte-for-byte from the approved Project Flint asset-master folder already bundled with the supplied project archive.

- `bone_firestarter.png` — exact uploaded Bone Firestarter PNG.
- `sleeping_bag.png` — exact second uploaded PNG.
- `unfired_kiln_glass.png` — `11_Decorative_Building/blocks/glass_crystal_0.png`.
- `advanced_kiln_glass.png` — `11_Decorative_Building/blocks/glass_crystal_1.png`.
- `unfired_clay_mold_base.png` — `06_Industrial_Steampunk/items/part_mould.png`.
- Axe mold variants — `06_Industrial_Steampunk/items/part_mould_axe.png`.
- Brick mold variants — `06_Industrial_Steampunk/items/part_mould_plate.png`.
- Cutters mold variants — `06_Industrial_Steampunk/items/part_mould_sickle.png`.
- Multitool mold variants — `06_Industrial_Steampunk/items/part_mould_hammer.png`.
- `flint_axe_pieces.png` and `flint_cutter_piece.png` — `01_Primitive_Rustic/items/material_dust_flint.png`.
- `refined_flint_multitool_piece.png` and `ground_flint.png` — `additional_textures_unsorted/xbony2/items/ironflint.png`.
- `flint_axe.png` — bundled `bone_wooden_axe.png`.
- `flint_plant_cutters.png` — `09_Tools_Weapons_Armor/items/tool_saw_stone.png`.
- `advanced_flint_multitool.png` — exact existing Project Flint `flint_multitool.png`.


## v0.28.17-a — intentionally null Wattle/Daub visuals
The following new progression IDs intentionally have no finalized texture hookup in this release: `wattle`, `tied_wattle`, `mud_pile`, `daub_pile`, `wet_daub_pile`, `unfired_wattle_and_daub`, and `wattled_daub_bricks`. This is deliberate: the owner is preparing the final art separately. Do not generate, recolor, resize, or substitute placeholder pixels. The next texture patch should wire only owner-approved/human-made assets to these already-established IDs.

## v0.28.18-a — Wattle/Daub null visuals resolved
The intentionally missing Wattle/Daub visuals listed under v0.28.17-a are now resolved by exact owner-supplied PNGs from `new update stuff(1).zip`. The Crude Plant Torch temporary vanilla-torch presentation is also resolved. No assistant-side pixel modification was performed.
