# Backpack rendering root cause — v0.26.21-a

The Leather Backpack was invisible because its `Equippable` component had no equipment asset id.

In Minecraft 1.21.11, `HumanoidMobRenderer.getEquipmentIfRenderable` only copies an equipped stack into `HumanoidRenderState.chestEquipment` when `HumanoidArmorLayer.shouldRender` returns true. That check requires the `Equippable.assetId()` to be present and the equipment slot to match.

`ReedBackpackLayer` reads `state.chestEquipment`, so:

- Reed Backpack worked because it had `REED_BACKPACK_EQUIPMENT_ASSET`.
- Leather Backpack was invisible because it used `Equippable.builder(EquipmentSlot.CHEST).build()` with no asset.

Fix:

- register `LEATHER_BACKPACK_EQUIPMENT_ASSET`
- set it on the Leather Backpack's `Equippable` component
- keep `assets/projectflint/equipment/leather_backpack.json` with an empty `layers` object so vanilla does not draw an armor layer
- keep the actual 3D worn backpack rendering in `ReedBackpackLayer`

Do not remove the Leather Backpack equipment asset while `ReedBackpackLayer` relies on `HumanoidRenderState.chestEquipment`.
