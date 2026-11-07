# ADR 0028 — Hardwood worktable batch and restricted dynamite reach

**Status:** Accepted for v0.31.69-b.

The 3x3 Hardwood Table has a separate, more expensive Hardwood recipe (`DDD / DWD / TTT`) made from 5 Wood Dust, a filled bucket of water, and 3 Tannin. Both vanilla and Clay buckets are accepted, and their empty containers return; the existing personal Hardwood recipe is preserved.

Weak Dynamite is intended to affect a few adjacent ore and rock blocks, not perform unrestricted excavation. Its block-damage radii become 3 (ore) and 2.25 (stone-family), with maximum four regular ore and two rock blocks destroyed. Stone retains a 65% destruction chance. Keep the selective block allowlist, deepslate protection, 1.25x independent Basin outcomes with overroll, 60-tick fuse and existing entity damage behavior.

Future tuning must coordinate `DynamiteChargeBlockEntity`, Hardwood recipe JSON, K-key crafting reference, G-key Field Guide, and release documentation. Do not infer any requirement to add other stone/ore types or boost material yields.

Vanilla Packed Mud, Mud Bricks, and Mud Brick building shapes share Fireclay Brick tool permissions and recover their original blocks without Silk Touch. Never grant recovery to bare hands or the Utility Vest alone.
