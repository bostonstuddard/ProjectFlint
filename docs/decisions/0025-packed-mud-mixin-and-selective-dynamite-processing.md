> **Superseded in v0.31.67-b:** The temporary Stripped Log + Bone Knife -> Wooden Sheet personal recipe described below was removed after confirming the existing Wooden Grinder route. See ADR 0026.

# ADR 0025 — Safe packed-mud movement and selective dynamite processing

**Status:** Accepted in v0.31.66-b.

Minecraft 1.21.11 `BlockBehaviour.BlockStateBase` does not implement `getSpeedFactor`. The previous required Mixin injection caused a fatal bootstrap crash. The movement bonus belongs on `Entity.getBlockSpeedFactor`, restricted to players standing on Packed Mud. Future changes to Mixin targets MUST verify actual mapped runtime method ownership and run a launch smoke test.

Weak Dynamite uses a non-TNT server-ticked block entity so blasts may affect only the explicit surface-stone and ore allowlist, not arbitrary blocks. Player placement requires main-hand Dynamite and an offhand firestarter. It persists the remaining 90-tick fuse; server alone executes destruction/drops/damage.

Use `BasinMaterialProcessingSystem` as the source of material outcomes; ordinary Basin processing uses multiplier 1.0 and dynamite uses 1.5. Each entry is rolled independently; chances above 1.0 produce guaranteed items plus a fractional extra roll. Never substitute a vanilla explosion that destroys unrelated blocks or allows deepslate shortcuts.

Carbon Dust's 400-second Forge recipe exceeds one 300-second charcoal batch, so its progress must continue into a second valid charcoal fire with the same input and sufficient output space. Other established five-minute recipes retain their previous requirements. Brick Furnace Fine Fuse Powder is 200 real seconds, independent of its normal speed multiplier.

The Flint Axe is explicitly excluded from `projectflint:plank_cutting_axes` to preserve woodworking economy. Wooden and Hardwood Axe rules remain unchanged. The first Wooden Axe would otherwise be blocked by a dependency loop: it requires Wooden Sheets, but the usual Sheet recipe requires Planks. The limited, lower-yield personal recipe 1 Stripped Log + Bone Knife → 1 Wooden Sheet breaks the loop without enabling Flint Axe Log-to-Planks. The later 1 Plank + Bone Knife → 3 Sheets remains more efficient. Keep the personal K-key guide aligned.
