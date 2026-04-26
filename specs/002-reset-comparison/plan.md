# Implementation Plan: Reset / Start a New Comparison

**Branch**: `002-reset-comparison` | **Date**: 2026-04-26 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-reset-comparison/spec.md`

## Summary

Close the basic comparison loop the v1 MVP left open. Add a single Reset
control to the existing `CompareScreen` that, when tapped on a non-empty
form, atomically clears the four input fields and the visible result, and
surfaces a transient Material 3 Snackbar offering "Undo" for a finite
window. Tapping Undo restores the four fields and the result exactly as
they were before the Reset; letting the Snackbar time out, typing into a
field, or backgrounding the app dismisses the Undo affordance.

Technical approach: extend the existing `CompareViewModel` with an
in-memory `PreResetSnapshot` that captures the pre-reset `CompareUiState`,
plus a `UndoState` exposed alongside the screen state. The
`CompareScreen` Composable is wrapped in a `Scaffold` whose `topBar`
hosts a Material 3 `TopAppBar` with a single trailing `IconButton`
(Reset), and whose `snackbarHost` consumes a `SharedFlow<ResetEvent>`
emitted by the ViewModel through a `LaunchedEffect`. No new modules, no
new dependencies, no persistence layer changes; everything lives in
existing ViewModel + Composable files plus their test counterparts.

## Technical Context

**Language/Version**: Kotlin 2.0.21 — same toolchain as feature 001.
**Primary Dependencies**: no additions. The Material 3 dependencies
already pulled in by feature 001's Compose BOM provide `Scaffold`,
`TopAppBar`, `IconButton`, `Snackbar`, `SnackbarHost`, and
`SnackbarDuration`.

**Storage**: None (unchanged from feature 001). The `PreResetSnapshot`
required by Undo lives only in the ViewModel for the lifetime of the
Snackbar, in line with the spec's no-persistence guarantee (FR-011).

**Testing**:

- JVM unit tests for the new ViewModel behaviour (`reset`, `undoReset`,
  `dismissUndo`, idempotence on already-empty form).
- Compose UI tests covering the four acceptance scenarios in spec.md US1
  (one-tap reset on a completed comparison, reset on partial input, reset
  on already-empty form is a no-op, TalkBack announcement) and US2 (Undo
  restores; Undo expires; typing dismisses; rotation preserves).
- Locale tests for English and Spanish strings.
- The constitution's coverage gate is on `core/calc/**` (not on UI),
  unchanged.

**Target Platform**: Android, `minSdk` 24, `targetSdk` 35, `compileSdk`
35 — unchanged from feature 001.

**Project Type**: mobile-app (single Android application, no backend) —
unchanged.

**Performance Goals**:

- Reset action operates in < 16 ms (one frame) — trivial state mutation
  on the main thread.
- Snackbar appearance respects Material 3 motion specs (no extra tuning
  required; the default `SnackbarHost` animations are within budget).
- No regression on cold start (the additional code is < 200 LOC and
  loaded as part of the existing Compose tree).

**Constraints**:

- Fully offline (Reset and Undo never touch the network).
- No new permissions.
- F-Droid Mode B reproducibility preserved (no new build features that
  could embed environment-specific bytes).
- No data collection; the `PreResetSnapshot` is never logged, serialised,
  or forwarded.

**Scale/Scope**: 1 control, 1 transient affordance, ~6 new strings per
locale. Estimated ~150–250 LOC production + ~250–400 LOC test.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Gates derived from `.specify/memory/constitution.md` v1.0.1.

| Principle / section                          | Gate                                                                                                   | Plan evidence                                                                                                                                                                                                          | Status |
|----------------------------------------------|--------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|
| I. Modern Mobile UX                          | Material 3, ≥48dp touch, no scroll in main flow, non-color cues, single-purpose UI                     | Reset is a Material 3 `IconButton` inside a `TopAppBar` (≥48dp touch by default); Undo is a Material 3 `Snackbar` with explicit text + action label (no color-only cue); the screen retains its single-purpose layout |   ✅   |
| II. Accessibility (NON-NEGOTIABLE)           | TalkBack labels, 200% font scale, WCAG AA contrast, non-color-only cues, keyboard / D-pad reachability | Reset `IconButton` declares `contentDescription`; Snackbar message + action are both reachable via TalkBack and are inherently announced (Snackbar is a polite live region in Material 3); strings sized in `sp`        |   ✅   |
| III. Internationalization (ES/EN)            | All strings in resources, locale-aware behaviour, both locales tested                                  | New strings (`reset_action`, `reset_action_description`, `comparison_cleared`, `undo_action`, `reset_announcement`) added to `values/strings.xml` and `values-es/strings.xml`; locale instrumented test extended        |   ✅   |
| IV. Offline-First Performance & Reliability  | No network in core flow, cold start ≤ 2 s, APK ≤ 15 MB, no ANR, state preserved on config change       | No network use; reset/undo are O(1) main-thread operations; `PreResetSnapshot` and `UndoState` survive rotation via `SavedStateHandle` (deadline epoch ms persisted); no growth in APK from new dependencies            |   ✅   |
| V. Test-First Quality (NON-NEGOTIABLE)       | TDD, ≥ 90% coverage on calculator, instrumented tests for the happy path, CI blocks merge on red       | Calculator code is untouched (gate stays satisfied). New ViewModel and UI behaviour are introduced test-first; instrumented tests extend the existing `CompareScreen*Test` family in both locales                       |   ✅   |
| Privacy & Platform Constraints               | No analytics/tracking SDKs, minimal permissions, latest stable target SDK, F-Droid compatible           | No new SDKs; no new permissions; no manifest changes beyond what feature 001 already declared; F-Droid Mode B reproducibility preserved (no AGP feature toggles changed)                                                |   ✅   |
| Distribution (v1.0.1)                        | Signed APKs on GitHub Releases; reproducible build; no non-free blobs                                  | No build-script changes that affect signing or determinism. Next release continues to ship a single signed `app-release.apk`                                                                                            |   ✅   |
| Development Workflow & Quality Gates         | Spec-Driven Development; feature branch; PR with spec reference; lint + tests green                    | Feature branch `002-reset-comparison`; this plan PR includes spec; CI required status checks remain in force                                                                                                            |   ✅   |

**Outcome**: all gates pass on initial check. No entries needed in
Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/002-reset-comparison/
├── spec.md      # Feature specification
├── plan.md      # This file
├── research.md  # Phase 0 output (decisions backing this plan)
└── tasks.md     # Phase 2 output (created next)
```

`data-model.md` and `contracts/` are intentionally absent: this feature
introduces no new domain types and no new module-level contracts. The
single transient in-memory record (`PreResetSnapshot`) is documented
inline in this plan (see *In-memory state additions* below) and lives
in the existing `ui.compare` package.

`quickstart.md` is intentionally absent: feature 001's
[`quickstart.md`](../001-unit-price-comparison/quickstart.md) covers the
project-level workflow and remains the canonical entry point. Manual
verification steps for this feature are listed at the end of this plan.

### Source code (changes are additive to feature 001's tree)

```text
android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/
├── CompareUiState.kt        # +UndoState data class, fields on existing CompareUiState
├── CompareViewModel.kt      # +reset(), +undoReset(), +dismissUndo(), +SavedStateHandle keys
└── CompareScreen.kt         # Wrapped in Scaffold; +TopAppBar with reset action; +SnackbarHost

android/app/src/main/res/values/strings.xml      # +reset_action, +reset_action_description,
                                                 #  +comparison_cleared, +undo_action,
                                                 #  +reset_announcement
android/app/src/main/res/values-es/strings.xml   # mirror in Spanish

android/app/src/test/kotlin/com/mablanco/pricegrab/ui/compare/
├── CompareViewModelResetTest.kt          # NEW — JVM tests for reset / undo / dismiss / idempotence
└── CompareViewModelSavedStateTest.kt     # MAY-be-new — covers UndoState round-trip via SavedStateHandle

android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/
├── CompareScreenResetTest.kt             # NEW — instrumented one-tap reset, focus, announcement
├── CompareScreenUndoTest.kt              # NEW — instrumented undo, timeout, typing-dismiss, rotation
└── CompareScreenAccessibilityTest.kt     # MODIFIED — extends existing test with reset / snackbar checks
```

**Structure decision**: keep everything inside the existing
`ui.compare` package. The reset / undo concept is intrinsic to the
Compare screen and does not warrant a new package. The `core/` layer is
not touched: there is no calculation change. The split between `core/`
and `ui/` introduced by feature 001 stays clean.

### In-memory state additions

The `CompareUiState` data class gains one optional field:

```kotlin
data class CompareUiState(
    // ... existing four *Raw + four *Error + outcome fields from feature 001
    val undoState: UndoState? = null,
)

data class UndoState(
    val snapshot: PreResetSnapshot,
    /**
     * Wall-clock instant (epoch milliseconds) at which the Undo affordance
     * should auto-dismiss. Used to compute the remaining lifetime after
     * configuration change so the affordance survives rotation with its
     * remaining lifetime intact (FR-006, AS-2.4).
     */
    val expiresAtEpochMillis: Long,
)

