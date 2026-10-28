# Project Flint locked visual references

These files are the confirmed-good visual baselines and must be used before changing Project Flint textures/models.

- `PROJECT_FLINT_TEXTURE_REFERENCE_CONFIRMED_GOOD_v0.26.10-a.png` — confirmed-good texture layout.
- `PROJECT_FLINT_TEXTURE_REFERENCE_CONFIRMED_GOOD_v0.26.10-a_LABELLED.png` — same layout with visual labels.
- `PROJECT_FLINT_TEXTURE_REFERENCE_CONFIRMED_GOOD_v0.26.10-a_INDEX.txt` — authoritative tile-to-resource mapping for that sheet.
- `CLAY_GRINDER_CONFIRMED_GOOD_v0.26.14-a.json` — confirmed-good Clay Grinder model/UV mapping.

## Rules for future texture updates
1. Never remap a user-edited sheet against a different index.
2. Compare against this reference and change only tiles the user actually edited.
3. Never mass-replace or deduplicate texture files unless explicitly requested and independently verified.
4. Preserve the confirmed-good Clay Grinder model unless the user explicitly requests a model change.
