# Project Flint v0.28.41-a — Crude Multitool leaf-harvest hotfix

## Fixed
- Restored the Crude Multitool as a valid leaves harvesting tool.
- Crude Multitool leaf harvesting once again produces **4 Twigs + 1 Branch** and costs 1 durability.
- The fix restores the intended early Branch/Burnt Branch progression path without changing Flint Multitool, Flint Plant Cutter, or Advanced Flint Multitool outputs.

## Regression guard
- Release validation now explicitly checks that `ToolCapabilities.canHarvestLeaves(...)` includes `CRUDE_MULTITOOL`.
