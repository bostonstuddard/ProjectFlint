# ADR 0018 — Four-input Basin, readable Field Guide and glass recovery

Status: Accepted for v0.31.59-b. Supersedes the single-stack Basin description from ADR 0015; does not retroactively rewrite that historical decision.

## Context

The Basin previously carried a stack of 64 but displayed one item. Jade could not observe a standard inventory; the guide's forced page breaks wasted vertical room and mixed historical implementation notes with gameplay. The Tanned Utility Vest correctly lacked stone-mining privileges, but a finished premium glass block could not be recovered. Mod Menu metadata and entrypoint lacked a useful placeholder.

## Decision

- Store four independent, individually visible Basin item slots. Process FIFO, return the most recent input first and migrate previous saved single-stack inventories without discarding overflow. Expose `Container` for generic Jade-style integration but do not make Jade a hard dependency.
- Keep two/three stomp economy and one food point per stomp; send feedback from the server and sync every inventory change.
- Grant vest-wearing survival players a narrow exception to mine and recover Pure Mica Insulated Glass, regardless of Silk Touch. Preserve all existing ore/stone restrictions.
- Author field-guide content as ordered short survival chapters and paginate by dynamically wrapped line counts. Keep release notes and developer explanations strictly outside the in-game guide.
- Add a client-only, optional Mod Menu config entrypoint with guide shortcuts. Preserve a verified project URL if Discord's actual invitation URL cannot be verified; do not guess or link unrelated Discord servers.

## Consequences / verification

Old stacks larger than four are migrated into a hidden overflow queue, feeding slots as material is consumed; no newly inserted input can bypass the four-slot limit while overflow exists. Jade presentation, optional Mod Menu entrypoint, GUI sizing, and survival glass recovery need live runtime tests. A verified Discord invite can replace the temporary official Modrinth project homepage in a later metadata-only patch.
