# ADR 0008 — Hardcore semantics without runtime gamemode locking

## Status
Accepted in 0.1.0-alpha.7. This narrows/supersedes the Hardcore portion of ADR 0007 while preserving ADR 0007's prohibition on runtime gamemode restrictions.

## Context
Project Flint needs two world-creation modes: Hardcore for normal play and Creative for development/testing. A Creative-created development world may later be switched to Survival for testing. Vanilla normally leaves that world as non-Hardcore Survival because Hardcore is a world property chosen at creation time.

An earlier implementation tried to solve this by restricting runtime gamemode changes. That broke normal Spectator behavior and was removed in alpha.5.

## Decision
Project Flint re-enables the focused `PrimaryLevelDataMixin`, making `PrimaryLevelData.isHardcore()` report `true` for loaded Project Flint worlds.

Do not intercept or reject normal runtime switches between Creative, Survival, and Spectator. The world-creation UI remains responsible for presenting Hardcore and Creative as the intended starting choices.

## Consequences
- Creative remains practical for development.
- Switching a Creative-created Project Flint world to Survival uses Hardcore semantics instead of ordinary Survival semantics.
- Spectator remains a normal vanilla gamemode and is not blocked.
- The saved world's original game mode can remain Creative; Project Flint supplies Hardcore semantics at runtime.
- If future systems need to distinguish a truly player-created Hardcore world from a Creative development world, they must inspect separate world metadata rather than assuming `PrimaryLevelData.isHardcore()` answers that question.
