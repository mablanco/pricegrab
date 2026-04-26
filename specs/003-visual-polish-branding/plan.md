# Implementation Plan: Visual Polish & Branding for the Compare Screen

**Branch**: `003-visual-polish-branding` | **Date**: 2026-04-26 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-visual-polish-branding/spec.md`

## Summary

Replace the current Material 3 placeholder theme of the Compare screen
with a deliberate, brand-coherent visual treatment that (a) reuses the
launcher icon's `#2F5C73` steel-blue as the Material 3 `ColorScheme`
seed, (b) adds a small decorative brandmark glyph in front of the
plain-text "PriceGrab" title in the top app bar, (c) makes the
comparison result the visual focal point of the screen via an explicit
hero card with text + icon-shape cue, and (d) opts out of Material You
dynamic color so brand identity stays consistent across users.

Technical approach: replace the existing palette in
`ui/theme/Color.kt`, simplify `ui/theme/Theme.kt` to drop dynamic
color, add an explicit Material 3 `Typography` scale in `ui/theme/Type.kt`,
introduce a tiny `ui/theme/Spacing.kt` `CompositionLocal` for a
deliberate spacing rhythm, ship a new vector drawable
`res/drawable/ic_brandmark.xml` (≈ 1 KB, scales infinitely), and
restructure `CompareScreen.kt`'s result region into a hero card. No
new Gradle dependency, no new permission, no asset that bloats the
APK beyond a few hundred bytes. The Reset / Undo flow from feature 002
is preserved unchanged.

## Technical Context

**Language/Version**: Kotlin 2.0.21 — same toolchain as features 001 / 002.

**Primary Dependencies**: no additions. The Material 3 dependencies
already pulled in by feature 001's Compose BOM provide
`MaterialTheme`, `lightColorScheme`, `darkColorScheme`, `Typography`,
`CompositionLocalProvider`, the `Card`, `Icon`, `Text` and `Row`
composables used by the new result hero card and the top app bar
brandmark.

**Storage**: None (unchanged from features 001 / 002). Visual treatment
introduces no persistent state.

**Testing**:

- Compose UI screenshot-style assertions extending the existing
  `CompareScreenAccessibilityTest` to verify the new brandmark is
  marked decorative (`invisibleToUser` semantics) and that the title
  text "PriceGrab" remains the only TalkBack-announced top-bar text.
- Compose UI tests that drive the screen at 200% font scale in both
  `en-US` and `es-ES` and assert no truncation on the result hero
  card and offer cards.
- Compose UI tests that assert the result region is present after a
  valid comparison, and absent after a Reset (US2 + feature 002
  cohabitation).
- A new JVM unit test for the `Spacing` `CompositionLocal` providing
  default values when not overridden.
- The constitution's coverage gate is on `core/calc/**` (not on UI),
  unchanged.

**Target Platform**: Android, `minSdk` 24, `targetSdk` 35,
`compileSdk` 35 — unchanged from features 001 / 002.

**Project Type**: mobile-app (single Android application, no backend) —
unchanged.

**Performance Goals**:

- Cold start to interactive remains ≤ 2 seconds on a Pixel 4a-class
  device (constitution principle IV). Removing the dynamic color
  branch in `Theme.kt` slightly *reduces* startup cost on Android 12+
  (no `LocalContext` lookup + dynamic palette generation per launch).
- The new `ic_brandmark.xml` vector drawable is rasterized once on
  first composition and cached by Compose; rendering at 24dp inside
  the top app bar is free at frame budget.
- The result hero card uses the existing `Card` composable; no
  custom Canvas drawing, no shadow recalculation per frame.

**Constraints**:

- Fully offline (visual treatment never touches the network).
- No new permissions.
- F-Droid Mode B reproducibility preserved: vector drawable is
  hand-authored XML, no rasterization at build time, no environment-
  specific bytes.
- No data collection; no analytics SDK.
- No custom font (would push cold-start budget and APK size).
- No motion library (no Lottie, no custom motion specs beyond the
  Material 3 defaults).

**Scale/Scope**: 1 new vector drawable, ≈ 4 small theme files
modified, 1 hero card refactor inside `CompareScreen.kt`. Estimated
~150–250 LOC production + ~150–250 LOC test. APK growth target
≤ 200 KB (constitution-cap headroom is huge: v0.1.4 is ~1.6 MB
against a 15 MB cap).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Gates derived from `.specify/memory/constitution.md` v1.0.1.

