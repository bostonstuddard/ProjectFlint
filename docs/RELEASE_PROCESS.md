# Release Documentation Process

## v0.31.55-b additional release checks

- For probabilistic outputs document independent/mutually-exclusive rolls, total yield bounds, guaranteed versus optional results, and where hunger/tool/water cost applies.
- For new effects check every exact height and light boundary and server-authoritative removal; status icon and localization must be present.
- For decor salvage explicitly list safe blocks and preserved contents; any new whitelist entry requires ADR review.
- For a new placeable craft include model, item definition, loot table, blockstates for all orientations, item group, G/K guide references, and regression checks.


Version identifiers must follow [`VERSIONING.md`](VERSIONING.md). New releases may never add a fourth numeric component.

Every release, including internal alphas, must add `docs/releases/<version>.md`.

A release document must contain:

1. **Intent** — the problem this release was supposed to solve.
2. **Player-facing behavior** — what can actually be experienced.
3. **Architecture** — new systems/interfaces/data boundaries.
4. **Design reasoning** — why this implementation was chosen.
5. **Tuning snapshot** — meaningful constants in that release.
6. **Assets/licenses** — anything imported, removed, or relicensed.
7. **Advancement coverage** — which new/changed progression milestones are represented in the Project Flint advancement tree, or why no advancement change is appropriate.
8. **Known limitations** — especially temporary architecture.
9. **Validation** — what was compiled/launched/tested.
10. **Next release pressure** — what this release intentionally leaves unresolved.

Also update:

- `CHANGELOG.md`
- `docs/ROADMAP.md`
- `docs/ASSET_SOURCES.md` when assets change
- an ADR in `docs/decisions/` for choices that would be expensive to reverse

Release documents are historical records. Do not rewrite old reasoning to make later decisions look inevitable; append corrections or superseding ADRs instead.

Advancement coverage is a release gate for progression work. A mechanic that introduces a meaningful new capability is not considered fully integrated until the advancement tree has been reviewed alongside it.


## Ground interaction reversibility

Any release that introduces a physical ground assembly or primitive stockpile must explicitly test:

- recovery of incomplete components;
- behavior when the five-slot inventory is full;
- correct count/state reduction;
- no silent deletion of materials;
- no accidental conversion into vanilla block drops.


## Current-state and guide parity (ADR 0011)

Every release must review `docs/CURRENT_SYSTEMS.md`, `docs/ARCHITECTURE.md`, `docs/ROADMAP.md`, `docs/PRIMITIVE_PROGRESSION_MATRIX.md`, G-key Field Guide chapters and K-key Recipe Guide records. Only modify an in-game guide when the release changes relevant player behavior. Disagreements with previous accepted mechanics require a superseding ADR, not silent historical edits. For mining changes, source and server permission paths must share a predicate, and client/server test coverage must include natural and placed stone, the Utility Vest, and unrelated tools.

## v0.31.56-b additions — Workstation item state and custom recipe type

- For physical workstations check block-entity save/load, network update packet/tag, rendered item correctness for all accepted input types, and no stale static model geometry.
- For timed crafts using custom recipe types check vanilla result slots' **hard-coded recipe lookups**. Use matched custom recipe for all remainders; test durable tools, emptied containers, filled grids, full inventory, shift-click, and cooldown reset.
- For repeated actions require documented thresholds, cooldown, persistence, per-action cost timing, refund/removal behavior, server ownership, and a guide update.
