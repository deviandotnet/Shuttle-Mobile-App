# Shuttle Mobile App — Visual System

## Typeface

Use **Inter** throughout the product.

- Regular: 400
- Medium: 500
- Semibold: 600
- Bold: 700
- Fallback stack: `Inter, "SF Pro Text", Roboto, sans-serif`
- Use tabular numerals for times, dates, and passenger counts. Hyphenated pass IDs (`SPT-2026-10-0012`) use proportional figures, because tabular hyphens space them out.

The generated PNG mockups are raster images and do not contain inspectable font metadata. Inter is the exact implementation font specified for recreating the visual system.

## Typography scale

| Token | Size / line height | Weight | Letter spacing | Usage |
|---|---:|---:|---:|---|
| Display | 48 / 52 px | 700 | -1.5% | Welcome headline only |
| Heading 1 | 32 / 40 px | 700 | -1% | Authentication headings |
| Heading 2 | 24 / 32 px | 700 | -0.5% | Screen sections and modal titles |
| App bar | 20 / 28 px | 600 | -0.25% | Navigation titles |
| Metric | 32 / 36 px | 700 | -1% | Dashboard numbers |
| Body large | 16 / 24 px | 400 | 0 | Prominent supporting copy |
| Body | 15 / 22 px | 400 | 0 | Standard UI copy |
| Label | 14 / 20 px | 600 | 0 | Field and control labels |
| Caption | 13 / 18 px | 500 | 0 | Metadata and helper text |
| Button | 16 / 24 px | 600 | 0 | Primary and secondary actions |
| Overline | 12 / 16 px | 600 | +4% | Small section identifiers |

## Color tokens

| Token | Hex | Usage |
|---|---|---|
| Brand / Midnight | `#07113F` | Primary buttons, app bars, main text |
| Brand / Navy | `#0B2A66` | Pressed states and dark illustrations |
| Brand / Blue | `#123A78` | Secondary navy accents |
| Accent / Mint | `#55D6B4` | Positive actions, scanner, active route markers |
| Accent / Mint dark | `#16A37A` | Accessible mint text and icons on light surfaces |
| Accent / Mint soft | `#DFF8F1` | Success and selected backgrounds |
| Canvas / Warm white | `#FAF9F6` | Main page background |
| Surface / White | `#FFFFFF` | Cards, dialogs, and inputs |
| Surface / Mist blue | `#DCEAF2` | Illustration and quiet selected surfaces |
| Surface / Subtle | `#F4F7FA` | Disabled and low-emphasis areas |
| Text / Secondary | `#65769A` | Supporting copy and metadata |
| Border | `#B8C5D9` | Input and outlined-control borders |
| Divider | `#E8EEF5` | List separators |
| Disabled text | `#9CA8BA` | Disabled controls |
| Warning | `#F59E0B` | Pending sync and caution states |
| Warning soft | `#FFF3D6` | Warning backgrounds |
| Error | `#DC2626` | Destructive and error actions |
| Error soft | `#FDE7E7` | Error backgrounds |
| Scrim | `#07113F` at 56% | Modal background dimming |

## Navigation (mockups 04, 05)

- **Bottom bar:** three destinations, Home · Passes · Sync. Each is a real screen, and the bar stays visible on all three. The selected item gets a Mist blue pill (64×32) with a Midnight icon and label. Sync carries an amber count badge for passes waiting to upload.
- **Primary action:** one Midnight Extended FAB, bottom-right, 16dp above the bar. It reads "New pass", or "Current trip" while a draft is open. There is no full-width sticky button.
- **Header:** the Midnight band draws behind the status bar (edge-to-edge, white system icons). The initials avatar is the only way to open the drawer. There is no gear on Home.
- **Drawer:** an account menu, all in Midnight. From top to bottom:
  - Brand line and close button.
  - Profile: avatar, name, employee number, shuttle provider.
  - Sync card: pending count, last-synced time, Mint "Sync now".
  - Settings and Sign out.
  - Version line.
  - Skyline footer.
  
  It never repeats bottom-bar destinations.

## Auth (mockups 01–03a)

Source: `source/auth.html?<state>`. The states are `welcome`, `sign-in`, `sign-in-error`, `create-account` and `create-account-error`.

- **Brand band:** Midnight, edge-to-edge behind the status bar, with faint route lines, the van wordmark (`ic_shuttle_van_logo`, 13 px Overline-style caps at +18%), the flat skyline, a Brand blue road with a dashed centre line, a Mint pin and a dotted Mint route. The white van sits on the road. A warm-white sheet with a 24 px top radius overlaps the band's foot. The band is 440 px tall on Welcome and 272 px on Sign in, so the form and button stay above the keyboard.
- **Welcome (01):** a 48/52 Display headline ("Scan in. Sync up."), one Body large line, and the actions pinned to the bottom: **Sign in** (Midnight, primary) above **Create account** (outlined). Returning drivers are the common case, so Sign in leads. There are no carousel dots.
- **Sign in (02):** a Heading 1 "Welcome back" plus one line. Username and Password fields, the password eye toggle, and a Caption helper: "Forgot it? Your shuttle provider can reset it." There is no reset flow, so there is no link. A divider labelled "New to Shuttle Mobile?" sits above an outlined Create account button at the bottom.
- **Sign-in error (02a):** both fields get a 2 px Error border. An Error soft alert sits above the button: **Username or password is incorrect** · "Check both and try again. Your shuttle provider can reset your password." The helper line is hidden while the alert shows.
- **Create account (03, 03a):** a Midnight app bar with a back arrow, and no stepper. Three groups with 16/24 bold headings: **About you** (first, middle and last name, full-width, no icons; middle name is required), **Work** (Shuttle provider as a picker with a chevron, Employee number with tabular digits) and **Sign-in details** (Username, Password, Repeat password). The **Create account** button and "Already registered? Sign in" are docked at the bottom over the scrolling form. Field errors sit under the field in Error red with an icon, e.g. "Passwords don't match. Type the same password in both fields." There is no length hint until a rule is enforced.
- **Fields:** a 14/20 semibold label above a 52 px input (White, 1 px Border, 12 px radius) with an optional 22 px Text / Secondary leading icon. Focus is a 2 px Midnight border plus a 4 px Midnight 8% ring. Error is a 2 px Error border. Trailing actions such as the eye or the chevron get a 44 px target.

