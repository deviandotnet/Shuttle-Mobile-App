# Shuttle Mobile App — Visual System

## Typeface

Use **Inter** throughout the product.

- Regular: 400
- Medium: 500
- Semibold: 600
- Bold: 700
- Fallback stack: `Inter, "SF Pro Text", Roboto, sans-serif`
- Use tabular numerals for times, dates, IDs, and passenger counts.

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
