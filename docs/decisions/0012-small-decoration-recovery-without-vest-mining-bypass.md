# ADR 0012 — Explicit hand recovery for lightweight placed decorations

Status: Accepted (v0.31.55-b). Supersedes the overly broad implication that all player-placed block recovery is disallowed in ADR 0010.

## Context

Restricting every recovery path successfully closed fast rock-breaking exploits, but also prevented players from reclaiming small placed Torches and Chicken Nests. The prior hardness-driven player-place ledger was too broad and required wearing a Tanned Utility Vest.

## Decision

Declare a hard whitelist of light decor in `PlayerPlacedBlockLedger.isHandRecoverableDecoration`: vanilla Torch variants, Flint and Fat Torches, the Chicken Nest and the Axle. Each is recoverable by hand with no Vest. Server pre-break and authoritative `destroyBlock` callbacks agree with client progress; Nest contents eject exactly once. A wall Torch must return the corresponding standing Torch item. The old persisted ledger remains read-compatible but is not consulted for arbitrary material recovery.

Never infer hand-recoverability from `hardness`, `placed` provenance alone, armor, or tool enchantments. Fireclay, Kiln, Forge, Well, Furnace, other storage blocks, and vanilla stone continue their specialized, tool-gated paths.

## Consequences and regression gate

- Test wall-mounted/standing lit/unlit Torches, a filled Nest, and all three Axle orientations; no duplication or fuel reset.
- Test bare-hand and Vest + bare-hand Stone/Cobblestone/Andesite and Fireclay blocks: no cracking shortcuts.
- Any newly hand-recoverable item must be separately reviewed and listed in this ADR's successor.
