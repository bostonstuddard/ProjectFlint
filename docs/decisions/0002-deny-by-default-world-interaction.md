# ADR 0002 — Survival world manipulation is deny-by-default

Status: accepted

## Decision

In survival, normal block breaking is denied unless Project Flint explicitly grants that interaction. Contextual primitive operations can still alter blocks through their own systems.

## Why

The core fantasy begins before Minecraft's usual "punch tree" capability. A blacklist of forbidden blocks would encode the wrong default and would become incomplete as content grows.

An additive model also maps naturally to the planned tool-operation architecture: technology grants `cut`, `dig`, `split`, `scrape`, etc.

## Consequences

- compatibility with unrelated mods will require deliberate integration rather than accidental vanilla breakability;
- player feedback for failed operations becomes important;
- tool introduction must update a shared capability layer, not poke exceptions into event code;
- creative remains unrestricted for development/building.
