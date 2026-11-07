## v0.31.65-b patch validation
- Verify release version and source-only root-level patch application.
- Regression-test 4 Logs -> 4 Charcoal, then a second batch starts automatically; a full byproduct slot or no Firestarter stops the cycle. An empty charcoal-fueled Forge must stay unlit.
- Test personal vs Hardwood Table leather yields, custom block loot/recovery, all mud recipes, bucket returns, Packed Mud speed, and K/G guide entries.
- Distinguish source/static validation from actual Gradle build and in-game tests. Do not claim a survival run without executing one.
## v0.31.62-b — Mandatory early-game break-action test
Mining rules are incomplete until the Crude Stone Hammer can **complete** its Stone conversion (not merely display cracks). Verify progress/attack/pre-break/server handlers together: natural Stone + Crude Hammer and Crude/Flint/Advanced Multitools yield Crushed Stone + 4 Rocks, and 1 damage to the held tool; ordinary unqualified tools and the Vest cannot harvest Stone. Check the Hardwood Hammer uses its own normal mining route, and that Cobblestone, Coal, Iron/Copper, Deepslate and other ores retain their tier rules. Full runtime tests must be performed before declaring the release playtested.


## v0.31.61-b — Preflight compatibility and recipe removal
Before shipping, verify glass class names against the actual **Mojang-mapped Minecraft 1.21.11 JAR** and run `:compileJava` in the user's Gradle environment. Verify that v0.31.60-b personal PMI Sheet recipe JSON is deleted by `APPLY_PROJECTFLINT_0_31_61_B.bat`; a patch-only ZIP overwrite cannot remove old files by itself. Check the live 2×2 personal grid rejects PMI Sheets, the Hardwood Table accepts them with 60-tick delay and tool wear, and glass lighting/vesting stays unchanged. Successful builds only copy runtime JAR to `dist/`. Older v0.31.60-b dual crafting instructions are superseded.

## v0.31.60-b — Finished JAR delivery and light behavior

On successful Windows build, `build.bat` publishes one finished runtime JAR under `dist/` using the exact `archives_base_name` and `mod_version` from Gradle properties. Check that no `*-sources.jar` / `*-dev.jar` / obsolete Project Flint JAR is left in `dist`, and that `--no-daemon` prevents long-lived build JVMs. Do not kill unrelated Java processes to implement this. Failing builds must report failure and not publish an artifact. 

Mica glass is now **vanilla Glass-style light-passing**, not Tinted Glass-style light-blocking. The Vest still governs intact pickup, while Bone Knife durability-based personal/Hardwood recipes create the nonplaceable PMI Sheet item.

## v0.31.59-b — Additional regression gates

- Confirm four Basin slots are truly one item apiece, distinct types display and serialize, oldest input crushes first and newest returns first. Ensure prior single-stack saves are not silently discarded.
- Test Jade or WAILA inventory HUD integration in-game; vanilla `Container` exposure alone is a source-level contract, not proof of a particular overlay's UI behavior.
- Test crushing particles/sounds at 1, 2, and 3 stomps and the 1-food-point cost each time; verify no duplicate outputs or free stomps.
- Test vest-gated Mica Glass breaking without Silk Touch, regular mining gates, creative behavior, and light blocking.
- Check Field Guide for exclusively player-facing text, stage order, no clipped sidebar, correct page counts at multiple GUI scales, no double blur, and no regressions in the K-key recipe guide.
- Test Mod Menu optional integration both present/absent, URL accuracy, and config-page navigation. Never invent a Discord invite.

## v0.31.58-b — Additional regression gates

- Verify both normal result clicking and shift-clicking with Bone Knife / milk buckets; the cup result should receive the correct personal/table yield with tool wear and empty container return.
- Verify multiple material bonuses may occur together; include seeded deterministic tests for every independent Basin chance.
- Verify Forge remains off after a burn regardless of remaining fuel; manually restarting must require an input and a suitable firestarter for Charcoal.
- Verify all Fat Torch wall orientations still match the Flint Torch and test ignite at Y = 0 versus Y = -1.
- Superseded by v0.31.60-b: verify Pure Mica Insulated Glass is placeable, returns intact with the Vest, and **passes** light like vanilla Glass.
- Keep G/K guide data and technical release notes updated with player-facing facts only in the guides.

## v0.31.57-b — Release and regression obligations

Use patch-only root-relative ZIP, incremented release versions and cleanup/apply BAT, updated CHANGLELOG/README/release notes/manifest, ADRs for mechanics, current systems/yields, and both G and K guide data. Check Java compile against Minecraft 1.21.11, screen regression, recipe probabilities, container returns, asset provenance and shift-click timer. No reapplication of obsolete playtests or replacement of approved textures with generated art.

