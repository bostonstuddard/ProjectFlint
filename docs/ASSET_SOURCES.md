# Asset Sources and License Ledger

## Policy

Third-party art is never copied merely because it is downloadable. Before an asset enters the repository:

1. its license must explicitly permit the intended reuse/modification/distribution;
2. the exact source project and version must be recorded here;
3. required license/attribution text must ship with the mod;
4. imported files should be listed by path when practical;
5. unclear or All Rights Reserved material is not imported without explicit permission.

## Current redistribution status

**External third-party research packs remain reference-only. Alpha.16 additionally uses assets from two resource packs that the user explicitly supplied and approved for Project Flint reuse; those imports are recorded below.**

Alpha.3's improved item, Loose Rock, and five forage-family textures are original Project Flint temporary development art. See `TEMPORARY_ASSETS.md`.

External packs below are research references/candidates only.

## Permissive research pool

### Bushy Vegetation

- Source: Modrinth — `bushy-vegetation`
- License shown by Modrinth during alpha.3 research: **BSD-3-Clause**
- Compatibility research: current releases include Minecraft 1.21.9–1.21.11.
- Useful direction: fuller, readable low-resolution vegetation silhouettes instead of a flat/gray cross.
- Status: **reference/candidate only; no files imported.**

### Bushy Leaves Textures

- Source: Modrinth — `bushy-leaves-textures`
- License shown by Modrinth: **MIT**
- 1.21.11-compatible release family observed during research.
- Useful direction: low-resolution bushy foliage treatment across vanilla leaf families.
- Status: **reference/candidate only; no files imported.**

### dronko's alternative Bushy Leaves

- Source: Modrinth — `better-busy-leaves`
- License shown by Modrinth: **MIT**
- Compatibility shown during research includes 1.21.11.
- Useful direction: low-complexity foliage depth.
- Status: **reference/candidate only; no files imported.**

### Better BushyLeaves

- Source: Modrinth — `better-bushyleaves`
- License shown by Modrinth: **MIT**
- Compatibility shown: 1.21.x.
- Useful direction: vanilla-adjacent foliage geometry.
- Status: **reference/candidate only; no files imported.**

## Explicitly excluded

Visually relevant packs marked **ARR / All Rights Reserved** are not development asset pools. They may inform broad observations such as “denser foliage is easier to read,” but their files/pixels are not copied into Project Flint without separate permission.

## Project Flint forage visual direction

Forage has separate biome IDs but may share temporary visual families until final art exists:

- temperate — broad readable greenery;
- cold — tighter/cooler vegetation;
- dry — reserved for a future ecology pass; arid bushes are inactive in alpha.4;
- jungle — dense broad-leaf growth;
- wet/unique — low dense growth for swamp-like/atypical environments.

Final textures can become more biome-specific without changing the gameplay registry IDs.
## Alpha.4 reference review

- **dronko's alternative Better Bushy Leaves** — Modrinth; MIT. Reviewed for low-cost bushy-leaf geometry and resource-pack compatibility. No pixels redistributed.
- **Better BushyLeaves** — Modrinth; MIT. Reviewed as another vanilla-like geometry reference. No pixels redistributed.
- Alpha.4's shipped temporary bush/rock art uses Project Flint-authored geometry and Minecraft's active leaf/stone texture references, so an installed resource pack naturally supplies the visible pixels.



## Alpha.16 — user-provided approved asset pools

The project owner supplied these two archives directly and explicitly approved their assets for Project Flint use:

- `§b§l      ⸎ §bLegendsRPG.zip`
- `textures.zip`

These are treated as **user-provided approved project asset sources**, distinct from the external Modrinth research pool above. No permission is inferred for unrelated external packs.

### Imported in alpha.16

| Project Flint asset | Approved source archive/path | Transformation |
|---|---|---|
| `textures/item/campfire_stone.png` | `§b§l      ⸎ §bLegendsRPG/assets/minecraft/textures/item/materials/mining/stone_chunk.png` | Direct project-local copy for Campfire Stone |
| `textures/item/branch.png` | `§b§l      ⸎ §bLegendsRPG/assets/minecraft/textures/item/materials/foraging/tool_rod.png` | Direct project-local copy repurposed as Branch |
| `textures/item/crude_multitool.png` | `§b§l      ⸎ §bLegendsRPG/assets/minecraft/textures/item/tools/foraging/rock_hatchet.png` | Direct project-local copy used for the first multitool presentation |
| `textures/item/burnt_branch.png` | Derived from the approved `branch.png` import above | Darkened derivative for heat-processed Branch |
| `textures/block/loose_dirt.png` | `textures.zip:textures/block/dirt.png` | Direct project-local copy for Loose Dirt block presentation |
| `textures/item/dirt_pile.png` | Derived from `textures.zip:textures/block/dirt.png` | Cropped/masked mound-style item sprite |
| `textures/item/flint_chip.png` | `textures.zip:textures/item/flint.png` | Reduced/centered chip derivative |

