# Project Flint v0.28.38-a — Water Bearing Pot texture patch

## Added
- Implemented the supplied Water Bearing Pot item texture.
- Wired the texture into both `water_bearing_pot` and `water_bearing_pot_filled` item models so neither item renders as the pending/null placeholder.

## Notes
- The supplied PNG was copied byte-for-byte with no pixel edits.
- Because only one sprite was supplied, the filled item currently uses the same texture asset as the base Water Bearing Pot.
