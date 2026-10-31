# Workforce Keycloak Environment Verification

This current status supersedes the earlier configuration-only finding while historical reports remain unchanged.

| Property | Current result |
|---|---|
| Docker/WSL during execution | `desktop-linux`; client/server 29.8.2; WSL2 active |
| Memory | 3,918,635,008 bytes assigned to Docker |
| Disk | C: 2,493,521,920 bytes free; D: 67,644,645,376 bytes free at check time |
| Provider | Keycloak 26.0.8 runtime confirmed |
| Realm/endpoints | Realm imported; live discovery/JWKS verified |
| Browser client | Static configuration verified; human flow blocked |
| Verifier | Genuine non-human cryptographic tests 5/5 passed |
| Secrets | Disposable process-only values cleared; no formal vault approval |
| Cleanup | Container/network/tmpfs removed; port 8082 closed |
| Other workloads | Preserved; 8 of 13 containers running at the post-acceptance check |
| Latest delivery check | Port 8082 closed; Docker Desktop daemon subsequently unavailable, so container enumeration could not be repeated |

C: free capacity, constrained Docker memory, and the daemon's later unavailability remain risks. Human identity, MFA, and session acceptance remain blocked.
