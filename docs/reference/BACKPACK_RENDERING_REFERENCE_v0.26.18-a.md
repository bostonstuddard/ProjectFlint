# Backpack Rendering Reference — v0.26.18-a

Authoritative rules for Project Flint backpacks:

1. `items/reed_backpack.json` and `items/leather_backpack.json` are ordinary inventory/held item model definitions. Do not add display-context selectors.
2. Worn rendering happens only in `ReedBackpackLayer` using a copied equipped stack.
3. That copied stack receives `DataComponents.ITEM_MODEL` pointing to either `reed_backpack_worn` or `leather_backpack_worn`.
4. `models/item/leather_backpack_worn.json` must keep the exact same geometry/display structure as `reed_backpack_worn.json`. Only texture/material bindings may differ.
5. Never replace the normal Leather Backpack item model to fix worn rendering.
