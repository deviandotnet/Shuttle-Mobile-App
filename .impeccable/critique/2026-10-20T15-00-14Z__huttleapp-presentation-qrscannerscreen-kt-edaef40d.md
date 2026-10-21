---
target: QR scanner screen (implementation + mockup 14)
total_score: 14
max_score: 40
na_heuristics: 
p0_count: 1
p1_count: 2
target_identity: "file:C:\\Users\\Administrator\\StudioProjects\\Shuttle-Mobile-App\\app\\src\\main\\java\\com\\example\\shuttleapp\\presentation\\QRScannerScreen.kt"
target_fingerprint: "sha256:086254d683bc4bde52c68a5f2039016d1b7def09d2028a68335f4ea99cfbb11e"
target_path: "C:\\Users\\Administrator\\StudioProjects\\Shuttle-Mobile-App\\app\\src\\main\\java\\com\\example\\shuttleapp\\presentation\\QRScannerScreen.kt"
timestamp: 2026-10-20T15-00-14Z
slug: huttleapp-presentation-qrscannerscreen-kt-edaef40d
---
Method: design review only (native platform, so the detector does not apply). The proposed mockup 14 was reviewed alongside it.

## Design Health Score (current implementation)
| # | Heuristic | Score | Key Issue |
|---|---|---|---|
| 1 | Visibility of System Status | 1 | No count, no trip context, no "scanning" signal; result only via modal |
| 2 | Match System / Real World | 2 | "Scan QR Code" and "Success"/"Error" titles are generic, not boarding language |
| 3 | User Control and Freedom | 2 | Back works, but there is no Done and no torch |
| 4 | Consistency and Standards | 2 | Translucent band, 18sp hint, square box are off the design system |
| 5 | Error Prevention | 2 | Duplicates and non-passenger codes are rejected by ValidateQRCodeInput |
| 6 | Recognition Rather Than Recall | 1 | Driver must leave to see who has boarded |
| 7 | Flexibility and Efficiency | 1 | Every scan needs a dialog dismiss; 30 boarders = 30 taps |
| 8 | Aesthetic and Minimalist Design | 2 | Sparse but unbalanced: magic offsets (100dp/130dp/315dp) |
| 9 | Error Recovery | 1 | Denied camera permission is a blank dead end; `qrCodeErrorMessage!!` can crash |
| 10 | Help and Documentation | 0 | No guidance beyond one hint line |
| **Total** | | **14/40** | Poor |

Proposed mockup 14 scored about 23/40: it has the right ingredients (trip context, Mint brackets, auto-scan), but it uses an iOS frame, carries stale and wrong data (Jan 02, 2025; "SEMPHIL" as the destination), puts the camera in a card while chrome takes about 55% of the screen, and relies on a laser-line cliché. Its "Ready to scan" pill never changes, the privacy footnote is unbacked, Flash and Enter manually have mismatched control sizes and no feature behind them, and it shows no result or permission states.

## Priority Issues
- [P0] Camera permission denied renders an empty navy screen with no recovery, and the permission is requested twice on launch (QRScannerScreen.kt:86-102). Fix: a camera-access-off state with Allow camera and Open app settings (mockup 14d).
- [P1] A modal dialog after every scan blocks the boarding flow and drops frames. Fix: an inline result card under the window, auto-hiding, with haptics (14a-14c).
- [P1] No trip context or progress. Fix: a docked sheet with the route, departure, "N on board" and the last three rows.
- [P2] Generic copy: "Error" / "Invalid QR Code. Unknown." Fix: "Not a passenger QR — Ask for the QR on their shuttle pass." and "Already on board — boarded at 16:19 as #3".
- [P2] No torch for dim cabins or dusk pickups. Fix: a Torch toggle beside Done.

## Persona Red Flags
- Driver at a dusk pickup with a queue: taps OK after every passenger, has no torch, and cannot see the count.
- First-time user who taps "Don't allow": stuck on a blank screen with only Back.

## Minor Observations
- The analyzer builds a new MultiFormatReader and launches a coroutine per frame; the analyzer runs on the main executor.
- `_currentQrResult` is never reset, so re-showing the same rejected code gives no feedback.

## Questions to Consider
- Should Done be disabled until at least one passenger boards, or always return to the pass?
- Should a rejected scan vibrate differently from a boarded one?
