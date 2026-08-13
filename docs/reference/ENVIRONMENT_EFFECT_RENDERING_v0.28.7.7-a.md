# Environment Effect Rendering Reference — v0.28.7.7-a

## Hardcore night
Project Flint must not use a short-distance fog wall for nighttime darkness.

At full night, unlit Overworld sky contribution is allowed to fall to zero through `assets/minecraft/shaders/core/lightmap.fsh`. There is no artificial positive moonlight floor. Block light remains independent, which means distant campfires, torches, lit kilns, and lit structures remain visible normally.

The desired visual rule is therefore:
- unlit terrain: capable of becoming pitch black at midnight;
- actual block-light sources: visible at normal render distance;
- no Project Flint 6–8 block fog cutoff.

Night Vision retains its intended visibility bypass.
