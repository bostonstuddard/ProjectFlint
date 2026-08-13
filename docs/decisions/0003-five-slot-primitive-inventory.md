# ADR 0003 — Five-slot primitive inventory

- Status: Accepted
- Introduced: 0.1.0-alpha.3

## Context

Minecraft gives every player 36 ordinary storage slots immediately, before the player has produced any container, garment, pack, or storage technology. That undermines Project Flint's capability-first opening and makes later portable storage mostly a convenience upgrade instead of a technological breakthrough.

## Decision

A survival player begins with only hotbar slots 1–5 as general item storage.

Hotbar slots 6–9 and main-inventory indices 9–35 are unavailable. The offhand is intentionally retained because the existing stone-working interaction manipulates a Rock in each hand. Creative mode retains normal capacity for development.

The rule is server-authoritative. Illegal stacks are moved into legal storage when possible and dropped when no legal space exists; they are never silently destroyed.

The personal 2x2 crafting grid and inventory recipe-book access are disabled during this primitive state.

Starting in alpha.22.6, wearing the infinite-durability Reed Backpack in the chest slot expands the usable hotbar from five slots to all nine. Removing it immediately restores the five-slot limit, with excess stacks moved into legal slots or dropped using the existing server-authoritative enforcement. The backpack grants 0.5 armor and occupies the chest equipment slot, making portable capacity an explicit equipment choice.

## Consequences

- Carrying capacity becomes meaningful progression space for future portable storage.
- Early gathering forces resource-selection decisions.
- Project Flint systems must use primitive-aware item insertion instead of assuming vanilla inventory capacity.
- Container/menu interactions require enforcement in addition to visual masking.
- Five slots are the opening state; wearable storage demonstrates the capacity-expansion architecture.