The following alpha.16 block-atlas support textures are derived from existing Project Flint-owned/current textures rather than the approved packs:

- `textures/block/plant_tool_binding_ground.png` — block-atlas copy of the existing Plant Tool Binding sprite;
- `textures/block/lightly_burnt_twig_ground.png` — block-atlas copy of the existing Lightly Burnt Twig sprite;
- `textures/block/campfire_stone_ground.png` — block-atlas support texture for campfire assembly geometry;
- `textures/item/burnt_twig.png` — item presentation based on the existing Project Flint burnt-twig ground treatment.

### Rendering derivative added in alpha.18

| Project Flint asset | Approved source lineage | Transformation |
|---|---|---|
| `textures/block/campfire_stone_block.png` | Existing approved `textures/item/campfire_stone.png` / alpha.16 `campfire_stone_ground.png` lineage | Transparent Campfire Stone pixels are extended into a fully opaque 16×16 block-atlas tile so the small multipart campfire-stone cuboids render visibly in stone-only states |

No new external archive or license source is introduced by alpha.18; this is a project-local rendering derivative of the already-approved Campfire Stone asset.


## Alpha.19 approved-pack imports

All alpha.19 art below comes from the user-provided/approved `textures.zip`; no web asset was used. Paths are archive-relative.

| Project Flint asset | Approved source | Treatment |
|---|---|---|
| `textures/item/flint_firestarter.png` | `textures.zip!/textures/item/flint_and_steel.png` | Direct development reuse |
| `textures/item/flint_multitool.png` | `textures.zip!/textures/item/stone_axe.png` | Direct temporary development reuse |
| `textures/item/crudely_woven_bowl.png` | `textures.zip!/textures/item/bowl.png` | Direct development reuse |
| `textures/item/unfired_woven_bowl.png` | bowl + `textures/item/clay_ball.png` | Project-local 16×16 derivative |
| `textures/item/fired_clay_bowl*.png` | bowl + `textures/item/brick.png` / clay pixels | Project-local staged-fill derivatives |
| `textures/item/unfired_clay_ball.png` | `textures.zip!/textures/item/clay_ball.png` | Direct development reuse |
| `textures/item/fired_clay.png` | `textures.zip!/textures/item/brick.png` | Direct development reuse |
| `textures/block/crude_clay_block.png` | `textures.zip!/textures/block/clay.png` | Direct development reuse |
| `textures/block/crude_clay_kiln.png` | `textures.zip!/textures/block/clay.png` | Direct development reuse |
| `textures/block/baked_clay_kiln.png` | `textures.zip!/textures/block/terracotta.png` | Direct development reuse |
| `textures/block/kiln_top_form.png` | `textures.zip!/textures/block/dirt.png` | Direct development reuse |

These are user-supplied project resources and are approved for Project Flint development use. Final-art status is tracked separately in `TEMPORARY_ASSETS.md`.


## Alpha.21.4 — strict human-made texture sweep

Alpha.21.4 supersedes all earlier Project Flint texture-derivative notes for the **current shipped PNG files**. Every PNG under `assets/projectflint/textures/` is now an exact, byte-for-byte copy from one of the two project-owner-provided approved archives:

- `textures.zip`
- `§b§l      ⸎ §bLegendsRPG.zip`

No PNG in the alpha.21.4 Project Flint texture tree was generated, recolored, composited, resized, cropped, filtered, or pixel-edited by the assistant.

See `TEXTURE_PROVENANCE.md` for the complete target/source table and SHA-256 values, and `texture_provenance.json` for the machine-readable equivalent. Those files are the authoritative current texture ledger; older derivative descriptions remain only as historical release records.


## Alpha.22 user-redone texture intake

The alpha.22 source upload contained a sibling `ProjectFlint - Textures/` folder of redone 16x Project Flint textures. Matching currently-implemented assets were copied **byte-for-byte** into the active resource tree. No resize, recolor, redraw, compositing, filtering, or other pixel modification was performed. Exact target/source/hash mappings are recorded in `TEXTURE_PROVENANCE.md` and `texture_provenance.json` under source archive `ProjectFlint sourceCode.zip`.