data class PreResetSnapshot(
    val priceARaw: String,
    val quantityARaw: String,
    val priceBRaw: String,
    val quantityBRaw: String,
)
```

`PreResetSnapshot` does **not** carry the cached `outcome`: on `Undo` the
ViewModel's existing `recomputeOutcome` rebuilds it deterministically
from the four raw strings, so the result on screen is reproduced
character-for-character without storing it twice.

`UndoState.expiresAtEpochMillis` is persisted in `SavedStateHandle`
together with the `PreResetSnapshot`; `expiresAtEpochMillis` makes the
"survive rotation with remaining lifetime intact" guarantee in spec
AS-2.4 mechanically achievable instead of restarting the timer.

### ViewModel API additions

```kotlin
class CompareViewModel(...) : ViewModel() {
    // existing onPriceAChange / onQuantityAChange / onPriceBChange / onQuantityBChange

    /** True iff at least one of the four raw fields is non-empty. Drives FR-004. */
    val isResetEnabled: StateFlow<Boolean>

    /** Atomically: capture snapshot, clear fields, hide outcome, start UndoState. */
    fun resetComparison()

    /** Restore the four fields from the active UndoState, then clear it. */
    fun undoReset()

    /** Clear the active UndoState without restoring (timeout / typing / backgrounding). */
    fun dismissUndo()
}
```

The existing `onPriceAChange` etc. are extended to call `dismissUndo()`
internally if `undoState != null`, satisfying FR-008.1 ("the shopper
begins typing into any of the four input fields"). The lifecycle
listener that observes `ON_STOP` (or absence of `ViewModel` retention,
in the case of process death) is wired in `MainActivity` /
`PriceGrabApp` and calls `dismissUndo()` for FR-008.3.

## Complexity Tracking

> Fill ONLY if Constitution Check has violations that must be justified.

No violations. Table intentionally empty.

## Manual verification (no quickstart.md)

After landing this feature, the following 60-second walkthrough should
pass on any device running PriceGrab v0.1.4-SNAPSHOT or later.

1. Launch the app on a clean cold start. Verify the Reset button in the
   top app bar is *visibly disabled* (greyed out / non-interactive).
2. Enter `2.50 / 500` for offer A and `4.00 / 1000` for offer B. The
   result card should show "Offer B is cheaper" with savings. The Reset
   button should now be enabled.
3. Tap Reset. All four fields clear, the result card disappears,
   keyboard focus moves to Price A, and a Snackbar appears at the bottom
   with text "Comparison cleared" and an "Undo" button.
4. Tap Undo before the Snackbar disappears. All four fields and the
   result card return to the state from step 2, byte-for-byte.
5. Repeat the reset, this time wait the full Snackbar duration (~10 s)
   without tapping Undo. The Snackbar disappears; the form stays empty.
6. Repeat the reset, this time start typing a new value into Price A
   while the Snackbar is still visible. The Snackbar disappears
   immediately and the new value lands in Price A.
7. Repeat the reset and rotate the device while the Snackbar is visible.
   The Snackbar must still be visible after rotation (with whatever
   lifetime remained), and tapping Undo must still restore the previous
   state.
8. Switch the device language to Spanish from Settings. Repeat steps 1–4
   and verify the Reset button's content description, the Snackbar
   message ("Comparación borrada"), and the Undo action label
   ("Deshacer") are all in Spanish.
9. Enable TalkBack and repeat step 3. TalkBack must announce that the
   comparison has been cleared and must read the Undo action when the
   focus reaches it.

If any step fails, the feature is not complete; do not tag a release.
