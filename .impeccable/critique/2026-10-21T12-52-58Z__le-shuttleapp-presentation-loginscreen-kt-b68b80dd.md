---
target: auth screens (Login, Register) vs mockups 01-03
total_score: 20
max_score: 40
na_heuristics: 
p0_count: 0
p1_count: 3
target_identity: "file:C:\\Users\\Administrator\\StudioProjects\\Shuttle-Mobile-App\\app\\src\\main\\java\\com\\example\\shuttleapp\\presentation\\LoginScreen.kt"
target_fingerprint: "sha256:167991778abcee15f50c7fa845957c0ece0f4aaa7bea0fda547644d16aae3366"
target_path: "C:\\Users\\Administrator\\StudioProjects\\Shuttle-Mobile-App\\app\\src\\main\\java\\com\\example\\shuttleapp\\presentation\\LoginScreen.kt"
timestamp: 2026-10-21T12-52-58Z
slug: le-shuttleapp-presentation-loginscreen-kt-b68b80dd
---
Method: design review only (detector not applicable, native platform)

Target: auth flow. Current LoginScreen.kt + RegisterScreen.kt (no Welcome screen exists), compared with proposed mockups 01–03.

## Design Health Score (current Login + Register)
| # | Heuristic | Score | Key Issue |
|---|---|---|---|
| 1 | Visibility of System Status | 2 | Login per-field errors commented out (LoginScreen.kt:216, :237); only an 11sp combined error |
| 2 | Match System / Real World | 2 | "Login your account", "Already had an account?", meaningless Abc icons on name fields |
| 3 | User Control and Freedom | 2 | Register → Login uses popUpTo(0); Back from Register exits the app |
| 4 | Consistency and Standards | 2 | Pill fields, 45dp button, curved gradient header ≠ v2 Home/Scanner |
| 5 | Error Prevention | 2 | Password uses ImeAction.Done before Repeat password; no password rule |
| 6 | Recognition Rather Than Recall | 3 | Labels above fields; provider is a picker |
| 7 | Flexibility and Efficiency | 2 | No autofill hints; new username not carried back to Login |
| 8 | Aesthetic and Minimalist Design | 2 | 26sp caps wordmark, floating card, empty bottom 40% |
| 9 | Error Recovery | 2 | Register per-field errors OK; Login doesn't say which field |
| 10 | Help and Documentation | 1 | "Forgot password?" is a dead TODO link |
| **Total** | | **20/40** | Acceptable (low end) |

## Priority Issues
- [P1] Mockup 03 contradicted the real form: no employee number, free-text provider, middle name "optional" though required, unenforced 8-char hint. /impeccable clarify
- [P1] 01–03 off-system: iPhone frame and iOS status bar, glossy 3D van vs the v2 Android frame and flat van/skyline. /impeccable polish
- [P1] Welcome made sign-up primary, had carousel dots and passenger-facing copy, and added a tap before every sign-in. /impeccable distill
- [P2] "Forgot password?" promised a reset flow that doesn't exist. /impeccable clarify
- [P2] 02's illustration took ~30% of the screen; the button falls under the keyboard. /impeccable adapt

## Persona Red Flags
- Casey: mid-screen login button; tiny Register link; keyboard Done hides the confirm field; 2-column names truncate at 360dp.
- Jordan: mockup 03 never asked for employee number; "Step 1 of 2" with no step 2; Abc icons.
- Sam: logo contentDescription "Test"; login error doesn't name the field; off-token light-blue focus ring.

## Minor Observations
- Repeated person icons on name rows; "Create account" stated twice in 03; Login could prefill the new username.

## Questions to Consider
- Does a driver tool need Welcome, or is Sign in the welcome?
- Should drivers self-register, or does the provider create accounts?

## Decisions (user)
- Keep Welcome with Sign in primary; match the current form; no reset link ("ask your provider"); render via HTML source.