| Principle / section                          | Gate                                                                                                   | Plan evidence                                                                                                                                                                                                                                       | Status |
|----------------------------------------------|--------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|
| I. Modern Mobile UX                          | Material 3, ≥48dp touch, no scroll in main flow, non-color cues, single-purpose UI                     | Visual treatment stays inside Material 3; no new touch target is added (the Reset `IconButton` still ≥48dp); the hero result card carries text + icon shape cues, never color alone; portrait fits without scroll on Pixel 4a (verified manually). |   ✅   |
| II. Accessibility (NON-NEGOTIABLE)           | TalkBack labels, 200% font scale, WCAG AA contrast, non-color-only cues, keyboard / D-pad reachability | Brandmark is `invisibleToUser`; result card text + icon survives 200% font scale (instrumented test); palette generated against WCAG AA contrast targets in both themes; no new control is added so reachability is unchanged.                       |   ✅   |
| III. Internationalization (ES/EN)            | All strings in resources, locale-aware behaviour, both locales tested                                  | At most a handful of new strings (e.g. `result_winner_a`, `result_winner_b`, `result_tied` if surfaced) live in `values/strings.xml` AND `values-es/strings.xml`; instrumented tests run in both locales.                                            |   ✅   |
| IV. Offline-First Performance & Reliability  | No network in core flow, cold start ≤ 2 s, APK ≤ 15 MB, no ANR, state preserved on config change       | Visual treatment is offline by construction; cold start unchanged or marginally improved (dynamic color removed); APK growth target ≤ 200 KB; rotation / theme / locale switch still preserves entered values via existing `SavedStateHandle`.       |   ✅   |
| V. Test-First Quality (NON-NEGOTIABLE)       | TDD, ≥ 90% coverage on calculator, instrumented tests for the happy path, CI blocks merge on red       | Calculator code is untouched (gate stays satisfied). New visual behavior (semantics for the brandmark, hero card visibility) introduced test-first; instrumented tests extend the existing `CompareScreen*Test` family in both locales.              |   ✅   |
| Privacy & Platform Constraints               | No analytics/tracking SDKs, minimal permissions, latest stable target SDK, F-Droid compatible          | No new SDKs; no new permissions; no manifest changes; F-Droid Mode B reproducibility preserved (no AGP feature toggles changed; new asset is plain XML).                                                                                              |   ✅   |
| Distribution (v1.0.1)                        | Signed APKs on GitHub Releases; reproducible build; no non-free blobs                                  | No build-script changes that affect signing or determinism. Next release continues to ship a single signed `app-release.apk` produced reproducibly.                                                                                                  |   ✅   |
| Development Workflow & Quality Gates         | Spec-Driven Development; feature branch; PR with spec reference; lint + tests green                    | Feature branch `003-visual-polish-branding`; this plan PR includes spec; CI required status checks remain in force.                                                                                                                                  |   ✅   |

**Outcome**: all gates pass on initial check. No entries needed in
Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/003-visual-polish-branding/
├── spec.md      # Feature specification
├── plan.md      # This file
├── research.md  # Phase 0 output (decisions backing this plan)
└── tasks.md     # Phase 2 output (created next via /speckit.tasks)
```

`data-model.md` and `contracts/` are intentionally absent: this
feature introduces no new domain types and no new module-level
contracts. Visual tokens (color, typography, spacing) are documented
inline in this plan and live in the existing `ui.theme` package.

`quickstart.md` is intentionally absent: feature 001's
[`quickstart.md`](../001-unit-price-comparison/quickstart.md) covers
the project-level workflow and remains the canonical entry point.
Manual verification steps for this feature are listed at the end of
this plan.

### Source code (changes are additive / replacing visual placeholders from feature 001)

```text
android/app/src/main/kotlin/com/mablanco/pricegrab/ui/theme/
├── Color.kt    # REPLACE — palette derived from launcher seed #2F5C73
├── Theme.kt    # MODIFY — drop dynamic color, drop dynamicColor parameter
├── Type.kt     # MODIFY — replace `Typography()` placeholder with explicit M3 scale
└── Spacing.kt  # NEW — CompositionLocal exposing Spacing(s = 4, m = 8, l = 16, xl = 24, xxl = 32)

android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/
└── CompareScreen.kt   # MODIFY — brandmark glyph in TopAppBar, hero result card

android/app/src/main/res/drawable/
└── ic_brandmark.xml   # NEW — 24dp vector; rounded square + price-tag glyph silhouette,
                      #  authored to echo the launcher icon's silhouette without
                      #  duplicating its detail

