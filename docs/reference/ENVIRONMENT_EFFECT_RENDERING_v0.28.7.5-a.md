# Environment Effect Rendering Reference — v0.28.7.5-a

## Full-night visibility
- Atmospheric night fog remains black.
- Full-night fog starts at about 6 blocks and finishes at about 8 blocks.
- The lightmap shader retains a 0.06 minimum hardened sky factor so nearby sky-exposed terrain is barely visible instead of becoming absolute RGB zero.
- Underground zero-sky-light locations remain unaffected by that sky floor.
- Night Vision continues to bypass the Project Flint atmospheric night fog.

## Darkness damage
Qualifying low-light exposure is tracked server-side. Damage now begins only after 45 seconds of continuous qualifying darkness.

## Crude Plant Torch
The placed torch emits block light level 6. The carried item does not add a client-only dynamic-light renderer; it becomes a real world light source when placed.

## Carried torch survival light
A carried Crude Plant Torch counts as effective local light level 6 for the server-side Terrified/darkness check. Its placed form supplies actual block light level 6; carried rendering does not mutate chunk block-light data.
