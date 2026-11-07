# ADR 0024 — Forge wood batches and Hardwood mud crafting

**Status:** Accepted for v0.31.65-b.

## Decisions
- Four burnable Logs in the Forge's fuel slot form a *valid processing batch* even with an empty material-input slot. With a formed structure, usable Firestarter and Charcoal output space, the next full 60-second Log batch may automatically ignite. The `ignite()` preflight is shared with charcoal-based alloy/glass cycles. Charcoal fuel itself never burns when there is no valid material input or output room. Keep no partial wood batches.
- The Hardwood Crafting Table owns new Mud, Packed Mud, Mud Bricks, Dirt slabs/stairs, Mud Brick slabs/stairs/walls and Dirt/Loose Soil 3x3 recipes. The early 2x2 grid does not gain these progressions. Filled Clay or vanilla Water Buckets become empty rather than disappearing.
- Mud Bricks convert into Loose Mud Bricks, never the reverse. Distinguish this from the previous Wooden Grinder path, which is cheaper now.
- A Packed Mud surface yields a 1.10 speed factor and tells the player. It must not apply its bonus off the block.
- Time cost justifies better yields: personal Leather Strap 2 vs Table 3, personal Repair Kit 1 vs Table 2, personal Clay Block 4 balls vs Table 3.
- Never gate natural Stone Hammer processing while introducing these building shapes; Dirt Slabs/Stairs remain tool-gated for recovery.

## Future release regression tests
1. Log-to-Charcoal produces four outputs per charge, automatically relights another eligible batch, and pauses safely on full output or missing firestarter.
2. Charcoal cannot burn for an empty recipe or when the primary output slot is full.
3. All new recipes appear **only** at intended workstation and return emptied water buckets.
4. Mud Bricks cannot be recovered from Loose Mud Bricks via any newly added conversion recipe.
5. G/K guides show final player-facing behavior without development notes.
