# ADR 0004 — Remove vanilla surface civilization from the primitive opening

- Status: Accepted
- Introduced: 0.1.0-alpha.3

## Context

The intended opening represents a player with extremely limited technology in a world where progress is earned through physical processes. Vanilla villages, traders, outposts, temples, wrecks, portals, and similar structures provide finished shelter, loot, crafted materials, and implied civilization that can bypass or contradict that premise.

## Decision

New Overworld generation suppresses major vanilla surface/civilization structures. Selected civilization NPCs are also removed when loaded.

The initial removed set includes villages, outposts, major surface temples/huts, mansions, wreck/ocean surface loot structures, buried treasure, trail ruins, and ruined portals.

Underground structures are deliberately retained for now. Their role will be decided when mining/cave progression exists rather than being removed without gameplay context.

## Consequences

- Fresh worlds better communicate Project Flint's premise immediately.
- Vanilla loot shortcuts become less dominant.
- Existing/generated chunks are not retroactively rewritten and require a fresh-world playtest.
- Structure suppression becomes a compatibility surface that must be revisited if another mod adds desired world content.
- Project Flint will eventually need its own environmental/story structures so the surface does not become empty rather than primitive.
