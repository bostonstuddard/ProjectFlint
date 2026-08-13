# Environment Effect Rendering Reference — v0.28.7.3-a

## Terrified
Terrified does not use vanilla Blindness and does not install a blindness-style fog environment.

## True darkness
Project Flint keeps its 1.21.11 lightmap shader override for zero-light handling. Outdoor night adds a separate atmospheric visibility rule:

- full-night fog starts at 6 blocks;
- full-night fog ends at 8 blocks;
- the Project Flint fog base color fades toward black using the same night-strength curve;
- Night Vision bypasses the Project Flint night-fog environment.

The fog layer is presentation-only; it does not change server light values.
