# ADR 0014 — Underground environmental effects and light safety

Status: Accepted (v0.31.55-b).

## Context

The overhaul's environmental hazards currently focus on darkness and soaked conditions. Deep underground play should become uncomfortable before dedicated counter-gear is introduced without direct lethal debuffs.

## Decision

Use two distinct registered Project Flint effects. Speluncaphobia applies below Y=0, stacks +1 amplifier per continuous 60 seconds up to amplifier 3 (four tiers) and increases food exhaustion while reducing natural healing. The extra exhaustion only applies while hunger is above 6, keeping the effect from directly pushing players toward starvation. Claustrophobia applies below Y=-40 and reduces movement speed by 7% and attack damage by 1. Direct daytime sky exposure (without rain at the player) or local block-light level >=13 clears both instantly and resets the Speluncaphobia stacking timer. The effects are server-driven and do not inflict direct damage.

Creative and Spectator players do not receive the effects. Keep vanilla Wet/Terrified behavior independent. Implement future counter-equipment or potions through a specific authorized exemption, not by disabling the global depth rules.

## Test boundaries

Y=0, -1, -40, -41; daytime/nighttime vertical shafts; block light level 12/13/14; removal on returning above thresholds; stacking duration; food and healing; Creative/Spectator. No abrupt rapid apply/remove flicker.
