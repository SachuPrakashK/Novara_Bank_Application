# Windows Storage Recovery Results

Measured on 2026-11-06 IST. Values are current observations, not reused Phase I.3.3 measurements.

## Capacity

| Drive | Total bytes | Free bytes after verification | Status |
|---|---:|---:|---|
| C: | 167,116,795,904 | 6,010,384,384 | Operational, but 8,989,615,616 bytes below the preferred 15 GB margin |
| D: | 342,883,299,328 | 72,687,611,904 | Adequate for project-scoped build output |

C: temporarily measured 4,966,346,752 free bytes during the initial inventory and recovered to the final value as transient activity ended. No deletion was performed in this execution.

## Itemized candidates

| Candidate | Verified path | Bytes | Regenerable/in-use assessment | Risk and approval |
|---|---|---:|---|---|
| Active npm cache | `C:\Users\user\AppData\Local\npm-cache` | 1,565,464,085 | Regenerable; no npm/node process at final snapshot | Low if cleaned with supported npm cache tooling; explicit approval required |
| User temporary files | `C:\Users\user\AppData\Local\Temp` | 1,224,654,295 | Mixed ownership; individual files may be live | Medium/high; inspect exact targets and obtain explicit approval |
| Maven repository | `C:\Users\user\.m2\repository` | 107,360,322 | Dependencies are redownloadable, but this is not a temporary-download set | Do not delete as generic cleanup; zero `.lastUpdated`, `.part`, or `.tmp` files found |
| Angular build output | `D:\Projects\Novara_Bank_Application\dist` | 2,137,920 | Regenerable; production build output | Low, but affects D: only; explicit cleanup approval required |
| Angular tooling cache | `D:\Projects\Novara_Bank_Application\.angular\cache` | 5,867,611 | Regenerable; no Node process at final snapshot | Low, but affects D: only; explicit cleanup approval required |
| Backend build output | `D:\Projects\Novara_Bank_Application\backend\target` | 40,859,733 | Regenerable; backend Java process is running | Do not remove while in use; affects D: only |
| Project tooling temp | `D:\Projects\Novara_Bank_Application\tmp\tooling` | 411,208,172 | Regenerable; created by current test/tooling activity | Low after all users stop; affects D: only and requires approval |
| Backend local logs | `backend/backend-local.*.log` | 639,672 | Diagnostic output; active backend may append | Preserve while backend runs |
| Docker logs | `C:\Users\user\AppData\Local\Docker\log` | 36,750,748 | Operational diagnostics; Docker is active | Do not broadly delete; review/rotation requires approval |

Even removing all identified npm-cache and user-temp bytes would not reach the preferred 15 GB C: margin. Docker reports 4.538 GB of reclaimable build cache, but it belongs to shared Docker storage and reclaiming it may not directly compact the host VHD. No prune or VHD operation is authorized.

The Docker data VHD is `C:\Users\user\AppData\Local\Docker\wsl\disk\docker_data.vhdx` (15,536,750,592 bytes). It was not deleted, truncated, moved, compacted, or modified directly.
