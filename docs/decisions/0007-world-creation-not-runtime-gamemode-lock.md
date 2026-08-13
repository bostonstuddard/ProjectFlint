# ADR 0007 — Restrict world creation, not runtime game modes

## Status
Accepted in 0.1.0-alpha.5.

## Context
Alpha.4 implemented the requested Hardcore/Creative-only direction by forcing the world to report Hardcore and by rejecting Adventure/Spectator transitions at runtime. This broke ordinary Spectator behavior and was broader than the intended rule.

## Decision
Project Flint constrains the create-world state to Hardcore or Creative and forces Hard difficulty there. It does not globally override `PrimaryLevelData.isHardcore()` and does not reject normal player game-mode transitions after the world exists.

For worlds created without commands, Open to LAN is disabled so it cannot be used as an in-game cheats bypass.

## Consequences
- Hardcore remains a real world-creation choice instead of a runtime disguise over Survival.
- Spectator works normally.
- Creative test worlds remain practical for development.
- World-creation policy and runtime player-state policy remain separate concerns.
