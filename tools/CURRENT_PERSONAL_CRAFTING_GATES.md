# Personal Crafting Release Gates — v0.31.64-b

These are **active** recipes, irrespective of legacy historical retirement lists:

- `wooden_axe.json` — personal 2×2, Tannin + Leather Sheet + Stick + Wooden Sheet.
- `wooden_pickaxe.json` — personal 2×2, Stick + String + Leather Strap + Wooden Sheet.
- `wooden_shovel.json` and `wooden_shovel_<wood>.json` — personal 2×2, Plank + Stick + Leather Strap.
- `wooden_sword_personal.json` — personal 2×2, 2 Wooden Sheets + Stick.
- `wooden_hoe_personal.json` — personal 2×2, 2 Wooden Sheets + Stick + Plant Tool Binding.
- `oak_sheet_from_plank.json` — personal Bone Knife/Plank → 3 Sheets.
- `oak_board_from_plank.json` — personal Flint File/Plank → 1 Board.
- `oak_planks_from_log.json` and alternate woods — personal Log + qualified Axe → 2 Planks. `plank_cutting_axes` must include Flint Axe, Wooden Axe and Hardwood Axe to avoid circular requirements.

The Hardwood Table has different station-owned versions for all wood types, including 4 Sheets / 2 Boards, and vanilla-shaped wooden tools. Do not register the personal Oak entries as `projectflint:hardwood_durable` or remove them in `RecipeManagerMixin`. Test recipe loading separately from in-game guide display.