android/app/src/main/res/values/strings.xml      # +result_winner_a, +result_winner_b,
                                                 #  +result_tied (if not already keyed)
android/app/src/main/res/values-es/strings.xml   # mirror in Spanish

android/app/src/test/kotlin/com/mablanco/pricegrab/ui/theme/
└── SpacingDefaultsTest.kt  # NEW — verifies Spacing CompositionLocal default values

android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/
├── CompareScreenAccessibilityTest.kt        # MODIFY — assert brandmark is decorative,
                                             #  title text "PriceGrab" is the only top-bar
                                             #  semantic content
├── CompareScreenLayoutTest.kt               # NEW — assert hero result card is the
                                             #  visually dominant element when present,
                                             #  and absent after Reset
└── CompareScreenLargeFontTest.kt            # NEW — runs the screen at 200% font scale
                                             #  in en-US and es-ES, asserts no truncation
```

**Structure decision**: keep all theme tokens inside the existing
`ui.theme` package; keep the result hero card inside `CompareScreen.kt`
as a private composable. This feature does not warrant a new
`ui/components/` package — the result card is single-use and tightly
coupled to the comparison outcome state. If a second screen ever wants
to render a similar card, the right move is to extract at that point,
not pre-emptively here.

The launcher icon assets under `mipmap-*` are NOT touched; the
brandmark on screen is a *separate* asset (smaller, simpler, single
color) precisely so it scales gracefully at 24dp without antialiasing
artefacts that the launcher PNG would suffer from at that size.

### Theme tokens (Phase 1 design)

#### Brand-derived `ColorScheme` (replaces the current placeholder)

The seed color is `#2F5C73` (steel blue, sampled from the launcher
icon's corner background). The plan generates light + dark Material 3
roles using Google's [Material Theme Builder](https://m3.material.io/theme-builder)
methodology applied to that seed: each role is hand-pinned in
`Color.kt` so the build is deterministic and reproducible (no runtime
generation, no theme-builder dependency at compile time).

The implementer pastes `#2F5C73` into Material Theme Builder, exports
the Compose code, and copies the output values into the existing
`internal val Brand…` constants in `Color.kt`. Each pair is then
verified against WCAG AA contrast targets (≥4.5:1 normal text, ≥3:1
UI components) using a contrast-checker tool, in both light and dark.

#### Typography (replaces the placeholder `Typography()`)

The Material 3 default `Typography()` is currently used wholesale. We
keep the same Roboto Flex platform default but pin explicit weight and
spacing assignments so the existing surfaces gain a more deliberate
hierarchy:

| Surface                                  | M3 token             | Weight     | Notes                                  |
|------------------------------------------|----------------------|------------|----------------------------------------|
| Top app bar title ("PriceGrab")          | `titleLarge`         | Medium     | Centered, paired with brandmark glyph. |
| Offer card label ("Offer A", "Offer B")  | `titleMedium`        | Medium     | Sentence-case in both locales.         |
| Field label ("Price", "Quantity")        | `labelLarge`         | Regular    | Above the input fields.                |
| Helper / error text                      | `bodySmall`          | Regular    | Below the input field, error-tinted.   |
| Hero result headline                     | `headlineSmall`      | Bold       | The visual focal point of the screen.  |
| Hero result body (savings, difference)   | `bodyLarge`          | Regular    | Beneath the headline.                  |

The mapping itself lives in `CompareScreen.kt`; `Type.kt` exports the
explicit `Typography(...)` block with these assignments and is the
single source of truth for any future surface that wants the same
hierarchy.

#### Spacing rhythm

A `Spacing` data class is exposed via a `CompositionLocal` so the
spacing scale is consistent across the screen and any future screen
without hand-rolling `dp` literals everywhere. Defaults: `s=4.dp`,
`m=8.dp`, `l=16.dp`, `xl=24.dp`, `xxl=32.dp`. The Compare screen
adopts these in: card-to-card vertical gap (`l`), inside-card padding
(`l`), result-card top margin (`xl`), top-bar to first card gap (`l`).

### Brandmark glyph asset

`res/drawable/ic_brandmark.xml` is hand-authored as a 24dp
`<vector>` with `android:tint="?attr/colorPrimary"` so it picks up
the brand `primary` color from the active `ColorScheme` (and
inverts cleanly in dark mode without a separate dark drawable).

