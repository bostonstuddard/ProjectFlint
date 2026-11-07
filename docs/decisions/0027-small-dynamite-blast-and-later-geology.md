# ADR 0027 — Weak dynamite is not a mining replacement (v0.31.68-b)

## Context
The original 2.75-block, uncapped weak dynamite blast removed too much terrain and multiplied every Basin drop chance by 1.5. Wooden-tier mining broadly allowed miscellaneous vanilla pickaxe-minable geology, including dripstone.

## Decision
Keep Weak Dynamite as a short-range, capped prospecting aid. The blast is restricted to a maximum of **two** ordinary Coal/Iron/Copper Ore blocks within **1.75** blocks, plus at most **one** Stone/Cobblestone/Andesite/Diorite/Granite block within **1.25** blocks, with a separate **50%** chance to break each eligible stone candidate. Shuffle targets to avoid directional bias. Entity damage is 12 within 1.75 blocks, and the fuse remains 90 ticks. Deepslate and all other terrain/machines are unaffected.

Blast material yields always use the current Basin material distributions with a **1.2× relative multiplier**. Keep independent rolls and guaranteed portions over 100%; do not clamp to 100%. The Hardwood Table recipe uses `PFP/PRP/CCC`, requiring 4 Black Powder, 1 Fuse, 1 Red Dye, 3 Copper Dust.

Geology access is handled centrally: `OreMiningRules.isAdvancedGeology` denies early wood-tier tools, without changing Stone, Cobblestone, Andesite, Diorite or Granite access or the primitive Stone Hammer transformation. Include Dripstone/Pointed Dripstone, Tuff variants, Calcite, Basalt/Blackstone, Amethyst and specified later rocks. Do not gate ordinary crafted utility blocks, and keep Deepslate entirely locked.

## Follow-up validation
Launch client and dedicated server; exercise blast caps, target fairness, independent overrolls, late geology restrictions, Crude Stone Hammer conversions, and the new recipe. Structural source checks are not a substitute for a live playtest.
