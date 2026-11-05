# Basin — exact material yields and stomp effort (v0.31.56-b)

Insert an input to see its exact item model sitting physically in the Basin. Land on it repeatedly until the required number of stomps completes. Each completed input costs **1 hunger point** (not one per stomp). Removing an input resets progress; partial stomps persist in block-entity saves. All chance-based extras are **independently rolled**, so more than one may appear. For fixed 1/2 outputs, the amounts are equally likely and mutually exclusive.

| Input (1) | Guaranteed or exclusive base | Independent bonus rolls |
|---|---|---|
| Raw Iron | 1 Iron Dust | +1 Iron Dust 75%; +1 Slag 50% |
| Raw Copper | 1 Copper Dust | +1 Copper Dust 83%; +1 Copper Dust 24%; +1 Nickel Dust 10%; +1 Silver Dust 2% |
| Andesite / Granite | 1 or 2 Fertilizer (50/50) | None |
| Stone / Diorite | 1 or 2 Stone Dust (50/50) | None |
| Stone Dust | None | Slag 50% |
| Gravel | 1 Flint | Quartz Dust 20%; Iron Dust 1% |
| Sand | 1 Potassium Dust | Mica Dust 5%; Gold Dust 1% |
| Slag | None | Iron Dust 33%; Copper Dust 33%; Silicon Dust 33% |

Wood processing is retained: every Plank -> 1 Wood Dust, and stripped Oak/Birch Log/Wood -> 2 Tannin.

In particular, Slag can yield zero, one, two, or three dust types. No byproduct is exclusive of another, except each fixed 1-versus-2 base quantity.

## Stomps to complete one input

| Input material | Landings required |
|---|---:|
| Sand | 2 |
| Wooden Planks (any) | 2 |
| Gravel / Stone Dust | 2 |
| Stripped Oak/Birch Logs or Wood | 3 |
| Slag | 3 |
| Raw Copper | 4 |
| Andesite / Granite / Stone / Diorite | 4 |
| Raw Iron | 5 |

Every completed input charges exactly one hunger point in Survival. A failed attempt with no available hunger at the final stomp leaves the partial progress intact; Creative players do not consume hunger. Additional inputs in the same Basin remain in a stack but are worked individually. Input is rendered at the basin floor and synced to clients, including multiplayer joins.
