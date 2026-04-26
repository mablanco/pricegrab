# Research: Visual Polish & Branding for the Compare Screen (Feature 003)

Phase 0 research consolidating the decisions that the plan depends on.
Each entry follows the **Decision / Rationale / Alternatives considered**
format. The first three decisions resolve the `[NEEDS CLARIFICATION]`
markers from the initial spec draft (FR-001, FR-003, FR-004), already
recorded in [`spec.md`](./spec.md) under "Initial clarifications
resolved". They are restated here for traceability and to capture the
*alternatives considered*, which the spec deliberately omits.

The remaining entries (4–9) capture supporting design decisions that
emerged while writing the plan and have no `[NEEDS CLARIFICATION]`
marker but are equally load-bearing for the implementation.

## 1. Branding placement on the Compare screen (resolves FR-001)

**Decision**: A **small logo glyph immediately preceding the
plain-text "PriceGrab" title** inside the Material 3
`CenterAlignedTopAppBar`. The title remains a regular `Text`
composable. The glyph is decorative (`invisibleToUser` semantics).

**Rationale**:

- Keeps the title a plain `Text` so TalkBack announces "PriceGrab"
  with zero extra `contentDescription` work, satisfying constitution
  principle II without a custom semantics block.
- Brings the launcher icon's silhouette inside the screen, so the
  brand recognition built up by the user (who tapped the icon to
  launch the app) carries over visually.
- Smallest possible vertical footprint: the top app bar height stays
  at the Material 3 default. Compare screen still fits without scroll
  on a Pixel 4a-class device in both portrait orientations.
- Lower risk than swapping the title for a graphical wordmark (which
  would require a `contentDescription` and would also be more
  expensive to localize in a hypothetical future locale).

**Alternatives considered**:

- *(a) Wordmark only — title replaced by a graphical mark*: more
  visual impact but requires a dedicated `contentDescription` per
  locale, and risks contrast / pixel-fitting issues at small densities
  on older devices. The risk is not justified by the gain on a
  utility app of this size.
- *(c) Wordmark + glyph as a dedicated header above the inputs*:
  occupies real estate that the inputs need; risks pushing the result
  card below the fold on small viewports at 200% font scale.
- *(d) No on-screen branding at all*: directly defeats Marco's
  explicit goal of *"reforzar el branding de la app"*.

## 2. Material You dynamic color opt-in (resolves FR-004)

**Decision**: **Opt out**. The Compare screen always renders with
the brand-derived `ColorScheme`, regardless of OS version or
wallpaper. Drop the existing `dynamicColor: Boolean = true` parameter
from `PriceGrabTheme` in `Theme.kt` and remove the
`dynamicLightColorScheme` / `dynamicDarkColorScheme` branches.

**Rationale**:

- PriceGrab is a single-purpose, branded utility. Consistency of
  brand identity across users and devices is more valuable than
  wallpaper-derived theming, which is a per-user expression that
  would dilute the app's distinct visual character.
- F-Droid listing screenshots, GitHub README screenshots, and any
  future store metadata benefit from a *predictable* visual that
  doesn't change based on a tester's wallpaper.
- Removing the dynamic-color branch slightly *reduces* cold start on
  Android 12+ (no `LocalContext` lookup + dynamic palette derivation
  per launch).
