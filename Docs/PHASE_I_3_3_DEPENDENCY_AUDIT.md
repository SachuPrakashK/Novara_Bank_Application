# Phase I.3.3 Dependency Audit

`npm audit --omit=optional` currently reports 24 transitive advisories: 4 high, 19 moderate, and 1 low.

| Chain | Installed | Risk / reachability | Remediation status |
|---|---|---|---|
| Angular build -> Vite | nested Vite 8.3.0 | High; development/build server, not production bundle runtime | Non-force update offered through Angular tooling |
| Angular CLI/forms -> Zod | 4.6.5 | High; CLI/tooling and framework transitive validation | Patch 4.6.6 offered |
| jsdom -> Undici | 8.12.1 | High; unit-test DOM/network tooling | Patch 8.12.3 offered |
| AJV -> Fast-URI | 3.1.8 | Moderate; schema/build tooling | Patch 3.1.9 offered |
| ESLint/TypeScript-ESLint -> Minimatch | 10.2.6 | Moderate; lint tooling | npm proposes force/breaking remediation; not applied |

`npm audit fix --dry-run` proposed semver-compatible tooling updates, but actual non-force application aborted with `ERESOLVE` before changing `package-lock.json`. `--force` and `--legacy-peer-deps` were not used. No advisory is declared resolved. A separately reviewed Angular/toolchain lockfile update is required, followed by the full frontend suite.
