# ADR 0009 — Physical Ground Assembly Before Generic Crafting

## Status
Accepted.

## Context
Project Flint's opening progression intentionally removes the personal crafting grid. The first post-fire technologies now need a way to combine several primitive materials without falling back to invisible shapeless recipes.

The immediate processes are small and tangible:

- two Plant Fibers worked with a Refined Rock;
- two Lightly Burnt Twigs plus a Rock worked with a Refined Rock;
- four Crude Reeds plus two Crude Plant Strings worked with the Crude Stone Hammer.

## Decision
Represent these first assemblies as low, visible stateful blocks placed directly in the world.

The player places each component onto the same ground position, sees the arrangement build up, and then applies the required processing item by interacting with the pile.

The first implementations remain narrow domain blocks rather than introducing an all-purpose recipe framework immediately. Their shared behavior will be used to discover the correct abstraction before a generic `ProcessingRecipe` / assembly system is introduced.

## Why
This preserves Project Flint's core design language:

- physical work instead of menu recipes;
- components visibly exist in the world;
- progression is expressed through capabilities and required tools;
- the five-slot inventory remains meaningful;
- future assembly infrastructure can be based on proven interactions rather than guessed requirements.

## Consequences
- `FiberWorkPileBlock`, `HammerAssemblyBlock`, and `BindingAssemblyBlock` temporarily contain process-specific state.
- All assembly blocks use explicit recovery rather than vanilla loot tables.
- The implementation should be consolidated once enough different processes exist to justify a generic definition format.
- Advancements must accompany each major new capability created by these processes.
