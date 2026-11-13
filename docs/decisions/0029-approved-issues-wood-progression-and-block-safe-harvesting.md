# ADR 0029 — Approved 0.32.1 Gameplay Corrections

Status: accepted (2026-11-12). Applies to Project Flint 0.32.1.

1. **Tool permission is authoritative and non-destructive on denial.** Unqualified tools must not produce mining cracks that complete or delete ore blocks. Keep the Crude Hammer's natural-Stone transformation and legitimate high-tier pickaxe paths unchanged.
2. **Paired harvests yield once.** Both halves of a tall flower are treated as one object. A transformed/removed plant must not spawn a second detached-half drop.
3. **Safe retrieval remains an explicit whitelist.** Placed String/Tripwire may be reclaimed with hands; stone, ore, masonry and other blocks are not added to the hand-recovery whitelist.
4. **Station gating is intentional.** Bone Knife Unfired Clay Bucket moves to Hardwood Table, with durable remainder; the older Wooden Grinder recipe must not reappear. Slab-to-log conversions are removed across wood variants to avoid duplication.
5. **A current recipe definition is the source of truth.** K-key recipe guide entries mirror actual recipes. Player-facing Field Guide gives concise, correct progression guidance, never patch explanations.
6. **Proposed changes are not implemented changes.** Broader cooking rebalance (#25) and byproduct uses (#17) remain in `docs/proposals` until individually approved.
7. **Versioning exception for bundled release line.** Release 0.32.1 is explicitly named by the owner, without the older -b source suffix. Preserve exact three-component identifiers.

Regression requirements: 2x2 versus Hardwood 3x3 crafting, tool wear, Woven Wood outputs, early Stone processing, custom break notifications, Sugar Cane with adjacent water, and successful Windows cleanup before Gradle.
