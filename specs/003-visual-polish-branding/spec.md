# Feature Specification: Visual Polish & Branding for the Compare Screen

**Feature Branch**: `003-visual-polish-branding`
**Created**: 2026-04-26
**Status**: Draft
**Input**: User description: "Polish the visual style of the Unit-Price Comparison Screen to make it more engaging and on-brand, while preserving WCAG-AA accessibility, ES/EN parity, and the offline / cold-start budget. Open question to resolve in the spec: whether the app name (or some equivalent branding element) should be visible on the comparison screen."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Recognizable, modern first impression (Priority: P1)

A first-time shopper opens PriceGrab in a supermarket aisle. Today the
Compare screen is functional but visually generic: a centered top-bar title,
two unstyled offer cards stacked vertically, a small text result. After this
feature, the same shopper opens the app and sees a screen that immediately
feels like a finished product — coherent typography hierarchy, deliberate use
of color, clear visual grouping of "Offer A vs Offer B vs Result" — without
any animation or background that delays the moment they can start typing.
The screen still fits in portrait without scrolling on a Pixel 4a-class device.

**Why this priority**: This is the value proposition of the feature. A shopper
deciding whether to keep the app installed makes that judgement on first
impression; the math has to be correct (it is) *and* the screen has to look
like a product someone cared about. This is what feature 003 is for.

**Independent Test**: Side-by-side visual comparison of the Compare screen at
v0.1.4 vs the build produced by this feature, on a Pixel 4a-class device in
both `en-US` and `es-ES`, both light and dark mode. The screen must look
clearly more polished and on-brand to a third-party reviewer who is not
otherwise involved in the project, and must remain fully usable in all four
combinations without regressions.

**Acceptance Scenarios**:

1. **Given** the user launches PriceGrab v0.1.5 on a fresh install,
   **When** the Compare screen appears, **Then** the visual identity is
   recognizable as PriceGrab (color, typographic hierarchy, branding
   element from FR-001) within 2 seconds of cold start, with no flicker
   between an unstyled and styled state.
2. **Given** the user is on the Compare screen,
   **When** they look at the screen at arm's length under aisle lighting,
   **Then** the boundary between "input area for Offer A", "input area for
   Offer B", and "result area" is unambiguous from layout / spacing /
   elevation alone, without needing to read labels.
3. **Given** the user toggles between light and dark mode,
   **When** the Compare screen redraws, **Then** the brand expression
   remains coherent (same logo / accent treatment philosophy in both
   themes; no inverted-looking widgets, no contrast drops).
4. **Given** the user rotates the device (config change),
   **When** the screen recomposes, **Then** the visual treatment is
   identical and no entered text or computed result is lost.

---

### User Story 2 — More prominent, still accessible result (Priority: P2)

A shopper has typed two prices and two quantities. The current "winner"
headline is a small text line above a textual statement of the unit-price
difference. After this feature, the result is the visual focal point of
the screen the moment it appears: clearly which offer wins (or that they
are tied), how much cheaper per unit, and which offer that corresponds
to in the layout above. The win/tie cue is conveyed by **text + icon /
shape + position**, never by color alone (constitution principle II).

**Why this priority**: The whole point of the screen is to deliver a
decision; once delivered, the decision should be the loudest thing on
screen. This is independent of US1 (US1 changes the chrome around the
inputs; US2 changes the result region) and could be shipped on its own
as a smaller MVP if needed.

**Independent Test**: With both US1 enabled and disabled, US2 alone makes
the result region visually dominant when present, and invisible /
neutral when absent. TalkBack still announces the result content first
when the comparison completes (no semantics regression).

**Acceptance Scenarios**:

1. **Given** the user has filled all four fields with valid numbers,
   **When** the result becomes available, **Then** the result region is
   the most visually prominent element on the screen (size, weight,
   spacing) and points to the winning offer in a way that is obvious
   without reading.
2. **Given** the user has cleared the fields (Reset from feature 002),
   **When** the result region disappears, **Then** the input area
   reflows naturally and the screen does not feel "missing a chunk".
3. **Given** TalkBack is enabled and the result becomes available,
   **When** focus moves to the result, **Then** the announcement is at
   least as informative as in v0.1.4 (winner offer + difference).