- Reduces the Compose Material 3 surface used by the app and the
  number of code paths that need accessibility verification (one
  palette → one set of contrast checks, not "one palette per
  conceivable wallpaper").

**Alternatives considered**:

- *(a) Opt in (dynamic color on Android 12+, brand-derived
  fallback)*: trendy, aligned with Google's "Material You" push,
  but trades brand consistency for personalization on a screen the
  user looks at for ~30 seconds at a time. Cost > benefit for a
  utility.
- *(c) Opt in + per-user toggle in a settings screen*: adds an
  entire settings surface (currently zero screens beyond Compare)
  for a feature whose value is unclear. Out of scope for v0.1.5;
  re-considered if user demand surfaces.

## 3. Brand seed color (resolves FR-003)

**Decision**: Use `#2F5C73` (steel blue) as the Material 3 seed
color. This is the value already pinned in
`branding/regenerate-icons.py` as `BG_HEX`, sampled from the four
corners of the launcher icon source image and used to fill the
adaptive icon background.

**Rationale**:

- Reusing the existing `BG_HEX` keeps a single source of truth: the
  launcher icon, the adaptive-icon background, and the in-app
  `ColorScheme` all derive from one decision. Anyone touching the
  brand seed in the future updates one file (`regenerate-icons.py`)
  and re-derives the in-app palette — no two places to keep in sync.
- The icon (with this exact background) was approved for v0.1.3 and
  is the version submitted to F-Droid. Reusing it in-screen
  preserves the visual contract the F-Droid screenshot pipeline
  already shows.
- Material 3's tonal palette generation produces well-balanced
  light + dark `ColorScheme`s from any reasonable hex; `#2F5C73` has
  enough chroma and lightness contrast to drive both modes without
  the typical "muddy primary container" failure that pure-greys
  produce.

**Alternatives considered**:

- *(b) Freshly chosen brand seed*: forces an independent decision
  about brand identity, with the corresponding need to also update
  the launcher icon to match. Out of scope for v0.1.5.
- *(c) Material You dynamic color (no fixed seed)*: rejected by
  research entry 2.

## 4. ColorScheme generation method

**Decision**: Use [Material Theme Builder](https://m3.material.io/theme-builder)
**at design time only**: paste `#2F5C73` as the source color, export
the Compose Color values, paste them into the existing
`internal val Brand…` constants in `Color.kt`. Build-time generation
is **not** done.

**Rationale**:

- Determinism / reproducibility: the constants in `Color.kt` are
  the same on every machine and every build; F-Droid Mode B requires
  byte-for-byte identical APKs, so any "compute palette at build
  time" approach is a non-starter.
- No Gradle dependency on a theme-builder or palette-generator
  library. The implementer pastes the values once and the file is
  reviewed normally.
- The hand-pinned values are also where WCAG AA contrast checks are
  performed (entry 5 below): we verify the *exact* values that ship,
  not values that a future palette-generation algorithm bump might
  silently change.

**Alternatives considered**:

- Computing the palette at runtime via `dynamicColorScheme(...)` from
  a fixed seed: not a thing in stock Compose Material 3; would
  require pulling in `androidx.compose.material3:material3` extras
  or a third-party library, with associated APK and reproducibility
  cost.
- Hand-picking each role from a designer's intuition: easy to drift
  away from a coherent tonal palette. The Theme Builder output is a
  well-tested baseline; tweaks happen on top of it if a contrast
  check fails.

## 5. WCAG AA contrast verification

**Decision**: Every text/background pair introduced or modified in
`Color.kt` is verified against WCAG 2.1 AA contrast targets (≥4.5:1
normal text, ≥3:1 large text + UI components) in BOTH light and
dark mode, using a contrast checker (e.g.,
[contrast-grid](https://contrast-grid.eightshapes.com) or
[colour-contrast-analyser](https://www.tpgi.com/color-contrast-checker/)).
Any pair that fails AA is adjusted (lightened/darkened the
non-content side) before the implementation PR opens.

**Rationale**:

- Constitution principle II is a release blocker. Material Theme
  Builder produces palettes that *target* AA but does not guarantee
  it for every role pair on every theme; a manual sanity check
  closes the loop.
- The check happens in the planning / implementation phase, not
  as part of CI: there is no Compose-friendly automated WCAG
  contrast tool in the AGP / detekt ecosystem at the time of this
  feature. (If one ships during the feature's lifetime, we revisit.)

**Alternatives considered**:

- Skipping the check and trusting Material Theme Builder: rejected
  on principle (constitution principle II is "release blocker, not a
  polish item").
- Adding a CI job that runs a WCAG checker: no mature open-source
  Android-CI integration exists today; add later as a separate
  feature if/when one matures.

## 6. Brandmark vector drawable strategy

**Decision**: Author a new `res/drawable/ic_brandmark.xml` as a
24dp `<vector>` with `android:tint="?attr/colorPrimary"`. The
silhouette is a simplified version of the launcher's rounded square
+ inner price-tag glyph, designed to remain readable at 24dp.

**Rationale**:

- A vector drawable is ~1 KB and scales infinitely without
  rasterization artefacts; far better than re-rasterizing the
  existing `ic_launcher_foreground.png` to 24dp.
- `android:tint="?attr/colorPrimary"` makes the glyph adapt to
  light/dark mode automatically through the `ColorScheme`, so we
  don't ship a `drawable-night/` variant.
- Keeps the launcher icon and the in-app brandmark visually
  related but not identical: the launcher does the photographic /
  detailed treatment (bigger surface, higher density), the in-screen
  glyph does the reductive / iconic treatment (smaller surface,
  must read at a glance).
- F-Droid Mode B reproducibility preserved: hand-authored XML, no
  build-time rasterization.

**Alternatives considered**:

- Reuse `mipmap-*/ic_launcher_foreground.png` directly at 24dp:
  visually muddy due to PNG rasterization at small densities; would
  also lock the brand glyph color to the launcher's foreground PNG
  (no theme tint).
- Use a built-in `Icons.Filled.ShoppingCart` from
  `material-icons-core`: zero APK growth, but the glyph is generic
  and doesn't carry PriceGrab's identity.
- Pull `material-icons-extended` for `Icons.Filled.Storefront`,
  `Icons.Filled.PriceCheck`, etc.: ~6 MB APK growth. Disqualified by
  the 15 MB cap and by the constitution's "justify any dependency
  added" rule.

## 7. Typography hierarchy

**Decision**: Replace the `Typography()` placeholder in `Type.kt`
with explicit Material 3 type tokens, using the platform Roboto Flex
default (no custom font ship). Pin the assignments to UI surfaces in
the plan's typography table (`titleLarge` → top-bar title,
`titleMedium` → offer card label, `headlineSmall` (bold) → result
hero headline, etc.).

**Rationale**:

- Custom fonts cost cold-start budget (typically 100–300 ms before
  text becomes visible) and APK size (typically 100–500 KB for a
  variable font). Both are scarce on a utility app where the
  constitution caps cold start at 2 s and APK at 15 MB.
- Material 3's default `Typography` is already deliberate; we just
  need to pin which surface uses which token, instead of accepting
  Compose's per-component default (which is e.g. `bodyLarge` for
  most `Text` calls).
- Centralizing the assignment in `Type.kt` is the single source of
  truth: any future surface uses the same hierarchy without
  re-deriving it.

**Alternatives considered**:

- Ship a custom font (Inter, Atkinson Hyperlegible, etc.):
  appealing for branding but cost not justified for v0.1.5. Defer.
- Leave the placeholder and just use bigger font weights inline in
  `CompareScreen.kt`: works for a single screen but bakes in
  inconsistency the moment a second screen is added. Easier to fix
  it once now.

## 8. Spacing rhythm via `CompositionLocal`

**Decision**: Introduce `Spacing(s = 4.dp, m = 8.dp, l = 16.dp,
xl = 24.dp, xxl = 32.dp)` exposed via a `LocalSpacing`
`CompositionLocal`. The Compare screen uses these tokens for
card-to-card gap (`l`), inside-card padding (`l`), result-card top
margin (`xl`), top-bar to first card gap (`l`).

**Rationale**:

- Material 3 does not ship a spacing scale (typography and color
  yes, spacing no). Hand-rolling `dp` literals at every call site
  drifts; a tiny `CompositionLocal` eliminates that without a
  dependency.
- Five values are enough granularity for a single-screen utility;
  not so many that a designer has to reason about which one to
  pick.
- Trivial to test (a JVM unit test against the default values is
  enough; no Compose UI test needed).

**Alternatives considered**:

- Hardcode `dp` literals everywhere: works but invites drift the
  moment a second screen lands.
- Adopt a third-party design-token library: APK + dependency cost
  not justified for ~5 constants.
- Use `dimensionResource(R.dimen.spacing_l)` from
  `res/values/dimens.xml`: Android-classic, but every call site
  forces a `Compose → Resources` round-trip at composition time.
  `CompositionLocal` is the Compose-native equivalent.

## 9. Result hero card design

**Decision**: When a valid comparison result exists, render an
elevated Material 3 `Card` (`elevatedCardElevation`) below the offer
cards, containing: an icon cue (`Icons.Filled.Check` for a winner,
`Icons.Filled.DragHandle` for a tie) on the leading side,
followed by a `headlineSmall` headline ("Offer A is cheaper" / "Offer
B is cheaper" / "Same price per unit") and a `bodyLarge` body line
with the per-unit savings. The card collapses entirely when no
result is available.

**Rationale**:

- Constitution principle I requires the result to be conveyed by
  text + non-color cue. Elevation + size + icon + headline weight
  satisfy this redundantly; a colour-blind shopper still parses the
  outcome.
- Elevation is a *Material 3* signal of importance; using it (rather
  than a saturated background color) avoids new contrast pairs to
  audit and stays within the brand-derived `ColorScheme`.
- The hero card is the only new layout element this feature
  introduces; placement below the inputs (rather than at the top)
  keeps the input flow intact for users who type-then-glance, and
  matches the natural reading order (top-to-bottom = act-then-see).

**Alternatives considered**:

- Background color tint on the winning offer card: relies on color
  alone for the cue, which violates constitution principle I.
- Modal bottom sheet on result: turns a glanceable comparison into
  a modal interaction; loses the at-a-glance value.
- Always-visible result card (showing "—" before any input): adds
  visual noise when the screen is empty; the empty-state collapse
  better reflects the screen's "scratch pad" nature.

## 10. Out of scope

The following are deliberately **not** in feature 003 and are not
research items — they are deferred for clarity:

- **Splash screen branding**. Android 12+'s `SplashScreen` API
  could surface the brand glyph during cold start. Deferred to a
  future feature so v0.1.5's diff stays focused. The default
  splash is acceptable for now.
- **Settings screen / theme override**. As stated in research entry
  2; unresolved if a future user request demands it.
- **Custom font**. As stated in research entry 7.
- **Animation / motion design**. The Compose Material 3 default
  enter/exit transitions for the hero card are sufficient; no
  Lottie, no choreographed motion specs.
- **Landscape layout polish**. The constitution scopes the comparison
  flow to portrait; landscape continues to work but is not redesigned
  for this feature.
- **Tablet layout polish**. Same reasoning as landscape; no two-pane
  layout, no foldable handling.
- **Brand asset governance file** (e.g. a `branding/STYLE.md`).
  The launcher icon, brandmark glyph, and seed color are
  documented inline in this plan and in
  `branding/regenerate-icons.py`. A dedicated style doc is
  tracked separately if the brand grows.
