# Project Flint Versioning

Project Flint uses exactly three numeric components followed by one channel suffix:

`MAJOR.RELEASE.DEV-CHANNEL`

Examples: `0.28.17-a`, `0.29.0-a`, `1.0.0-r`.

## Numeric components

- **MAJOR** — a major project milestone. Incrementing this resets `RELEASE` and `DEV` to `0`.
- **RELEASE** — the release line / candidate generation within the major version. Incrementing this resets `DEV` to `0`.
- **DEV** — development and bug-fix iterations on the same release line, typically moving toward the next playable release candidate.

## Channel suffix

- `-a` — alpha
- `-b` — beta
- `-r` — release

## Hard rule

Never introduce a fourth numeric component. Versions such as `0.28.7.8-a` or `0.28.17.1-a` are invalid. A follow-up on `0.28.17-a` must advance the `DEV` component (for example `0.28.18-a`) unless the release/channel itself changes.

Historical files may retain old version strings as records, but every newly produced build, patch, release note, validator expectation, and cleanup BAT must use this scheme.

Current release example: `0.28.37-a`.
