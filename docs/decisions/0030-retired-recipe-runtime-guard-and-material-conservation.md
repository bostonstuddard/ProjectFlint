# ADR 0030 — Retire Duplicating Reverse Recipes at Runtime

## Decision (v0.32.2)

Recipe-guide removal never deactivates gameplay recipes. Every retired gameplay recipe must be removed from its actual source JSON (via a versioned cleanup script when shipping a patch-only ZIP), excluded from server `RecipeManager` loading when stale data exists, and excluded from workstation lookups where feasible.

Retired IDs: all ten `*_stripped_log_from_slabs`; `loose_dirt_from_slabs`; `loose_mud_bricks_from_slabs`; `sanded_wood_slabs_from_grinded`; plus two previously retired personal recipes `primitive_wooden_sheet_from_stripped_log` and `personal_pmi_sheets`.

Reason: 2 Planks -> 6 Wood Slabs at the Hardwood Table coupled with 2 Slabs -> 1 Stripped Log enables material multiplication. Likewise, 2 Loose Dirt/Loose Mud Brick blocks -> 6 slabs coupled with 2 slabs -> 1 block produces 3 blocks from 2. Finally, 3 Sanded Wood Slabs + Flint File -> 2 Grinded Slabs coupled with 1 Grinded -> 2 Sanded yields four Sanded from three.

Preserve non-profitable reverse conversions (e.g., Fireclay Slabs/Blocks), approved forward woodworking and upgraded station rates, and never loosen mining/tool restrictions to fix recipe math. The shared runtime retirement policy is a backstop for stale resources, not a general ban on converting slabs back to blocks.

## Validation

Run `python tools/validate_0322.py` on a cleaned source folder. Test the actual Clay and Wooden Grinder menus on a loaded server. Prior `validate_0321.py` describes the previous release and is not the validator for this release.
