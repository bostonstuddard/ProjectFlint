# Project Flint bear/environment implementation reference — v0.28.1-a

- Bear textures are canonical user-provided files: `textures/entity/black_bear.png` and `brown_bear.png`; do not redraw or recolor them.
- Custom bears subclass vanilla Polar Bear so they reuse Mojang's bear model/animations.
- Black bear natural spawn weight: 2, groups 1-2. Brown bear weight: 1, groups 1-2.
- Aggro/flee radius: 8 blocks.
- Polar bear movement base: 0.3125 (25% above 0.25).
- Soaked is a registered Project Flint mob effect marker; hidden vanilla Slowness/Hunger instances supply the requested gameplay modifiers.
- Terrified is a registered Project Flint mob effect marker; hidden vanilla Blindness/Mining Fatigue/Weakness instances supply the requested modifiers.
- Terrified damage begins after 300 ticks of continuous <5 local brightness.