4. **Given** the user views the result in dark mode at 200% font scale,
   **When** the screen renders, **Then** all result text is readable
   (no truncation, no contrast below WCAG AA).

---

### User Story 3 — On-screen branding decision (Priority: P3)

The app currently uses the system top-app-bar title "PriceGrab" for
branding. The user has explicitly raised the question of whether
on-screen branding (app name, wordmark, logo glyph, or a tagline)
deserves more presence on the Compare screen, or whether the launcher
icon alone is sufficient and the screen should stay maximally focused
on the comparison task.

**Why this priority**: This is a deliberate product decision rather than
a usability gap; the app works without it. Resolving it upfront avoids
rework later and gives the brand identity introduced in US1 a place to
live. Marked P3 because it is the lowest-risk-to-defer of the three
stories: even if we ship US1 + US2 alone, the app is more polished.

**Independent Test**: Once the branding placement is chosen
(see FR-001 / [NEEDS CLARIFICATION] markers), the chosen treatment can
be added or removed in isolation without touching the input or result
regions. A reviewer can validate the choice with the same visual
side-by-side as US1.

**Acceptance Scenarios**:

1. **Given** the design decision in [NEEDS CLARIFICATION 1] resolves
   to "show app name on screen", **When** the Compare screen renders,
   **Then** the on-screen branding occupies less than 12% of the
   portrait viewport vertical space and never obscures or pushes any
   input below the fold on a Pixel 4a-class device.
