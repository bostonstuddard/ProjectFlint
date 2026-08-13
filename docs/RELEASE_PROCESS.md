# Release Documentation Process

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
