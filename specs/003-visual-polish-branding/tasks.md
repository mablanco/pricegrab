---
description: "Task list for feature 003 — visual polish & branding for the Compare screen"
---

# Tasks: Visual Polish & Branding for the Compare Screen

**Input**: Design documents from `/specs/003-visual-polish-branding/`
**Prerequisites**: spec.md (required), plan.md (required), research.md (required)
**Tests**: INCLUDED. The constitution (Principle V, NON-NEGOTIABLE) and
the spec (FR-008..FR-013) require tests, so every story below explicitly
lists its test tasks and they must be written **before** the
corresponding implementation tasks (TDD).

**Organization**: tasks are grouped by user story from `spec.md`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on
  incomplete tasks).
- **[Story]**: `[US1]` (visual identity), `[US2]` (hero result card).
  US3 (on-screen branding decision) has no implementation tasks of its
  own — it is resolved into FR-001 and absorbed by US1's brandmark
  task. Cross-cutting tasks carry no story label.
- File paths are absolute within the repository.

---

## Phase 1: Foundational (theme tokens)

**Purpose**: Replace the Material 3 placeholder theme with the
brand-derived palette, the explicit typography hierarchy and the
spacing rhythm. No screen surface is touched yet — all Compare-screen
visual changes ride on top of this phase. Adds no behaviour on its
own; both US1 and US2 plug into the same theme tokens.

**⚠️ CRITICAL**: no user-story work begins until this phase passes.

### Tests (write first, watch them fail)

- [X] T001 [P] Write `android/app/src/test/kotlin/com/mablanco/pricegrab/ui/theme/SpacingDefaultsTest.kt` asserting that the new `Spacing` data class exposes the documented defaults (`s = 4.dp`, `m = 8.dp`, `l = 16.dp`, `xl = 24.dp`, `xxl = 32.dp`) and that `LocalSpacing.current` returns those defaults inside a `CompositionLocalProvider`-less composable scope. This test fails to compile until T004 lands.

### Implementation

