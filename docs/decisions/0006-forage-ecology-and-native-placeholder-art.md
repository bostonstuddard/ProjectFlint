# 0006 — Forage ecology and native placeholder art

## Status
Accepted in 0.1.0-alpha.4.

## Decision
Hot/arid and shore biomes do not actively generate Project Flint forage bushes. Bush placement must be air directly above dirt-tagged ground and uses a no-leaves heightmap so a shrub cannot be planted onto a tree canopy.

Temporary bush visuals use Project Flint geometry with Minecraft's active leaf textures rather than copied third-party pixels. Loose Rock likewise uses the active stone texture. This means development art automatically follows the player's resource pack and remains visibly temporary.

## Why
Biome-specific IDs are useful, but an ID existing does not imply every biome should contain the same resource. Ecology should constrain progression rather than become decorative noise. Native-texture placeholders also give us better test readability without creating an asset-licensing mess or fighting a tester's resource pack.
