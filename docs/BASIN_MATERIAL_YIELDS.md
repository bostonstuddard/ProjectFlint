# Basin Material Processing — v0.31.57-b

This table is authoritative for the current 1.21.11 source (`BasinMaterialProcessingSystem.java` and `BasinBlockEntity.java`). Inputs are physical: the Basin stores a real ItemStack and renders its existing item model. One item is processed at a time; excess matching input can remain queued in the block entity.

Each **valid stomp** costs **1 food point**, including the final stomp. If a survival player has no food points, that stomp is rejected without advancing progress or consuming the input. Creative players do not pay hunger. All processes take **2–3 landed stomps**. Progress is saved with the Basin, synchronized to clients and reset if the current input is removed. Hunger is not charged on mere insertion or extraction.

Every percentage listed under independent rolls is its own random check. Multiple bonuses, including the same dust repeated, may succeed together. Outputs with 1-or-2 at 50/50 are one mutually exclusive quantity selection.

| 1 input | Stomps | Fixed or exclusive output | Independent chance-based outputs |
|---|---:|---|---|
| Wooden Planks (any) | 2 | 1 Wood Dust | — |
| Stripped Oak/Birch Logs/Wood | 3 | 2 Tannin | — |
| Raw Iron | 3 | 1 Iron Dust | +1 Iron Dust 75%; Slag 50%; Gold Dust 3% |
| Raw Copper | 3 | 1 Copper Dust | +1 Copper Dust 83%; +1 Copper Dust 24%; Nickel 10%; Silver 2%; Slag 5% |
| Andesite or Granite | 3 | 1 or 2 Fertilizer (50/50) | Stone Dust 33% |
| Stone or Cobblestone | 3 | None guaranteed | 1 Stone Dust 66%; another 44%; another 13.33% |
| Diorite | 3 | 1 or 2 Stone Dust (50/50) | — |
| Stone Dust | 2 | — | Slag 50% |
| Gravel | 2 | 1 Flint | Quartz Dust 20%; Iron Dust 1% |
| Sand | 2 | 1 Potassium Dust | Tin Dust 15%; Mica Dust 5%; Gold Dust 1% |
| Clay Block (vanilla `minecraft:clay`) | 3 | — | Mica Dust 5%; Rare Earth Dust 3.33%; Silver Dust 0.33%; Gold Dust 2% |
| Rare Earth Dust | 3 | — | Lithium Dust 22.3%; Iron Dust 15%; Aluminum Dust 4.8%; Gallium Dust 0.85% |
| Slag | 3 | — | Iron Dust 33%; Copper Dust 33%; Silicon Dust 33% |

The copper Slag (5%) and iron Gold Dust (3%) rates are new balancing defaults for the user-requested **small chance**, which did not specify exact percentages. Any unlisted output chance is not currently implemented. Up to 3 Stone Dust may emerge from one Stone/Cobblestone input; none is guaranteed.

## Player-facing guide responsibilities

Update the K-key Basin list and the G-key Ore Washing/Dust/Glass chapters whenever a material, roll, or stomp threshold changes. Do not add historic/implementation commentary to player-facing text. Keep this document as the full precise reference.
