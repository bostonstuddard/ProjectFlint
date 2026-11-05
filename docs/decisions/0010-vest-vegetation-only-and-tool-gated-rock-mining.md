# ADR 0010 — Only held tools grant stone mining; Utility Vest is vegetation-only

Status: accepted (v0.31.54-b). Supersedes general recovery privileges described in v0.31.39-b, v0.31.40-b and v0.31.42-b; retains the deny-by-default foundation of ADR 0002.

## Decision

1. Armor cannot grant rock/ore/structural breaking permission. The **Tanned Utility Vest** only unlocks documented plant/vegetation hand clearing. A player must still use a tool for Well Beams and Pulleys, workstations, rock and stone-built structures.
2. Vanilla pickaxe-mineable Minecraft blocks (excluding ores) are treated as a single restricted stone/rock family. This covers terrain and placed variants: Stone, Cobblestone, Diorite, Andesite, Granite, Tuff and other vanilla pickaxe targets. Restrict only vanilla block-registry content here and explicitly exclude vanilla Bricks to preserve the existing dedicated brick recovery path. Custom Project Flint multiblock/brick handlers remain unchanged.
3. `OreMiningRules` owns both the tool permission and speed. Ineligible hands/items get **zero** progress on restricted rock, not 1 tick followed by a server cancellation. Authorized wooden-tier speed is 270 ticks; Hardwood Hammer is 240 ticks. Appropriately tiered vanilla pickaxes retain their ordinary vanilla speed; no early-age item gains a higher mining tier by wearing a vest.
4. Ores remain explicitly tiered: Stone/Coal by wooden-tier tools and vanilla pickaxes; ordinary Iron/Copper Ore by the Hardwood Hammer or vanilla Stone+ pickaxes; other ores require vanilla pickaxes appropriate to their tier. The Hammer yields two Coal from Coal Ore. Unsupported ores retain slow 900-tick removal with no drops, unlike the completely disallowed rock interactions.
5. Both client attack handling and server pre-break guard consult the same predicate. `ServerPlayerGameModeMixin` must gate rewards and hand-offs using the same rules; it may not use unrestricted hardness or placement detection as authorization.
6. The persistent player-placement ledger is retained as a legacy save format, but its general-purpose recording Mixin is no longer registered and no generic player-placed salvage is permitted by the vest. Future recovery paths must be explicit per tool/block.

## Reasoning

The vest's hardness-based placement preview treated virtually all low-hardness rock as immediately mineable on the client. The server then rejected the action, creating fast cracking and ghost breaks. A block’s placement history or low hardness does not make stone hand-breakable. Requiring tool-held permission is easier to reason about and preserves progression.

## Consequences

- No more Stone, Cobblestone, Diorite, Andesite or Granite hand mining from wearing the vest.
- Previous Vest-based Well dismantling is deliberately discontinued. Use the specialized Well tooling and salvage handlers instead.
- New tool tiers must update `OreMiningRules` once and be tested in client progress, `AttackBlockCallback`, `PlayerBlockBreakEvents.BEFORE`, and `ServerPlayerGameModeMixin`.
- Modded block families need explicit compatibility handling; do not silently grant them stone permissions.
- Earlier release notes remain as historical records; the current behavior is described here and in `docs/CURRENT_SYSTEMS.md`.
