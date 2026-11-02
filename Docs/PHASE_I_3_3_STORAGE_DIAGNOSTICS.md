# Phase I.3.3 Storage Diagnostics

Observed on 2026-11-02 IST.

| Target | Observed size/state |
|---|---|
| C: before cleanup | 167,116,795,904 bytes total; **0 bytes free** |
| C: after approved npm-cache cleanup | 8,395,370,496 bytes free |
| D: | 342,883,299,328 bytes total; 69,402,431,488 bytes free |
| Repository on D: | 1,401,012,688 bytes; 28,083 files |
| `node_modules` on D: | 285,819,614 bytes |
| Project `dist` | 2,137,920 bytes |
| Backend target | 39,379,784 bytes |
| Angular cache | 5,867,840 bytes |
| Project temp | 18,228,157 bytes at measurement |
| User npm cache on C: before cleanup | 8,187,698,258 bytes; 87,562 files |
| Maven repository on C: | 107,360,322 bytes |
| Windows user temp on C: | 1,326,058,467 bytes; 19,734 files |
| Docker data VHD on C: | 15,104,737,280 bytes |
| Docker logs | 35,209,363 bytes |
| Page-file usage | approximately 13.56% |

RAM totals were unavailable because CIM/type access was restricted. No broad filesystem scan was performed. With explicit approval, only the contents of the user npm cache were removed; that cache is reproducible by downloading packages again.

`Use-ProjectLocalBuildEnvironment.ps1` provides repeatable D:-scoped npm/temp/Java paths. Frontend builds passed with this adjustment. The approved cleanup recovered about 8.40 GB on C:. Windows temp and Docker storage were not deleted; Docker VHD/images/volumes must not be treated as generic cleanup targets.
