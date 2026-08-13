# Environment Effect Rendering Reference — v0.28.7.2-a

## Soaked
Do not add vanilla Slowness or Hunger status instances to represent Soaked. `projectflint:soaked` owns its movement-speed modifier and hunger-style exhaustion.

## Drenched
After 90 seconds of continuous wet exposure, server state replaces Soaked with `projectflint:drenched`.
Drenched inherits the same movement/exhaustion behavior, applies Poison-I-equivalent nonlethal periodic damage internally, and suppresses natural healing through `FoodDataMixin`.
Do not add a separate vanilla Poison effect merely to implement Drenched damage.

## Terrified
Terrified must not apply or emulate Blindness. It retains its Project Flint attack/mining penalties and the existing darkness-danger gameplay only.
The retired `MobEffectFogEnvironmentMixin` and `BlindnessFogEnvironmentMixin` are deleted by the v0.28.7.2-a cleanup BAT.

## True darkness
Project Flint overrides `assets/minecraft/shaders/core/lightmap.fsh` rather than writing into Minecraft's finished GPU lightmap texture.
At night, sky contribution is aggressively suppressed while block light remains available.
`ProjectFlintNightFogEnvironment` additionally caps exposed outdoor full-night atmospheric visibility at approximately 8 blocks, fading in/out around dusk and dawn.
Night Vision bypasses the Project Flint night-fog environment.
