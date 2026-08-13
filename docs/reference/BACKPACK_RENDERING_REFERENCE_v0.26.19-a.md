# Project Flint backpack rendering reference — v0.26.19-a

Known-good rules for backpack rendering:

- The normal inventory/held items remain `reed_backpack` and `leather_backpack`.
- The worn renderer in `ReedBackpackLayer` overrides only the copied equipped stack via `DataComponents.ITEM_MODEL`.
- `reed_backpack_worn` resolves to the existing 3D worn model.
- `leather_backpack_worn` must resolve to a dedicated 3D worn model that uses a block-space texture path.
- The Leather Backpack worn model is a literal copy of the working Reed Backpack worn model, with only the texture references swapped.
- The dedicated worn texture file for Leather Backpack is `textures/block/leather_backpack_worn.png`, copied byte-for-byte from `textures/item/ground_hide.png`.

Do not alter the normal held/inventory item models when changing the worn backpack renderer.