The glyph silhouette echoes the launcher icon's structure (rounded
square frame with an inner price-tag shape) but is intentionally
*simpler* — the launcher's photographic detail does not survive at
24dp, whereas a single-path silhouette stays crisp and readable.

The asset is committed (not regenerated at build time) for
reproducibility.

### Compare screen layout (Phase 1 design)

```text
┌─────────────────────────────────────────┐
│  TopAppBar:  [glyph]  PriceGrab    [↻]  │  ← brandmark + title + reset (feature 002)
├─────────────────────────────────────────┤
│                                         │
│  Offer A                                │
│  ┌────────────────────────────────────┐ │
│  │ Price: ____   Quantity: ____       │ │
│  └────────────────────────────────────┘ │
│                                         │
│  Offer B                                │
│  ┌────────────────────────────────────┐ │
│  │ Price: ____   Quantity: ____       │ │
│  └────────────────────────────────────┘ │
│                                         │
│  ╔═════════════════════════════════════╗│  ← Hero result card (US2):
│  ║  ✓ Offer A is cheaper               ║│    bold headline, icon cue,
│  ║  Save 0.20 €/L                      ║│    elevated surface,
│  ╚═════════════════════════════════════╝│    only visible when result exists
│                                         │
└─────────────────────────────────────────┘

(SnackbarHost reuses feature 002's positioning at the bottom edge,
visually compatible with the new layout.)
```

The top-bar height stays at the Material 3 default
`CenterAlignedTopAppBar` height (no oversized header). Reset
(`Icons.Filled.Refresh`) stays in the trailing slot from feature 002.
The hero card uses Material 3 `CardDefaults.elevatedCardElevation` so
its prominence comes from elevation + size, not from a different
color (which could have failed the non-color-only-cue principle).

The icon cue inside the hero card is Material 3 `Icons.Filled.Check`
for "winner" outcomes and `Icons.Filled.DragHandle` (or
`Icons.Filled.Remove`) for "tied". Both are part of
`material-icons-core` (same family the Reset action uses), so no
`material-icons-extended` dependency is added.

## Complexity Tracking

> Fill ONLY if Constitution Check has violations that must be justified.

No violations. Table intentionally empty.

## Manual verification (no quickstart.md)

After landing this feature, the following 90-second walkthrough should
pass on any device running PriceGrab v0.1.5-SNAPSHOT or later. The
walkthrough is independent of the Reset / Undo walkthrough from
feature 002 (which must continue to pass unchanged).

1. Launch the app on a clean cold start. Cold start to interactive
   stays ≤ 2 s on a Pixel 4a-class device (no regression vs v0.1.4).
2. The top app bar shows a small glyph immediately preceding the text
   "PriceGrab"; the glyph color matches the new brand `primary`. The
   reset action (refresh icon) from feature 002 is still in the
   trailing slot.
3. Switch the device to dark mode. The brand identity stays coherent:
   the glyph adapts color via tint, no surface inverts unexpectedly,
   text contrast remains readable.
4. Enter `2.50 / 500` for offer A and `4.00 / 1000` for offer B. The
   hero result card appears below the offer cards with: a winner icon
   cue (✓), a bold headline ("Offer B is cheaper" or "Offer A is
   cheaper"), and the per-unit savings beneath it. The card is
   visually the most prominent element on the screen.
5. Tap the reset action. The hero card disappears; the screen
   reflows naturally without an empty rectangle. The Snackbar from
   feature 002 still appears with its Undo affordance.
6. Repeat steps 4–5 with the device language set to Spanish. All
   labels (offer card title, result headline, result body) appear in
   Spanish; no truncation.
7. Increase the system font scale to 200% (Settings → Display → Font
   size). Repeat step 4 in both English and Spanish. No text on the
   screen is truncated; the screen still fits without horizontal
   scroll on a Pixel 4a viewport.
8. Enable TalkBack and traverse the top app bar. TalkBack announces
   "PriceGrab" exactly once (from the title text); it does NOT
   announce the brandmark glyph. The reset action (feature 002) is
   announced as before. The hero card, when present, announces the
   winner and the savings.
9. On Android 12+ with a colourful wallpaper: the colors on the
   Compare screen come from the brand-derived palette, NOT from
   Material You / wallpaper-derived dynamic color. A side-by-side
   comparison with the v0.1.4 build on the same device must show
   the same brand colors regardless of the wallpaper.

If any step fails, the feature is not complete; do not tag a release.
