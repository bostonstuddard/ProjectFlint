# Minecraft 1.21.11 display-context reference

Project Flint v0.26.17-a uses the same item-definition mechanism vanilla 1.21.11 uses for context-specific item models: `minecraft:select` with `property: minecraft:display_context`, explicit cases, and a fallback. Leather Backpack uses `fixed` for its worn 3D model because the custom player render layer requests FIXED context.
