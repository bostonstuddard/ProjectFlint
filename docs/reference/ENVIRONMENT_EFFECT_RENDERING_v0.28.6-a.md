# Environment Effect Rendering Reference — v0.28.6-a

## Soaked
Do not add vanilla Slowness or Hunger instances to represent Soaked.
The single `projectflint:soaked` effect owns its movement-speed modifier and hunger-style exhaustion.

## Terrified
Do not add vanilla Blindness, Mining Fatigue, or Weakness instances to represent Terrified.
The single `projectflint:terrified` effect owns its attribute penalties.
Client fog mixins make Terrified trigger constant full-strength blindness-style fog without depending on a vanilla Blindness effect.

## True darkness
`ProjectFlintHardcoreDarkness` replaces the final 16x16 client lightmap after vanilla renders it.
Gamma is multiplied by the real light strength before being applied, so zero-light texels remain black.