Unused supplied concepts (`ash`, clay pots, woven basket, wooden toolbox) were **not** silently added as gameplay content in this consolidation release; their source files remain part of the user's upload for later design work.


## Alpha.22.1 direct owner texture

- `assets/projectflint/textures/block/clay_grinder_wheel.png` ← direct user upload `minecraft_grindstone_round (1).png`
- SHA-256: `fde3925f76757bb59cf973d7b65535d998f3e9c5a683c44a103e5d30d5a20de9`
- The file is copied byte-for-byte. No assistant-side pixel modification was performed.
## alpha.22.3 direct texture-fill and metadata icon

The project owner supplied a new human-made `ProjectFlint - Textures` texture-fill archive for alpha.22.3. Every corresponding active Project Flint texture target is copied **byte-for-byte** from those PNGs; no resize, crop, recolor, redraw, compositing, filtering, or other pixel modification is permitted. Exact per-target hashes and source filenames are recorded in `docs/texture_provenance.json` and `docs/TEXTURE_PROVENANCE.md`.

The supplied `projectFlint Icon.png` is also copied byte-for-byte to `assets/projectflint/icon.png` and referenced by `fabric.mod.json` for loader/Mod Menu metadata. The icon is not part of the texture-atlas provenance manifest because it lives outside `assets/projectflint/textures/`.

`Animal Bones` uses an exact unmodified copy of `textures.zip:textures/item/bone.png`; no new pixel art was generated. Uploaded concepts that are not yet active gameplay objects (including the basket, toolbox, clay-pot, ash, and straw-bedding art) remain unused rather than being silently introduced into progression.

## alpha.22.4 direct technology textures

Alpha.22.4 uses only exact byte-for-byte copies from the latest project-owner-supplied `ProjectFlint - Textures/texture asset master file for use/` archive. Crushed Stone and Crude Clay Block replace their prior active files; Mortar Bricks, Filed Bone, Bone Firestarter, Bone Club, Woven Wool, Clay Mortar, Unfired Mortar Brick, and Raw Hide add matching active targets. No PNG was generated, redrawn, recolored, resized, cropped, composited, filtered, or otherwise pixel-edited.

The Crude Sleeping Bag models reference Minecraft's existing sixteen wool block textures according to the block's `DyeColor` state. No sleeping-bag texture file is created or derived. Exact Project Flint target/source/hash mappings are recorded in `TEXTURE_PROVENANCE.md` and `texture_provenance.json`.

## alpha.22.5 exact texture repairs and calamari intake

Alpha.22.5 copies the following project-owner-supplied human-made PNGs byte-for-byte, with no pixel modification:

| Project Flint target(s) | Direct user source |
|---|---|
| `textures/item/fired_clay_bowl_2.png`, `_4.png`, `_6.png` | `ProjectFlint - Textures/fired clay bowl.png` |
| `textures/block/burnt_branch_ground.png` | `ProjectFlint - Textures/burnt branch.png` |
| `textures/block/flint_assembly.png` | `ProjectFlint - Textures/flint chip.png` |
| `textures/item/raw_calamari.png` | `ProjectFlint - Textures/texture asset master file for use/03_Farming_Food/items/food_mysterymeat.png` |

The two block targets are atlas-domain copies required by placed assembly models, not edited derivatives. Exact SHA-256 values appear in both texture provenance ledgers.

## alpha.22.6 master asset priority and direct replacements

The newly supplied `ProjectFlint - Textures/texture asset master file for use/` directory is the first-choice source for current and future Project Flint texture targets. Root Project Flint files remain authoritative when the owner names them directly, including `roasted seeds.png`. Category subfolders are used only when an explicit root/master target is absent.

Alpha.22.6 copies the Bone Club from root-master `bone_wooden_axe.png`, the Bone Brush from root-master `bone_brush.png`, and the new fan/backpack/storage/sand/glass/cooked-food targets from the matching category files. The attached `bone firestarter.png` is copied exactly to the active Flint Firestarter target at the owner's request. Roasted Seeds is re-copied exactly from root `roasted seeds.png`. No PNG pixels are edited.


## v0.28.7.4-a additions

