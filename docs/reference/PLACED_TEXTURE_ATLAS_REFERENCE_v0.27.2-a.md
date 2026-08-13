# Project Flint placed/crafting texture reference — v0.27.2-a

Placed crafting/tool/campfire block models use block-atlas mirror textures under `textures/block/placed/`.

These mirrors are **not independent artwork**. `build.bat` copies them directly from the canonical item sprites before every build so the in-world placed version always has identical pixels to the item version.

This avoids the missing-texture behavior caused by pointing these block models directly at item-space sprites while also preventing manual duplicate texture maintenance.

Special cases:
- Naturally spawning `loose_rock` intentionally uses the restored known-good `block/loose_rock_placeholder.png` texture.
- Shift-placed vanilla Flint uses `block/placed/flint.png`, restored from the known-good Project Flint Flint sprite.