# Release Documentation Process

## v0.31.55-b additional release checks

- For probabilistic outputs document independent/mutually-exclusive rolls, total yield bounds, guaranteed versus optional results, and where hunger/tool/water cost applies.
- For new effects check every exact height and light boundary and server-authoritative removal; status icon and localization must be present.
- For decor salvage explicitly list safe blocks and preserved contents; any new whitelist entry requires ADR review.
- For a new placeable craft include model, item definition, loot table, blockstates for all orientations, item group, G/K guide references, and regression checks.


Version identifiers must follow [`VERSIONING.md`](VERSIONING.md). New releases may never add a fourth numeric component.

Every release, including internal alphas, must add `docs/releases/<version>.md`.

A release document must contain:

1. **Intent** — the problem this release was supposed to solve.
2. **Player-facing behavior** — what can actually be experienced.
3. **Architecture** — new systems/interfaces/data boundaries.
4. **Design reasoning** — why this implementation was chosen.
5. **Tuning snapshot** — meaningful constants in that release.
6. **Assets/licenses** — anything imported, removed, or relicensed.
7. **Advancement coverage** — which new/changed progression milestones are represented in the Project Flint advancement tree, or why no advancement change is appropriate.
8. **Known limitations** — especially temporary architecture.
9. **Validation** — what was compiled/launched/tested.
10. **Next release pressure** — what this release intentionally leaves unresolved.

Also update:

- `CHANGELOG.md`
- `docs/ROADMAP.md`
- `docs/ASSET_SOURCES.md` when assets change
- an ADR in `docs/decisions/` for choices that would be expensive to reverse

Release documents are historical records. Do not rewrite old reasoning to make later decisions look inevitable; append corrections or superseding ADRs instead.

Advancement coverage is a release gate for progression work. A mechanic that introduces a meaningful new capability is not considered fully integrated until the advancement tree has been reviewed alongside it.


## Ground interaction reversibility

Any release that introduces a physical ground assembly or primitive stockpile must explicitly test:

- recovery of incomplete components;
- behavior when the five-slot inventory is full;
- correct count/state reduction;
- no silent deletion of materials;
- no accidental conversion into vanilla block drops.


## Current-state and guide parity (ADR 0011)

Every release must review `docs/CURRENT_SYSTEMS.md`, `docs/ARCHITECTURE.md`, `docs/ROADMAP.md`, `docs/PRIMITIVE_PROGRESSION_MATRIX.md`, G-key Field Guide chapters and K-key Recipe Guide records. Only modify an in-game guide when the release changes relevant player behavior. Disagreements with previous accepted mechanics require a superseding ADR, not silent historical edits. For mining changes, source and server permission paths must share a predicate, and client/server test coverage must include natural and placed stone, the Utility Vest, and unrelated tools.

## v0.31.56-b additions — Workstation item state and custom recipe type

- For physical workstations check block-entity save/load, network update packet/tag, rendered item correctness for all accepted input types, and no stale static model geometry.
- For timed crafts using custom recipe types check vanilla result slots' **hard-coded recipe lookups**. Use matched custom recipe for all remainders; test durable tools, emptied containers, filled grids, full inventory, shift-click, and cooldown reset.
- For repeated actions require documented thresholds, cooldown, persistence, per-action cost timing, refund/removal behavior, server ownership, and a guide update.

## v0.31.63-b — Break-event interoperability gate

When customizing `ServerPlayerGameMode.destroyBlock` to return `false` after successful world mutation, regression-test Fabric `PlayerBlockBreakEvents.AFTER` consumers (especially Flint Quests `BREAK_BLOCK` tasks). Check original block identifier, exactly-one dispatch for custom breaks, no manual event for ordinary vanilla breaks, no quest credit for no-op/canceled forbidden mining, and no loot/tool/wear changes. Test Stone-to-Crushed-Stone with a near-broken Hammer and normal coal/stone mining as distinct paths.

## v0.31.64-b — Survival progression and recipe-ownership gate
Before packaging, compare K-key personal guide entries to actual recipe IDs and RecipeManager filters, and compare Hardwood Table recipes for duplicate ingredient/tool signatures. Test recipes in both the 2×2 and 3×3 inputs; guide entries alone do not prove that a recipe loads. A change to `RecipeManagerMixin` must not silently disable early tools. Verify vanilla plant variants (including newly added plants) against the Vest and both client/server break handlers. Auto-ignition must never spend fuel when output is blocked or input missing. Preserve existing cleanup lineage, build version, distribution behavior and root-relative patch ZIP.

