# Docker Environment Diagnostics

Executed: 2026-10-31, Asia/Calcutta. All checks were read-only and time-bounded.

| Check | Result |
|---|---|
| `docker version` | Client 29.8.2; server unavailable |
| `docker info` | Failed; `dockerDesktopLinuxEngine` named pipe absent |
| `docker context ls` | `desktop-linux` selected |
| `docker ps` | Failed; engine pipe absent |
| `wsl --status` | Failed with `E_ACCESSDENIED` while enumerating distributions |
| `wsl --list --verbose` | Failed with `E_ACCESSDENIED` |
| Docker service | `com.docker.service` stopped (Manual) |
| Docker processes | No Desktop/backend/WSL VM processes observed |
| Port 8082 | Available |
| Compose model | Valid with `config --quiet --no-interpolate` |
| Required secrets | 0/4 present; values were never read or printed |
| Disk | C: 2.7 GB free of 155.6 GB; D: 61.2 GB free of 319.3 GB |

Recent Docker Desktop lifecycle logs repeatedly record backend exits, including on October 30. The VM init log also contains a WSL name-resolution failure. These are observations, not proof that DNS or disk pressure alone caused shutdown.

Best-supported explanation: Docker Desktop is not running, its service is stopped, and its selected Linux-engine pipe does not exist. The reason for the last backend exit cannot be conclusively established without an authorized restart and further WSL/Desktop inspection. Low C: free space is a material recovery risk.

## Recovery recheck

After the user reported Docker Desktop running, elevated read-only engine access on 2026-10-31 verified client/server 29.8.2, overlayfs, 13 containers (8 running), 11 images, and 3,918,635,008 bytes assigned memory. Existing unrelated containers were only listed and were not modified. The initial restricted-session retry returned pipe permission denied; the elevated read-only retry succeeded.
