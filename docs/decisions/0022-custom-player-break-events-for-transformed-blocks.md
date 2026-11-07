# ADR 0022 — Custom block transformations count as player breaks

Status: Accepted (v0.31.63-b)

## Problem
Minecraft's vanilla player break callback is not invoked when Project Flint intercepts `ServerPlayerGameMode.destroyBlock` and returns `false` after replacing or removing a block. Flint Quests' `BREAK_BLOCK` tasks listen to Fabric's normal AFTER-break event, so a legitimate Crude Stone Hammer conversion `minecraft:stone` → `projectflint:crushed_stone` did not satisfy the Stone-break task.

## Decision
For the intercepted server-side player break operation, capture the *original* state and block entity before processing. After the operation completes, when (a) the injection cancels vanilla handling, (b) the original block is actually removed or replaced at the target position, and (c) the operation is not an unauthorized restricted mining attempt, publish Fabric `PlayerBlockBreakEvents.AFTER` once with the original block snapshot.

Avoid introducing another Flint Quests dependency, a parallel custom event type, or a phantom `destroyBlock` call. Continue to let Fabric deliver the event for normal vanilla successes; do not double-fire them. Only direct player break targets count, not unrelated nearby stone weakened as a side effect. Capture permissions before tool damage/breakage.

## Compatibility and validation
The event has the Fabric 1.21.11 callback shape `(Level, Player, BlockPos, BlockState, BlockEntity)`. The listener in Flint Quests `QuestEngine.register()` derives `TaskType.BREAK_BLOCK` progress from the original state's block registry ID. Static tests check event delivery guards and that the early Stone operation has unchanged drops/required tools/durability. Live integrated-client/Flint Quests verification remains necessary.

## Consequences
Future custom intercepts that remove or replace the player-targeted block automatically notify generic break observers without each gameplay branch adding quest-specific code. No notification occurs for canceled no-op attempts, unqualified ore removal, or ordinary vanilla breaks.
