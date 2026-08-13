# Project Flint v0.27.1-a — User Texture Import Map

Source: `updated textures.zip`

| Uploaded file | Project Flint target | Notes |
|---|---|---|
| `advanced kiln glass.png` | `src/main/resources/assets/projectflint/textures/item/advanced_kiln_glass.png` |  |
| `bone club.png` | `src/main/resources/assets/projectflint/textures/item/bone_club.png` |  |
| `burnt branch block side.png` | `src/main/resources/assets/projectflint/textures/block/burnt_branch_block_side.png` |  |
| `burnt branch block top.png` | `src/main/resources/assets/projectflint/textures/block/burnt_branch_block_top.png` |  |
| `clay grinder base (THIS WILL NEED TO BE REWORKED IN THE JSON AND FILES TO BE A SLAB).png` | `src/main/resources/assets/projectflint/textures/block/clay_grinding_base.png` | Converted Clay Grinding Base model/shape into a full 16×8×16 slab. |
| `clay mortar.png` | `src/main/resources/assets/projectflint/textures/item/clay_mortar.png` |  |
| `crude clay block.png` | `src/main/resources/assets/projectflint/textures/block/crude_clay_block.png` |  |
| `dirt pile.png` | `src/main/resources/assets/projectflint/textures/item/dirt_pile.png` |  |
| `fired clay axe mold.png` | `src/main/resources/assets/projectflint/textures/item/fired_clay_axe_mold.png` |  |
| `fired clay block mold.png` | `src/main/resources/assets/projectflint/textures/item/fired_clay_block_mold.png` |  |
| `fired clay bowl.png` | `src/main/resources/assets/projectflint/textures/item/fired_clay_bowl.png` |  |
| `fired clay brick mold.png` | `src/main/resources/assets/projectflint/textures/item/fired_clay_brick_mold.png` |  |
| `fired clay cutters mold.png` | `src/main/resources/assets/projectflint/textures/item/fired_clay_cutters_mold.png` |  |
| `fired clay multiool mold.png` | `src/main/resources/assets/projectflint/textures/item/fired_clay_multitool_mold.png` |  |
| `flint axe piece.png` | `src/main/resources/assets/projectflint/textures/item/flint_axe_pieces.png` |  |
| `flint cutter piece.png` | `src/main/resources/assets/projectflint/textures/item/flint_cutter_piece.png` |  |
| `flint multitool.png` | `src/main/resources/assets/projectflint/textures/item/flint_multitool.png` |  |
| `image.png` | `src/main/resources/assets/projectflint/textures/block/fired_clay_block.png` | Mapped to Fired Clay Block (the only unnamed texture; visually corresponds to the fired-clay block asset). |
| `leather backpack.png` | `src/main/resources/assets/projectflint/textures/item/leather_backpack.png` |  |
| `lightly burnt twig.png` | `src/main/resources/assets/projectflint/textures/item/lightly_burnt_twig.png` |  |
| `mortar bricks.png` | `src/main/resources/assets/projectflint/textures/block/mortar_bricks.png` |  |
| `refined flint multitool piece.png` | `src/main/resources/assets/projectflint/textures/item/refined_flint_multitool_piece.png` |  |
| `sand ball.png` | `src/main/resources/assets/projectflint/textures/item/sand_ball.png` |  |
| `sand flake.png` | `src/main/resources/assets/projectflint/textures/item/sand_flake.png` |  |
| `super heated clay.png` | `src/main/resources/assets/projectflint/textures/item/super_heated_clay.png` |  |
| `unfired clay block mold.png` | `src/main/resources/assets/projectflint/textures/item/unfired_clay_block_mold.png` |  |
| `unfired clay brick mold.png` | `src/main/resources/assets/projectflint/textures/item/unfired_clay_brick_mold.png` |  |
| `unfired clay multiool mold.png` | `src/main/resources/assets/projectflint/textures/item/unfired_clay_multitool_mold.png` |  |
| `unfired clay pot mold.png` | `src/main/resources/assets/projectflint/textures/item/unfired_clay_pot_mold.png` |  |
| `woven wood top_bottom texture.png` | `src/main/resources/assets/projectflint/textures/block/woven_wood_top_bottom.png` | Used for Woven Wood top/bottom, slabs, and Woven Chest top/bottom. |

- `fired clay bowl.png` is also copied to the 2/8, 4/8, and 6/8 bowl-state PNGs because those four textures were identical before this update.
- `unfired_clay_block_mold` and `fired_clay_block_mold` now have dedicated model texture references instead of borrowing the brick-mold texture.
- User-provided PNG pixels are copied byte-for-byte; no image generation, recoloring, resizing, or other pixel edits were performed.