- [X] T002 Replace the entire palette in `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/theme/Color.kt` with values exported from [Material Theme Builder](https://m3.material.io/theme-builder) using `#2F5C73` as the source color. Pin every Material 3 role pair (primary / onPrimary / primaryContainer / onPrimaryContainer / secondary / onSecondary / secondaryContainer / onSecondaryContainer / tertiary / onTertiary / error / onError / background / onBackground / surface / onSurface) for both light and dark. Verify each text/background pair against WCAG 2.1 AA contrast targets (≥4.5:1 normal text, ≥3:1 UI components) using a contrast checker; adjust the non-content side if any pair fails. Document the contrast verification in a leading comment of `Color.kt` (date + the tool used). The variable names already in `Color.kt` (`BrandPrimary`, `BrandOnPrimary`, etc.) stay; only the hex values change.
- [X] T003 [P] Modify `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/theme/Theme.kt` to drop dynamic color: remove the `dynamicColor: Boolean = true` parameter, remove the `dynamicLightColorScheme(ctx)` / `dynamicDarkColorScheme(ctx)` branches, and the corresponding `LocalContext.current` lookup. Keep the existing `darkTheme: Boolean = isSystemInDarkTheme()` parameter. After this task, `PriceGrabTheme` is a thin wrapper around `MaterialTheme(colorScheme = …, typography = …)`. Make sure the call sites in `PriceGrabApp.kt` and any `@Preview` Composables still compile (they use `PriceGrabTheme` without arguments, so this is a non-breaking change).
- [X] T004 [P] Replace the `PriceGrabTypography: Typography = Typography()` placeholder in `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/theme/Type.kt` with an explicit `Typography(...)` block that sets, at minimum: `titleLarge` (Medium weight), `titleMedium` (Medium), `labelLarge` (Regular), `bodyLarge` (Regular), `bodySmall` (Regular), `headlineSmall` (Bold). All other roles inherit Material 3 defaults. Add a leading comment listing which UI surface owns which token (the table in `plan.md` §"Typography").
- [X] T005 [P] Create `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/theme/Spacing.kt` with: a `Spacing` data class (`val s: Dp = 4.dp, val m: Dp = 8.dp, val l: Dp = 16.dp, val xl: Dp = 24.dp, val xxl: Dp = 32.dp`); a `LocalSpacing = staticCompositionLocalOf { Spacing() }`; an extension property `MaterialTheme.spacing: Spacing get() = LocalSpacing.current` so call sites read `MaterialTheme.spacing.l`. T001 should now pass.

**Checkpoint**: `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:detekt` all green. The Compare screen looks identical to v0.1.4 to the user (no surface uses the new spacing tokens yet, no surface uses the explicit typography assignments yet, `Color.kt` has new values but the screen happens to use roles that look similar enough). Cold start re-verified ≤ 2 s on a Pixel 4a-class device — confirm no regression vs v0.1.4 baseline.

---

## Phase 2: User Story 1 — Recognizable, modern first impression (P1) 🎯 MVP

**Goal**: The Compare screen visibly carries the new brand identity:
the brandmark glyph appears immediately before the "PriceGrab" title in
the top app bar, the typography hierarchy in input cards reads as
deliberate, the spacing rhythm replaces hand-rolled `dp` literals.
TalkBack still announces "PriceGrab" exactly once and ignores the
glyph.

**Independent test**: launch the app and visually compare the Compare
screen with the v0.1.4 build; the new build clearly shows the
brandmark glyph in the top app bar and a more deliberate visual
hierarchy.

### Tests for US1 (write first, watch them fail)

- [X] T006 [P] [US1] Extend `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenAccessibilityTest.kt` with two new assertions: (a) the brandmark `Icon` node is marked `invisibleToUser` (not announced by TalkBack); (b) the top app bar exposes a single semantic node with text "PriceGrab" (i.e. no second node with a `contentDescription` from the glyph). Use `onAllNodes(hasParent(hasTestTag("topBar"))).assertCountEquals(...)` or equivalent.
- [X] T007 [P] [US1] Write `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenLargeFontTest.kt` that drives the screen at 200% font scale in both `en-US` and `es-ES` and asserts: (a) the top app bar title "PriceGrab" / "PriceGrab" is fully visible (no truncation); (b) every offer card label, field label, and helper text fits without truncation; (c) the Compare screen still fits the viewport without horizontal scrolling on a Pixel 4a-class emulator (`width = 411dp`).

### Implementation for US1

- [X] T008 [US1] Author `android/app/src/main/res/drawable/ic_brandmark.xml` as a 24dp `<vector>`. Geometry: a single-path silhouette of a rounded square frame (matching the launcher icon's frame proportions) with an inset price-tag glyph. Use `android:tint="?attr/colorPrimary"` so the glyph adapts to light/dark via the `ColorScheme`. Keep the file under 2 KB. Reference the launcher icon's silhouette but do NOT copy `ic_launcher_foreground` paths verbatim — the brandmark must read at 24dp.
- [X] T009 [US1] Modify `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreen.kt` to render the brandmark inside the existing `CenterAlignedTopAppBar`. Change the `title = { Text(...) }` slot to `title = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(painterResource(R.drawable.ic_brandmark), contentDescription = null, modifier = Modifier.size(24.dp).semantics { invisibleToUser() }); Spacer(Modifier.width(MaterialTheme.spacing.s)); Text(stringResource(R.string.app_name)) } }`. Keep the trailing reset `IconButton` from feature 002 untouched.
- [X] T010 [US1] Replace hand-rolled `dp` literals in `CompareScreen.kt` with `MaterialTheme.spacing.*` references at: card-to-card vertical gap (`l`), inside-card padding (`l`), top-bar to first card gap (`l`). Field-internal padding stays at the Material 3 `OutlinedTextField` defaults; do not micromanage.
- [X] T011 [US1] Apply explicit typography tokens in `CompareScreen.kt`: offer card label uses `style = MaterialTheme.typography.titleMedium`; field labels use `style = MaterialTheme.typography.labelLarge`; helper / error text uses `style = MaterialTheme.typography.bodySmall`. The result region's typography lands in Phase 3 (US2).

**Checkpoint**: T006–T007 are green. The brandmark glyph is visible in the top app bar; TalkBack still reads only "PriceGrab"; the screen fits at 200% font scale in both locales. The result region still looks the same as in v0.1.4 (US2 has not landed yet). CI green.

---

## Phase 3: User Story 2 — More prominent, still accessible result (P2)

**Goal**: When a valid comparison result exists, render it in an
elevated `Card` with an icon cue (`Icons.Filled.Check` for a winner,
`Icons.Filled.DragHandle` for a tie), a bold `headlineSmall` headline,
and a `bodyLarge` body line with the per-unit savings. The card
collapses entirely when no result is available, leaving the input
flow untouched. The non-color cue (icon + size + elevation) is
sufficient to convey the outcome without color.

**Independent test**: enter `2.50 / 500` for offer A and `4.00 / 1000`
for offer B; the hero result card appears; tap reset; the card
disappears.

### Tests for US2 (write first, watch them fail)

- [X] T012 [P] [US2] Write `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenLayoutTest.kt` covering: (a) before any input, the hero result card is absent (no node with the test-tag `heroResult`); (b) after entering valid values for both offers, the hero result card is present and contains a winner icon (`Icons.Filled.Check`) AND a non-empty headline AND a non-empty body; (c) after tapping the reset action (feature 002), the hero result card disappears within one frame and the rest of the screen reflows without a stale empty rectangle.
- [X] T013 [P] [US2] Extend `CompareScreenAccessibilityTest.kt` with: (a) when the hero result card is present, TalkBack's announcement order is "Offer A" → "Offer B" → "Result" (i.e. the result is the last semantic node in the screen's reading order, matching the visual order); (b) the hero card's icon is `invisibleToUser` and the announcement comes from the headline + body text only; (c) the hero card's headline carries `Role.Heading` semantics so TalkBack announces it as a heading.

### Implementation for US2

- [X] T014 [US2] Add new strings to `android/app/src/main/res/values/strings.xml` (English): `result_winner_a` ("Offer A is cheaper"), `result_winner_b` ("Offer B is cheaper"), `result_tied` ("Same price per unit"), `result_savings` (formatted string with `%1$s` for the per-unit currency / unit, e.g. "Save %1$s per unit"). Add a `result_card_test_tag` if the layout test needs it. **Do NOT remove** the existing result strings used by v0.1.4 yet — the implementation may want to keep the textual phrasing identical for `talkback` consistency; remove unused strings only after all references are updated.
- [X] T015 [US2] Mirror T014 in `android/app/src/main/res/values-es/strings.xml`: "La oferta A es más barata" / "La oferta B es más barata" / "Mismo precio por unidad" / "Ahorras %1$s por unidad".
- [X] T016 [US2] Refactor the result region inside `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreen.kt` into a private `@Composable HeroResultCard(state: CompareUiState)` that returns early (renders nothing) when `state.outcome == null`. When a result exists, render an `ElevatedCard(elevation = CardDefaults.elevatedCardElevation())` containing a `Row` with: leading `Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.semantics { invisibleToUser() })` for winners or `Icon(Icons.Filled.DragHandle, …)` for ties; followed by a `Column` with a `Text(style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })` for the headline and a `Text(style = MaterialTheme.typography.bodyLarge)` for the savings line. Apply `MaterialTheme.spacing.xl` as top padding so the hero card visually separates from the offer cards. Add `Modifier.testTag("heroResult")` for T012.
- [X] T017 [US2] Wire the hero card into the main column of `CompareScreen` immediately after the second offer card. Make sure the hero card respects the existing column's `Modifier.fillMaxWidth()` and the Reset / Undo flow from feature 002 keeps working: typing into a field while the hero card is visible must NOT dismiss the Snackbar (feature 002's logic); typing while the Snackbar is visible MUST dismiss it (feature 002's logic). No regressions on the `CompareScreenResetTest` / `CompareScreenUndoTest` instrumented tests from feature 002.

**Checkpoint**: T012–T013 are green. Reset / Undo from feature 002 still works (its tests still pass). The screen's result region is now visually dominant when present and absent otherwise. CI green.

---

## Phase 4: Release prep

**Purpose**: Mirror the cadence from feature 002 (Phase 4) so the
F-Droid recipe never lags behind `main`. The actual release-cut work
(manual verification on device, tag push, F-Droid `Builds:` sync)
ships in PR N — see "PR strategy" below — but the prep tasks
(version bump, changelogs, ledger) ride along with PR M so the
implementation PR lands a release-ready `main`.

- [X] T018 [P] Bump `android/app/build.gradle.kts`: `versionCode = 6`, `versionName = "0.1.5"`.
- [X] T019 [P] Create `fastlane/metadata/android/en-US/changelogs/6.txt` with a 2–4 line description of the visual polish & branding feature (no marketing fluff; mention the brandmark in the top app bar, the new palette derived from the launcher icon, the more prominent result card, and the explicit Material 3 typography / spacing). Cap at ~500 characters.
- [X] T020 [P] Mirror T019 in `fastlane/metadata/android/es-ES/changelogs/6.txt` in Spanish.
- [X] T021 Update the master PR ledger in `specs/001-unit-price-comparison/tasks.md` with a new `### PR O — 003-visual-polish-branding` (planning only) section, a `### PR P — feat/018-visual-polish-impl` (implementation) section, and a `### PR Q — chore/019-fdroid-doc-sync-v0.1.5` (release cut) section. Cross-reference the feature 002 ledger entries (PR L / PR M / PR N) for the cadence.
- [ ] T022 Manual verification on a real device using the 9-step walkthrough from `plan.md`'s "Manual verification" section. Record cold-start regression (must stay below 2 s budget). Do this on the signed v0.1.5 release APK (not on a debug build) so the verification matches what F-Droid will republish.
- [ ] T023 Tag `v0.1.5` from `main` after the implementation PR merges and CI is green. Verify the `Signed release APK` job publishes `app-release.apk` to the v0.1.5 GitHub Release.
- [ ] T024 Update `docs/fdroid.md` §3 build recipe and §5 chronology to point at v0.1.5: bump `Builds[0]` to `versionName: 0.1.5` / `versionCode: 6` / `commit: <full SHA of v0.1.5>`, and bump `CurrentVersion` / `CurrentVersionCode` accordingly. Coordinate with the GitLab MR edit in the same window.

---

## PR strategy

Same three-PR cadence introduced in feature 002 (PR L / PR M / PR N).
Feature 003's PR letters in the global master ledger are O / P / Q.

### PR O — `003-visual-polish-branding` *(planning only)*

This PR ships only `specs/003-visual-polish-branding/` (spec.md,
plan.md, research.md, tasks.md) and the `.specify/feature.json`
pointer bump. No source code, no build script, no test changes. Marco
reviews the contract (visual identity decisions, FR-001..FR-015,
SC-001..SC-008) and signs off on it before any Kotlin / drawable
lands.

### PR P — `feat/018-visual-polish-impl` *(implementation)*

Phases 1–3 (T001–T017) plus the release-prep tasks (T018–T021).
Lands the brand-derived palette, the explicit typography / spacing
tokens, the brandmark glyph in the top app bar, and the hero result
card. Branched off `main` *after* PR O merged, so the planning
artefacts are on `main` before any code lands. The implementation
diff is bounded by the task list and is expected to land at
~400–600 LOC including tests, well under the spec's 200 KB APK growth
target.

### PR Q — `chore/019-fdroid-doc-sync-v0.1.5` *(release cut)*

Covers Phase 4 tasks T022–T024 (manual verification on device, tag
push, F-Droid `Builds:` sync). Mirrors the v0.1.4 cadence (PR N from
feature 002) so the F-Droid recipe never lags more than one release
behind `main`. The tag push (T023) happens outside the PR diff;
this PR is the pure-doc twin that bumps `docs/fdroid.md` to v0.1.5.
Manual on-device verification (T022) is independent and gates the
upstream GitLab MR edit, not this PR's merge.

### Notes

- Each PR must keep the main branch green (lint, detekt, unit tests, JaCoCo coverage gate, instrumented tests).
- Commit after each task or logical group; keep the commit message in English, Conventional Commits prefix (`feat:`, `test:`, `chore:`, …).
- Stop at each phase checkpoint and validate before moving on.
- If a `[NEEDS CLARIFICATION]` surfaces while executing a task, STOP and raise it with Marco rather than inventing an answer.