## Scanner (mockups 14–14d)

Source: `source/qr-scanner.html?<state>`. The states are `scanning`, `boarded`, `duplicate`, `invalid` and `denied`.

- **Layout:** Midnight app bar titled "Scan passengers". Below it, a full-bleed camera feed with a Midnight scrim at 62% around a 240 × 240 window, which has a 20 px radius and corner brackets. Under the feed is a docked warm-white sheet, with a 24 px top radius, showing:
  - the trip line (route → destination, departure time);
  - "N on board" at Heading 2;
  - the last three manifest rows, styled like mockup 07;
  - two actions: Torch (outlined, 132 px) and Done (Midnight, returns to the pass).
- **Auto-scan:** there is no shutter button. A valid code is saved the moment it is decoded.
- **No dialogs:** each scan shows a result card under the window, and scanning never pauses. The card replaces the previous one and hides after about 3 s. Pair it with haptics: a short tick for boarded, a double buzz for a rejection.

  | State | Brackets | Card | Copy |
  |---|---|---|---|
  | Boarded | Mint | Mint soft, Mint check | **Boarded** · `EMP-… · 16:26` · `#7`. The new row tops the list with a Mint soft fill |
  | Duplicate | Amber | Warning soft, amber icon | **Already on board** · `EMP-… boarded at 16:19 as #3` |
  | Not recognised | Red (`#F87171` on the dark feed) | Error soft, red icon | **Not a passenger QR** · "Ask for the QR on their shuttle pass." |

  Card text is Midnight. The secondary line is Midnight at 74%, never Text / Secondary, which is too faint on the soft fills.
- **Torch:** a toggle. When on, it fills Midnight with a Mint icon. It is disabled when the camera is unavailable.
- **Camera access off:** the feed area becomes a Navy panel showing:
  - a camera-off icon;
  - the title "Camera access is off";
  - the line "Scanning needs the camera. Passengers already on board stay on this pass.";
  - a Mint "Allow camera" button;
  - an "Open app settings" link, used once the system will no longer prompt.

  The sheet and Done stay usable.
- The hint above the window reads "Hold the passenger's QR in the frame / It scans on its own". There is no laser line, no status pill and no privacy footnote.

## Dark mode

Implemented in `ui/theme/ShuttleColors.kt` as semantic roles (`ShuttleTheme.colors.<role>`). The user picks System, Light or Dark on the Settings screen (drawer → Settings); System follows the device setting.

| Role | Light | Dark | Usage |
|---|---|---|---|
| canvas | `#FAF9F6` | `#0A0F24` | Page background |
| surface | `#FFFFFF` | `#131B36` | Cards, dialogs, inputs, nav bars |
| surfaceSubtle | `#F4F7FA` | `#1A2442` | Disabled and low-emphasis areas |
| surfaceQuiet | `#DCEAF2` | `#22305A` | Selected surfaces, avatar, illustration |
| textPrimary | `#07113F` | `#E8EEF8` | Main text |
| textSecondary | `#65769A` | `#9AA8C7` | Supporting copy and metadata |
| textDisabled | `#9CA8BA` | `#5D6A85` | Disabled controls |
| border / divider | `#B8C5D9` / `#E8EEF5` | `#34436A` / `#222E4F` | Outlines and separators |
| primary / onPrimary | `#07113F` / `#FFFFFF` | `#A9C2F5` / `#07113F` | Primary CTA, links, icons, progress |
| brandBand / onBrandBand | `#07113F` / `#FFFFFF` | `#0E1B4D` / `#E8EEF8` | App bars, Home header, drawer header, snackbar, status bar |
| brandAccent | `#123A78` | `#8FB0EE` | Secondary navy accents |
| success / successText / successSoft | `#55D6B4` / `#16A37A` / `#DFF8F1` | `#55D6B4` / `#55D6B4` / `#12392F` | Scan, synced, positive CTA (label `onSuccess` = Midnight) |
| warning / warningText / warningSoft | `#F59E0B` / `#B45309` / `#FFF3D6` | `#FBBF24` / `#FCD34D` / `#3A2A0A` | Pending sync, caution |
| error / errorSoft | `#DC2626` / `#FDE7E7` | `#F87171` / `#3B1418` | Errors, late, destructive |
| scrim | Midnight 56% | Black 64% | Modal dimming |

Rules: brand bands stay dark navy in both themes, so their status bar icons are always light. In dark mode the primary CTA flips to a light blue with a Midnight label.

## Usage rules

- Primary CTA: Midnight background, white label, 12 px radius, minimum 52 px height.
- Positive completion CTA: Mint background with Midnight or white text after contrast testing.
- Inputs: White surface, 1 px Border color, 12 px radius, 52–56 px height.
- Cards: White surface, 16 px radius, restrained shadow; avoid stacking cards inside cards.
- Layout uses an 8 px spacing grid with 24 px page margins and 16–24 px section gaps.
- Minimum interactive target is 48 × 48 px.
- Navy text on warm white is the default high-contrast pairing.
- Mint should signal scanning, success, or a positive primary action—not decoration.
- Amber is reserved for pending/caution; red is reserved for errors and destructive actions.
