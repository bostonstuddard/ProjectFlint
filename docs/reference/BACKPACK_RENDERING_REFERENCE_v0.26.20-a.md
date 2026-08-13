# Project Flint backpack rendering reference — v0.26.20-a

## Verified modern Minecraft item-model behavior

- The equipped render layer may override `minecraft:item_model` on a copied ItemStack.
- `projectflint:leather_backpack_worn` resolves through `assets/projectflint/items/leather_backpack_worn.json`.
- That item definition points to `models/item/leather_backpack_worn.json`.
- A separate registered block is not required for the model ID.

## Leather Backpack worn model

The Leather Backpack intentionally inherits the working Reed Backpack model:

```json
{
  "parent": "projectflint:item/reed_backpack_worn",
  "textures": {
    "particle": "minecraft:block/stripped_spruce_log",
    "wicker": "minecraft:block/stripped_spruce_log"
  }
}
```

This means the Leather Backpack has exactly the Reed Backpack's elements and `fixed` display transform. There is no `#worn` key and no custom Leather Backpack texture involved in the worn model.

The normal held/inventory Leather Backpack continues to use `projectflint:item/leather_backpack`.
