# Project Flint placed-texture reference — v0.27.0-a

For crafting piles/assemblies, do not create or remap a separate texture when an item texture already exists. Models should reference the canonical item texture directly.

Canonical replacements introduced in v0.27.0-a include:
- Burnt Branch → `projectflint:item/burnt_branch`
- Burnt Twig → `projectflint:item/burnt_twig`
- Lightly Burnt Twig → `projectflint:item/lightly_burnt_twig`
- Crude Reed → `projectflint:item/crude_reed`
- Crude Plant String → `projectflint:item/crude_plant_string`
- Plant Fiber → `projectflint:item/plant_fiber`
- Plant Tool Binding → `projectflint:item/plant_tool_binding`
- Rock → `projectflint:item/rock`
- Very Refined Stone → `projectflint:item/very_refined_stone`
- Branch → `projectflint:item/branch`
- Twig → `projectflint:item/twig`
- vanilla Flint → `minecraft:item/flint`

The configurable Place In World system already renders the real ItemStack model and should not have a separate texture variant.
