# Backpack Rendering Reference — v0.26.17-a

- Reed Backpack: custom render layer sets `ITEM_MODEL` to `projectflint:reed_backpack_worn`; this is confirmed working.
- Leather Backpack: render layer leaves its registered item model intact and requests `FIXED` context. `assets/projectflint/items/leather_backpack.json` selects `projectflint:item/leather_backpack_worn` for `fixed`.
- Leather worn geometry must remain identical to Reed worn geometry unless intentionally redesigned.
- Shared worn transform: scale `0.85`, translation Y `0.40`, Z `0.28`.
