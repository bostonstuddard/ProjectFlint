# ADR 0026 — Preserve Wooden Grinder as Wooden Sheet Source

**Status:** Accepted in v0.31.67-b.

The existing Wooden Grinder has a recipe that uses one Wooden Beam and a Flint File to produce four Wooden Sheets. The File is returned with one point of durability used. The v0.31.66-b stripped-log-and-Bone-Knife personal crafting route was created based on an incorrect assumption that no pre-Planks route existed.

**Decision:** Remove that additional personal crafting recipe, its unused tag, and guide entries. Keep the Wooden Grinder behavior unchanged. Only Wooden and Hardwood Axes produce Planks from Logs; the Flint Axe does not.

**Future changes:** Trace the full existing workstation and tool progression before inserting an emergency early-game recipe. Validate that a fresh survival world can actually reach the Wooden Grinder's input materials; do not infer the route is viable solely from the existence of a recipe.
