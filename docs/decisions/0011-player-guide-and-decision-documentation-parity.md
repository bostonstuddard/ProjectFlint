# ADR 0011 — Keep implementation, player guides, and technical records in sync

Status: accepted (v0.31.54-b).

## Decision

Every gameplay patch must review (a) server implementation and client feedback; (b) Field Guide chapters if instructions, progression or exact quantities change; (c) the K-key Recipe Guide when a craft/station/input/output changes; (d) the current systems document, architecture, progression matrix, roadmap and related ADRs; and (e) the release record and automated regression checks. Only update a guide when that guide actually covers the changed feature; do not invent new recipes for a mining-only fix.

Player guides contain only present-tense player information and **no** developer commentary, previous versions, changelogs, implementation notes or hypothetical features. Historical rationale belongs in `docs/releases` and `docs/decisions`. `docs/CURRENT_SYSTEMS.md` records the latest authoritative technical behavior and supersedes historical snapshots when they conflict.

## Consequences

- `docs/RECIPE_GUIDE_RELEASE_CHECKLIST.md` gains a broader current-guide parity check.
- A release cannot claim gameplay verification from JSON validation alone; compilation/runtime testing must be stated honestly.
- Decisions that contradict earlier accepted mechanics require an explicit superseding ADR (as v0.31.54-b does for generic vest salvage).