2. **Given** the design decision in [NEEDS CLARIFICATION 1] resolves
   to "no on-screen branding", **When** the Compare screen renders,
   **Then** the visual hierarchy from US1 still feels intentional
   without a wordmark / logo (i.e. the screen does not look "missing
   a header").
3. **Given** any branding element is added,
   **When** TalkBack reaches it, **Then** it is either announced as
   the app name (with role = heading) or marked decorative if it is
   purely visual ornament alongside an existing announceable label.

---

### Edge Cases

- **200% system font scale**: any new typographic hierarchy MUST hold.
  Result region in particular cannot grow so large that, at 200%
  scale, it pushes inputs below the fold or truncates the winner
  callout.
- **Dynamic color (Material You) on Android 12+**: see
  [NEEDS CLARIFICATION 2]. If we opt in, brand expression must still
  be recognizable when dynamic color is *not* available
  (Android < 12 or user has not set a wallpaper-derived palette).
- **Dark mode contrast drop**: every new text/background pair MUST hit
  WCAG 2.1 AA (≥4.5:1 normal text, ≥3:1 large text & UI components)
  in **both** light and dark themes.
- **Long localized strings**: Spanish typically runs ~15–25% longer
  than English. Any new label must hold without truncation in `es-ES`
  at 200% font scale.
- **Reset/Undo from feature 002**: the snackbar, the disabled-reset
  state, and the focus-on-Price-A-after-reset behavior MUST be
  visually compatible with the new style. No functional regressions.
- **Cold-start budget**: any new resource (logo asset, custom font,
  background image) added by this feature must keep cold-start ≤ 2s
  on a Pixel 4a-class device. Custom fonts in particular often cost
  100–300ms — if we use one, it has to be justified against this
  budget.
- **APK size budget**: the constitution caps APK at 15 MB. v0.1.4 is
  ~1.6 MB; this feature must not push us beyond ~2 MB.
- **Process death restoration**: any visual state that depends on
  user input (e.g. result-prominence styling reacting to
  hasResult / hasError) MUST work after process death (already covered
  by the `SavedStateHandle` infrastructure from feature 002, but the
  feature MUST NOT introduce new ephemeral UI state that loses on
  process death).

## Requirements *(mandatory)*

### Functional Requirements

#### Visual identity (US1 + US3)

- **FR-001**: The Compare screen MUST display a coherent visual brand
  identity (color, typography hierarchy, spacing rhythm, optional
  brand mark) that makes it recognizable as PriceGrab at a glance.
  [NEEDS CLARIFICATION 1: branding element placement —
  (a) app-name wordmark inside the top app bar (replacing the plain
  text title), (b) logo glyph only as a small icon next to the
  title, (c) full wordmark + glyph centered above the inputs,
  (d) no on-screen branding (rely on launcher icon + top app bar
  title alone). What does Marco want?]
- **FR-002**: The Compare screen MUST stay within Material 3 (Material
  You) components, typography scale, elevation tokens, and motion
  primitives. No third-party UI library, no custom Canvas-based
  branding asset that bypasses Material 3 theming.
- **FR-003**: The brand seed color MUST be a single source of truth
  used to derive the Material 3 `ColorScheme` for both light and
  dark themes. [NEEDS CLARIFICATION 3: brand seed color value —
  (a) match the dark teal of the launcher icon
  (`branding/regenerate-icons.py` already exposes this via the
  `BACKGROUND` constant and the icon's sampled corner color),
  (b) a freshly-chosen accent color, (c) Material You dynamic color
  on Android 12+ with a fixed seed fallback. The choice in
  [NEEDS CLARIFICATION 2] determines whether (c) is even on the table.]
- **FR-004**: The Compare screen MUST work coherently in BOTH the
  app's brand-derived palette AND, on Android 12+, optionally
  Material You dynamic color. [NEEDS CLARIFICATION 2: Material You
  dynamic color opt-in — (a) opt in (use `dynamicColorScheme()` on
  Android 12+, fall back to brand-derived `ColorScheme` below),
  (b) opt out (always ship the brand-derived `ColorScheme` for
  consistency), (c) opt in but document a manual override toggle
  (out of scope for v0.1.5 if so).]
- **FR-005**: The visual treatment MUST be coherent in both light and
  dark theme. The user-perceived brand identity (logo, accent color,
  emphasis hierarchy) MUST not invert, disappear, or drop below
  WCAG 2.1 AA contrast in either theme.

#### Result prominence (US2)

- **FR-006**: When a valid comparison result is available, the result
  region MUST be the visually dominant element of the Compare screen
  (size of the winner indicator, weight of the headline, surrounding
  whitespace), and MUST clearly point to the winning offer (Offer A,
  Offer B, or "tied") through layout, label, and a non-color cue
  (icon or shape) — never color alone.
- **FR-007**: When no result is available (incomplete inputs, parse
  error, or after a Reset / Undo), the result region MUST collapse
  gracefully so the input area does not feel orphaned.

#### Accessibility, i18n and constitution-driven invariants

- **FR-008**: Every new piece of visible text introduced by this
  feature MUST live in `res/values/strings.xml` AND
  `res/values-es/strings.xml`. Hardcoded strings are forbidden.
- **FR-009**: Every interactive control or branding element that is
  not purely decorative MUST expose a meaningful semantics label for
  TalkBack. Purely decorative ornaments MUST be excluded from
  semantics (`Modifier.semantics { invisibleToUser() }` or
  equivalent).
- **FR-010**: The Compare screen MUST honor the system font scale up
  to at least 200% with no truncation, overlap, or loss of
  functionality, in both `en-US` and `es-ES`, both light and dark.
- **FR-011**: Touch targets for any new interactive element MUST be
  ≥ 48dp × 48dp.
- **FR-012**: Every new text/background pair MUST meet WCAG 2.1 AA
  contrast in BOTH light and dark themes (normal text ≥4.5:1, large
  text and UI components ≥3:1).
- **FR-013**: Configuration changes (rotation, theme switch, locale
  switch) MUST preserve all currently-entered values and the
  current result, with no flicker between an unstyled and styled
  state.
- **FR-014**: The feature MUST NOT introduce any new Android
  permission, any new network request, or any tracking / analytics
  / proprietary SDK. Bundle stays free-software for F-Droid.
- **FR-015**: The feature MUST preserve the Reset / Undo flow from
  feature 002 untouched at the behavior level: the reset icon
  remains in the top app bar trailing, the Snackbar still appears
  with the same lifetime semantics, ON_STOP still dismisses the
  Undo affordance.

### Key Entities *(visual system)*

- **Brand seed color**: a single hex value that drives the Material 3
  `ColorScheme` derivation for light and dark mode. Resolution
  pending [NEEDS CLARIFICATION 3].
- **Typography hierarchy**: an explicit assignment of Material 3
  typography tokens (`displayMedium`, `headlineSmall`, `titleLarge`,
  `bodyLarge`, `labelMedium`, …) to each piece of text on the Compare
  screen (top-app-bar title, offer card label, field label, helper
  text, result headline, result body). Until this feature, we use
  defaults; with this feature, the assignment is intentional.
- **Spacing rhythm**: an explicit set of spacing values
  (e.g. 4 / 8 / 12 / 16 / 24 dp) used consistently between fields,
  cards, and the result region.
- **Brand mark (optional)**: a logo glyph and/or wordmark, depending
  on the resolution of [NEEDS CLARIFICATION 1].

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Cold start to interactive on a Pixel 4a-class device
  remains ≤ 2 seconds (constitution principle IV). Measured the same
  way as v0.1.4 baseline.
- **SC-002**: Release APK growth from v0.1.4 ≤ 200 KB (i.e. APK stays
  comfortably under the 15 MB constitutional cap).
- **SC-003**: Lint / detekt / unit tests / instrumented tests all
  green in CI, on every PR in the feature 003 cadence.
- **SC-004**: Manual visual review by Marco confirms the Compare
  screen feels meaningfully more on-brand and polished than v0.1.4
  in both `en-US` and `es-ES`, both light and dark.
- **SC-005**: TalkBack smoke test on the implementation PR passes:
  every interactive or semantic element is announceable, no
  decorative ornament is announced as content, the result is still
  the most informative announcement on the screen.
- **SC-006**: WCAG 2.1 AA contrast verification on every new
  text/background pair, in both themes.
- **SC-007**: 200% font scale: no truncation, overlap, or clipped
  controls in either locale, with the Compare screen still fitting
  the viewport without horizontal scroll.
- **SC-008**: ES/EN string parity: every new string exists in both
  `values/strings.xml` and `values-es/strings.xml`; instrumented
  tests cover at least one screenshot or assertion per locale.

## Assumptions

- **Material 3 stays the design system**. No Compose Material 2,
  no third-party UI kit (Skydoves, Accompanist, etc.) for visual
  treatment. Only `androidx.compose.material3` already in the
  dependency graph.
- **Launcher icon palette is the brand starting point**. The dark
  teal of the launcher icon (introduced in v0.1.3) is the working
  hypothesis for the brand seed unless [NEEDS CLARIFICATION 3]
  resolves otherwise.
- **No new feature behavior**. This is purely a visual / branding
  feature. No history, no multi-product comparison, no share, no
  settings UI. Reset / Undo from feature 002 is preserved
  unchanged behaviorally.
- **No custom fonts**. Cold-start budget is tight; we ship with
  Material 3's default Roboto Flex (or platform default). If a
  custom font is ever desired, it is a separate feature.
- **No animations beyond Material 3 defaults**. No Lottie, no
  custom motion specs. This stays visual treatment, not motion
  design.
- **Single screen scope**. Only the Compare screen is touched.
  There is no other screen in the app. The feature does NOT
  introduce a settings screen, an about screen, or any navigation.
- **Out of scope: dynamic theming UI**. The user does not choose
  light/dark from inside the app; the app follows the OS setting
  (already the case).
- **Out of scope: brand assets governance**. We do not introduce
  a brand-asset folder beyond what `branding/` already contains.
  If a logo glyph asset is needed, it is committed under
  `branding/` alongside the existing icon-source assets.
- **Reproducibility**. Anything new added to the build (vector
  drawable, color resource, typography import) MUST keep the
  Mode-B reproducibility property from feature 001 / `docs/fdroid.md`.
  No timestamped resources, no machine-specific metadata.

## Open clarifications to resolve before `/speckit.plan`

These are the three [NEEDS CLARIFICATION] markers above, restated in
one place for the clarification round:

1. **[NEEDS CLARIFICATION 1] Branding element placement on the
   Compare screen.** Options:
   (a) wordmark inside the top app bar replacing the plain text
   title; (b) logo glyph only, as a small icon next to the title;
   (c) full wordmark + glyph centered above the inputs; (d) no
   on-screen branding (rely on launcher icon + plain top-bar title).
2. **[NEEDS CLARIFICATION 2] Material You dynamic color (Android
   12+) opt-in.** Options: (a) opt in with brand-derived fallback;
   (b) opt out (always brand-derived for consistency); (c) opt in
   with a manual override toggle (toggle is itself out of scope for
   v0.1.5 if chosen).
3. **[NEEDS CLARIFICATION 3] Brand seed color.** Options:
   (a) match the launcher icon's dark teal; (b) a freshly-chosen
   accent color; (c) tied to the resolution of clarification 2 if
   we go full Material You.
