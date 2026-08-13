# ADR 0001 — Target Minecraft 1.21.11 with Mojang mappings

Status: accepted for the initial branch

## Decision

Start Project Flint on Minecraft 1.21.11, Fabric, Java 21, and Mojang official mappings.

## Context

The project needs a modern baseline but is expected to live for years. Fabric identifies 1.21.11 as the final obfuscated Minecraft release before the 26.x unobfuscated line and recommends Mojang mappings around this transition.

Starting on 1.21.11 gives us a mature 1.21.x runtime while deliberately using the naming direction that reduces future porting friction.

## Build snapshot

- Minecraft 1.21.11
- Fabric Loader 0.19.3
- Fabric API 0.141.6+1.21.11
- Loom 1.14.10
- plugin `net.fabricmc.fabric-loom-remap`
- Java 21

## Consequences

- source names differ from many older Yarn-based tutorials;
- examples copied from old Fabric tutorials must be translated/verified rather than blindly pasted;
- a future 26.x port should have less conceptual mapping churn;
- version changes remain explicit ADR-worthy decisions because this mod will touch deep vanilla behavior later.
