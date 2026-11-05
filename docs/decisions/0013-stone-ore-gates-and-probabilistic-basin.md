# ADR 0013 — Locked Deepslate and independently rolled Basin yields

Status: Accepted (v0.31.55-b).

## Context

Vanilla Deepslate Ore and ordinary Copper Ore inherited higher-tier break/drop rules that enabled premature progression, while player-facing tools and station interfaces described different material quantities. The Basin previously only supported Planks and two stripped-log types.

## Decision

`OreMiningRules.isForbiddenDeepslate` blocks survival mining of all vanilla Deepslate family blocks and Deepslate Ores by any held tool. That early progression lock is checked by visual break progress, attack callbacks, server pre-break and authoritative block destruction. In the Hardwood tier, Copper and Iron Stone Ore each give one Raw Ore from the Hammer; Coal gives two. Vanilla upgrades still follow the specified tier rules for non-Deepslate blocks.

`BasinMaterialProcessingSystem` is the processing authority for raw Iron/Copper, rock, Gravel, Sand, Stone Dust and Slag. Each candidate byproduct has an **independent** roll. Guaranteed primary outputs and mutually exclusive 1-or-2 outcomes are explicit. Slag may yield no output (all three rolls fail). Every processed input consumes one hunger point, matching Planks.

## Consequences

Future recipes, quests, guide rows and technical tables must cite *the same* output chances. Any reduction of ore/stone gates needs a new decision record and testing on both clients and servers. No unrelated vanilla smelting recipes may leak into the vanilla pool.
