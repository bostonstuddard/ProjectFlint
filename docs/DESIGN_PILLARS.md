# Design Pillars

These are not flavor notes. Future systems should be rejected or redesigned when they conflict with these principles without a strong documented reason.

## 1. Capability before tier

Progression should answer **"what can the player physically do now?"** rather than merely increasing a number.

Examples:

- A refined rock permits cutting woody forage.
- Cordage permits composite tools.
- A digging implement permits deliberate soil removal.
- Controlled heat permits drying, cooking, firing, and eventually metallurgy.

A material being "Tier 2" is not, by itself, progression.

## 2. The starting world is mostly read-only

The vanilla assumption that a bare hand can convert terrain into inventory cubes is intentionally removed. Early interaction should be narrow and plausible: gather a loose object, scrape a surface, pull accessible vegetation, work one object against another.

## 3. Processes replace magic recipes where the process matters

The mod should model meaningful transformations instead of hiding every activity in a crafting grid. This does **not** mean simulating every real-world motion. The process should exist when it creates decisions, dependencies, infrastructure, or a feeling of technological accomplishment.

## 4. Realism inspires mechanics; it does not justify tedium

Repetition must communicate labor without becoming an autoclicker test. Randomness should create texture, not endless bad-luck walls. Alpha stone working therefore works in short strike cycles; a completed failed attempt may consume the working Rock, but the system should not allow failure streaks to become the primary progression challenge.

## 5. Operations are explicit

Long-term block and item interaction should be expressed in operations such as:

- scrape
- cut
- split
- hew
- dig
- crush
- grind
- dry
- fire

A future tool grants one or more operations. Blocks/materials define what operations affect them. This prevents the codebase from degrading into hundreds of unrelated `if item == X && block == Y` checks.

## 6. Infrastructure should be physically constructed

Important production technology should usually exist in the world: work surfaces, drying areas, hearths, kilns, bellows, frames, mills, and later mechanical networks. One-block magic machines are an abstraction that needs justification.

## 7. Biomes change the route, not the destination

Different environments should provide different early forage and constraints while keeping viable progression paths. A spawn biome should influence *how* the player solves the opening problem, not randomly make the save impossible.

## 8. Systems before content volume

Build reusable heat, tool-operation, processing, construction, and mechanical-network systems before creating dozens of items that each require bespoke logic.

## 9. Every release explains itself

Every numbered release gets a Markdown release record containing:

- player-facing changes
- architecture changes
- design reasoning
- tuning constants
- known limitations
- migration notes
- next intended work

The purpose is to preserve *why* decisions were made, not merely a changelog of file names.


## 10. Feedback must represent capability

Minecraft's normal cracking animation tells the player that persistence will eventually break a block. Project Flint must not show that feedback when an action is physically impossible. Valid primitive processes should use world-space or HUD feedback that communicates real progress; debug/action-bar prose is not a substitute for finished interaction design.

## 11. Temporary art is tracked, not hidden

Fast iteration may use disposable placeholders, but every non-final asset must have explicit provenance and a replacement target. A temporary asset that is not tracked is a future accidental dependency.


## 12. Inventory space is technology

Vanilla inventory capacity is not assumed to be a free biological property. Opening carrying capacity can be deliberately small, and future storage/carrying equipment should expand that capability through explicit systems. Restrictions must be server-authoritative and must never silently delete player items.

## 13. The world must agree with the premise

Vanilla content that implies finished civilization, freely available industrial materials, or progression shortcuts should not remain merely because it is familiar. World content is evaluated by whether it supports Project Flint's progression. Removal should still be deliberate: unexplored systems such as underground structures can remain until their role is designed instead of being deleted reflexively.

## 14. Progression must teach itself

Project Flint deliberately removes familiar vanilla paths, so advancement guidance is part of the progression system rather than optional decoration.

Every meaningful new player capability or technological breakthrough should be evaluated for advancement coverage in the same release that introduces it. Advancements should:

- mark discoveries and capability unlocks, not every trivial pickup;
- form a readable path through the currently playable progression;
- use concise descriptions that hint at what was accomplished without becoming a walkthrough;
- be updated when progression order changes;
- never lag several stages behind the mechanics they are supposed to explain.

A release that adds meaningful progression but intentionally adds no advancement must explain why in its release record.
