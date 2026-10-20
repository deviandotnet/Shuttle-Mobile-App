---
target: navigation drawer, top and bottom bar (Home)
total_score: 22
max_score: 40
na_heuristics: 
p0_count: 0
p1_count: 3
target_identity: "file:C:\\Users\\Administrator\\StudioProjects\\Shuttle-Mobile-App\\app\\src\\main\\java\\com\\example\\shuttleapp\\presentation\\HomeScreen.kt"
target_fingerprint: "sha256:3441ab47f1e0ec088c1937c75fa147c4ef76dd5dd227dad8827593623706d8a9"
target_path: "C:\\Users\\Administrator\\StudioProjects\\Shuttle-Mobile-App\\app\\src\\main\\java\\com\\example\\shuttleapp\\presentation\\HomeScreen.kt"
timestamp: 2026-10-20T13-41-17Z
slug: ple-shuttleapp-presentation-homescreen-kt-9fc748e3
---
Method: design review only (detector not applicable, native platform)

## Design Health Score
| # | Heuristic | Score | Key Issue |
|---|---|---|---|
| 1 | Visibility of System Status | 2 | Status-bar icons unreadable (white on warm-white strip); bottom bar never reflects Passes/Profile |
| 2 | Match System / Real World | 3 | "trip" / "active pass" / "shuttle pass" / "Completed Shuttle Pass List" drift |
| 3 | User Control and Freedom | 3 | Drawer closes three ways; Back works; sign-out guarded |
| 4 | Consistency and Standards | 1 | Avatar, gear and Profile tab all open the drawer; gear isn't Settings; tabs aren't tabs |
| 5 | Error Prevention | 3 | Sign-out dialog warns about unsynced passes |
| 6 | Recognition Rather Than Recall | 2 | Settings buried behind Profile > drawer > Settings |
| 7 | Flexibility and Efficiency | 2 | Stats not tappable; no path from "pending sync" to the passes |
| 8 | Aesthetic and Minimalist Design | 3 | Clean and on-brand; heavy bottom chrome; empty drawer |
| 9 | Error Recovery | 2 | Load error has no Retry |
| 10 | Help and Documentation | 1 | No help anywhere |
| **Total** | | **22/40** | Acceptable |

## Priority Issues
- [P1] Status bar unreadable on all screens: draw the Midnight band behind the status bar (or use dark icons on light tops). /impeccable adapt
- [P1] Navigation model contradicts itself: three entry points to the drawer, the gear announced as "Settings" opens the drawer (HomeHeader.kt:73), Passes pushes a separate screen without the bar, Profile opens an overlay. Fix: bottom bar Home · Passes · Sync (badge); avatar opens the account drawer; remove gear. /impeccable shape
- [P1] Bottom chrome takes ~22% of the screen (sticky CTA bar plus bottom bar). Fix: one Extended FAB. /impeccable layout
- [P2] Drawer is a full-height overlay for two live actions, ~55% empty. Fix: account + sync-health drawer. /impeccable distill
- [P2] Greeting wraps to two lines; subtitle is filler. Fix: one-line greeting + date/pass count. /impeccable clarify

## Persona Red Flags
- Casey: Settings target is top-right and opens a menu; tapping "pending sync" does nothing; three zeros when there's no trip.
- Sam: the gear is announced as "Settings" but opens a menu; the bar's selected state doesn't change.
- Alex: no one-tap path to Not-synced passes; Back is always needed from the pass list.

## Minor Observations
- The cloud shows through "today's" in the route card; "View all" is underlined; "button below" copy breaks if the button moves; a disabled "Soon" item sits in the main menu; the list title doesn't match the tab.

## Questions to Consider
- Should pending sync be the loudest thing on Home when it's above zero?
- Is the drawer really an account page?
- Could the route card be the main action?