### Owner-provided Sweet Berry assets
Source archive: `new shit.zip` supplied directly by the Project Flint owner. The following PNGs were imported byte-for-byte with only destination filenames normalized:
- `sweet berry seeds.png` → `assets/projectflint/textures/item/sweet_berry_seeds.png`
- `sweet berry jam.png` → `assets/projectflint/textures/item/sweet_berry_jam.png`
- `sweet berry pie.png` → `assets/projectflint/textures/item/sweet_berry_pie.png`

### Bear Minimum model source
Black/Brown Bear client geometry and UV layouts are adapted from the separate `BlackBearModel.java` and `BrownBearModel.java` model classes in Thomas1034's Bear Minimum project. Bear Minimum is Apache-2.0 licensed; Project Flint includes the license text at `LICENSES/Bear-Minimum-Apache-2.0.txt`. Project Flint changes package names, identifiers, mappings, and integration while retaining the source model geometry/UV intent.


## v0.28.7.8-a direct user asset
- `pumpkin_slice.png`: supplied directly by the user in the release request and imported unchanged for `projectflint:pumpkin_slice`.

## v0.28.18-a owner-supplied texture batch
Source archive: `new update stuff(1).zip`, supplied directly by the Project Flint owner.

Exact files imported without pixel modification:
- `finalized wattle and daub stuff/wattle.png`
- `finalized wattle and daub stuff/tied wattle.png`
- `finalized wattle and daub stuff/mud_pile.png`
- `finalized wattle and daub stuff/daub_pile.png`
- `finalized wattle and daub stuff/wet daub_pile.png`
- `finalized wattle and daub stuff/unfired_wattle_and_daub.png`
- `finalized wattle and daub stuff/wattled_daub_bricks.png`
- `crude plant torch.png`
- `advanced_kiln_front active rework.png`


## v0.28.19-a direct owner upload
The owner supplied the finalized Wattled Daub Floor Tile, Window, Door top/bottom, and refreshed Flower Petals textures in `e16f77d8-49a7-4b9e-ae00-5d29c6ce0eff.zip`. These files are approved first-party Project Flint assets and are imported byte-for-byte. See `TEXTURE_PROVENANCE.md` for exact target paths and hashes.

## v0.28.20-a direct owner upload
The dedicated Wattled Daub Door inventory sprite was supplied directly by the Project Flint owner as `watled daub door item texture.png` and imported byte-for-byte to `assets/projectflint/textures/item/wattled_daub_door.png`. The placed Door top/bottom textures remain the v0.28.19-a owner assets. No texture pixels were modified.

## v0.28.21-a direct owner upload — Flint Torch progression

Source: user-provided `new update stuff(2).zip`.

Imported unchanged:
- `torch holder item textuer.png` → Torch Holder item texture.
- `unlit flint torch item and wall texture.png` → Unlit Flint Torch item + wall texture.
- `torch holder with unlit flint torch.png` → combined Flint Torch Holder item texture.
- `lit flint torch item and wall texture.png` → Lit Flint Torch item + wall texture.

These are owner-supplied Project Flint assets. Preserve their pixels exactly in future synchronization passes.


## v0.28.37-a owner-provided Wood -> Water workstation art
Source archive: `new update stuff(3).zip` supplied directly by the Project Flint owner. Used byte-for-byte for the Sanding Station work surface, Lumber Processor Chopping Block top/side/bottom, and Water Pot front/side/top. See `TEXTURE_PROVENANCE.md` for exact hashes.


## v0.31.0-b owner-provided Wood/Storage/Bone asset batch
Source archive: `new update stuff(4).zip`, supplied directly by the Project Flint owner. Active item, beam, Water Pot, Bookshelf, Wooden Chest, Sawmill, Bone equipment, bear spawn-egg, Plant Fiber/Binding, Wooden Board, and Wood Firestarter PNGs are copied byte-for-byte. Exact hashes and destination mappings are in `TEXTURE_PROVENANCE.md`.

`bound wooden processor.png` is intentionally not used: the owner explicitly instructed the Bound Wood Processor to use the existing Lumber Processor top texture instead. `beam.png` / `beamtop.png` are not used because the archive provides individual per-species beam assets. The chest/example and Sawmill/example JSON files are reference material rather than texture sources.


## v0.31.45-b derived Hardwood Crafting Table textures
`hardwood_crafting_table_side.png`, `_top.png`, and `_bottom.png` are 52% brightness derivatives of existing approved `lumber_processor_side.png`, `_top.png`, `_bottom.png`. Source pixels come from the Project Flint owner's earlier texture pack. These are transformations of approved assets, not AI-generated artwork. The Brick Shingles texture is reused as a reference in the baked Clay Kiln body and top models, with no pixel modification.
