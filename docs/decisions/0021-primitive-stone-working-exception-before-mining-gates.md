# ADR 0021 — Primitive Stone working must precede generalized mining restrictions

**Status:** Accepted in Project Flint v0.31.62-b.

## Context
The v0.31.51-b+ generalized ore/stone restrictions ran before the original Crude Stone Hammer's special Stone conversion. The mining preview returned zero progress for an early hammer, both attack and pre-break checks denied Stone, and the server's ordinary mining branch intercepted the original Stone-working handler. That made the first tools unable to produce Crushed Stone and four Rocks, blocking intended early progression.

## Decision
Keep **physical Stone processing** distinct from **pickaxe-tier mining**. Centralize its exact permission in `OreMiningRules.canPrimitiveStoneWork`: only natural vanilla `Blocks.STONE` with Crude Stone Hammer, Crude Multitool, Flint Multitool or Advanced Flint Multitool. Reuse this predicate in the client progress, attack, pre-break, and completed-break handlers. The action takes the established `GameplayTuning.CRUDE_HAMMER_STONE_PROGRESS_PER_TICK` scaled by each tool's existing Hammer speed multiplier and converts the block to `ModBlocks.CRUSHED_STONE`, drops exactly four Rocks, costs one durability, plays the established sound and awards the existing milestone. The Hardwood Hammer is **excluded** from special conversion and continues its wooden-tier mining behavior.

Do not add these primitive tools to `OreMiningRules.isWoodenTier` or make them capable of mining the whole stone family or coal/metal ores. No changes to Utility Vest permissions. Generic restrictions remain server-authoritative and separate from a primitive conversion.

## Regression requirements
A source validator must ensure the permission reaches all four paths and that the conversion precedes generic loot. A live survival test must demonstrate breaking natural Stone with Crude Stone Hammer (including drops and tool wear), followed by negative tests for bare hands, food, Vest, Cobblestone, Deepslate and ore. Check analogous Multitools and later pickaxes. The static checks are not evidence of a successfully played opening.
