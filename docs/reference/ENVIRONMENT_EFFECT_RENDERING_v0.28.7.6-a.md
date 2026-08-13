# Environment Effect Rendering Reference — v0.28.7.6-a

## Soaked / Drenched
These effects own their gameplay penalties directly and should not create unrelated vanilla HUD effect entries.

## Terrified
Terrified owns no Blindness effect and no blindness-style fog rendering.

## Hardcore darkness
Hardcore night darkness is **lightmap-only**.

Do not impose a Project Flint 6–8 block atmospheric fog cutoff. The intended presentation is:
- unlit terrain becomes nearly black at full night;
- nearby faint moonlit terrain can remain barely readable through the configured sky-light floor;
- actual block-light sources remain visible at distance;
- campfires, torches, lit kilns, and illuminated structures can therefore act as navigation landmarks;
- Night Vision retains its intended visibility behavior.

`ProjectFlintNightFogEnvironment` and `FogRendererMixin` are retired as of v0.28.7.6-a.
