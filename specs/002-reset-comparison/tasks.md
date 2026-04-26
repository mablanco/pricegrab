---
description: "Task list for feature 002 — reset / start a new comparison"
---

# Tasks: Reset / Start a New Comparison

**Input**: Design documents from `/specs/002-reset-comparison/`
**Prerequisites**: spec.md (required), plan.md (required), research.md
**Tests**: INCLUDED. The spec (FR-001..FR-011) and the constitution
(Principle V, NON-NEGOTIABLE) both require tests, so every story below
explicitly lists its test tasks and they must be written **before** the
corresponding implementation tasks (TDD).

**Organization**: tasks are grouped by user story from `spec.md`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on
  incomplete tasks).
- **[Story]**: `[US1]` (one-tap reset) or `[US2]` (undo). Cross-cutting
  tasks carry no story label.
- File paths are absolute within the repository.

---

## Phase 1: Foundational

**Purpose**: Extend the existing Compare screen scaffolding with the
shared structure both stories need (`Scaffold` shell, `TopAppBar`,
`SnackbarHost`, new strings, new state types). Adds no behaviour on its
own; both US1 and US2 plug into the same shell.

**⚠️ CRITICAL**: no user-story work begins until this phase passes.

### Tests (write first, watch them fail)

- [ ] T001 [P] Write `android/app/src/test/kotlin/com/mablanco/pricegrab/ui/compare/CompareViewModelStateShapeTest.kt` asserting that the new fields land on `CompareUiState`: `undoState: UndoState?` defaults to `null`, and `isResetEnabled` derives `false` when all four `*Raw` fields are blank and `true` otherwise. This test fails to compile until T005–T006 land.

### Implementation

- [ ] T002 [P] Extend `android/app/src/main/res/values/strings.xml` (English) with the five new strings: `reset_action`, `reset_action_description`, `comparison_cleared`, `undo_action`, `reset_announcement_for_no_snackbar_path`. The fifth is currently unused (research §5) but is reserved for the no-Snackbar branch and prevents drift if the design changes.
- [ ] T003 [P] Mirror T002 in `android/app/src/main/res/values-es/strings.xml` with the Spanish translations: "Reiniciar", "Reiniciar comparación", "Comparación borrada", "Deshacer", "Comparación reiniciada".
- [ ] T004 Wrap `CompareScreen` in `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreen.kt` with a `Scaffold` whose `topBar` is a `CenterAlignedTopAppBar` and whose `snackbarHost` is a `SnackbarHost(snackbarHostState)`. The `topBar` carries a single trailing `IconButton` with `Icons.Filled.Restore`; both `enabled` and `onClick` are wired to placeholders that compile but do nothing yet (real wiring lands in US1 / US2 phases). Existing layout stays as the `Scaffold`'s content.
- [ ] T005 [P] Introduce `PreResetSnapshot` and `UndoState` data classes in `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareUiState.kt` per the schema in `plan.md` ("In-memory state additions"). Add `undoState: UndoState? = null` as a new field on `CompareUiState`.
- [ ] T006 Add a derived `isResetEnabled: StateFlow<Boolean>` to `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareViewModel.kt` (computed via `state.map { it.priceARaw.isNotBlank() || it.quantityARaw.isNotBlank() || it.priceBRaw.isNotBlank() || it.quantityBRaw.isNotBlank() }.stateIn(viewModelScope, SharingStarted.Eagerly, false)`). Wire the `IconButton`'s `enabled` parameter in `CompareScreen.kt` to this flow.

**Checkpoint**: `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:detekt` all green. The screen now shows a permanently-visible-but-disabled Reset icon in the top app bar; tapping it does nothing yet. T001 should pass after T005–T006 are in.

---

## Phase 2: User Story 1 — Start a new comparison without leaving the screen (P1) 🎯 MVP

**Goal**: One tap on the Reset action clears all four input fields and
the visible result, moves keyboard focus to Price A, and announces the
reset to TalkBack via the Snackbar shown in Phase 3 (or a no-op when the
form was already empty).

**Independent test**: enter four valid values, observe a result, tap
Reset, verify all four fields are empty and the result area is hidden.

### Tests for US1 (write first, watch them fail)

- [ ] T007 [P] [US1] Write `android/app/src/test/kotlin/com/mablanco/pricegrab/ui/compare/CompareViewModelResetTest.kt` covering: (a) `resetComparison()` on a non-empty state clears all four `*Raw` fields and sets `outcome = null`; (b) `resetComparison()` on an already-empty state is a no-op (no event emitted, no `UndoState` created); (c) `resetComparison()` populates `undoState` with a `PreResetSnapshot` containing the pre-reset values when the form was non-empty.
- [ ] T008 [P] [US1] Write `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenResetTest.kt` covering: (a) tapping the Reset icon after entering values clears all four fields and hides the result card; (b) the Reset icon is disabled (`SemanticsProperties.Disabled` is set) on cold launch and becomes enabled after typing into any field; (c) after Reset, keyboard focus is on the Price A field (assert `SemanticsProperties.Focused` on the Price A node).
- [ ] T009 [P] [US1] Extend `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenAccessibilityTest.kt` with: (a) the Reset icon node carries a non-empty `contentDescription` matching `R.string.reset_action_description`; (b) the Reset icon node has `Role.Button` semantics; (c) the disabled state is exposed via `SemanticsProperties.Disabled`.

### Implementation for US1

- [ ] T010 [US1] Add `resetComparison()` to `CompareViewModel.kt`. Implementation: capture the current four `*Raw` strings into a `PreResetSnapshot`; if all four are blank, return early (FR-004 / AS-1.3); otherwise, persist the snapshot + a deadline (`now + SNACKBAR_LONG_MS`) into `SavedStateHandle`; clear the four `*Raw` keys (and the in-memory `_state` mirror); rebuild the state with `recomputeOutcome` so errors and outcome are cleared.
- [ ] T011 [US1] Wire the Reset `IconButton.onClick` in `CompareScreen.kt` to `viewModel::resetComparison`. Add a `FocusRequester` for the Price A `OutlinedTextField`; on a `LaunchedEffect(state.undoState)` keyed by reset events (or by transitions of `priceARaw` from non-blank to blank initiated by Reset), call `focusRequester.requestFocus()`.
- [ ] T012 [US1] Make `onPriceAChange`/`onQuantityAChange`/`onPriceBChange`/`onQuantityBChange` in `CompareViewModel.kt` also call `dismissUndo()` (defined in Phase 3 — until then, leave a `// TODO(US2)` comment and a no-op stub). Verify T007 still passes.

**Checkpoint**: T007–T009 are green. The Reset button works as a one-tap form-clear. There is no Undo affordance yet (Phase 3); the user is left with an empty form and no recourse, which is acceptable as an intermediate state inside this PR but **must not ship to a release**. CI green.

---

## Phase 3: User Story 2 — Recover from an accidental reset (P2)

**Goal**: After every successful Reset on a non-empty form, a Material 3
Snackbar with an "Undo" action appears for ~10 s. Tapping Undo restores
the four fields and the result. Typing into any field, the Snackbar
timer expiring, or backgrounding the app dismiss the affordance.

**Independent test**: tap Reset on a non-empty form, observe the
Snackbar, tap Undo, verify all four fields and the result are restored.

### Tests for US2 (write first, watch them fail)

- [ ] T013 [P] [US2] Extend `CompareViewModelResetTest.kt` (or split into a sibling `CompareViewModelUndoTest.kt`) with: (a) `undoReset()` after a `resetComparison()` restores the four `*Raw` fields character-for-character and clears `undoState`; (b) `dismissUndo()` clears `undoState` without restoring; (c) calling any of the four `on*Change` methods while `undoState != null` triggers `dismissUndo()` *before* applying the change (assert ordering by checking that the snapshot is not consumed).
- [ ] T014 [P] [US2] Write `android/app/src/test/kotlin/com/mablanco/pricegrab/ui/compare/CompareViewModelSavedStateTest.kt` asserting that an `UndoState` with `expiresAtEpochMillis` in the future round-trips through `SavedStateHandle` (simulate process death by constructing a new ViewModel with the same handle), and that an UndoState whose deadline is already in the past is dropped silently on rebuild.
- [ ] T015 [P] [US2] Write `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenUndoTest.kt` covering: (a) Snackbar appears with `R.string.comparison_cleared` after Reset; (b) tapping the Undo action restores all four fields and the result; (c) typing into Price A while the Snackbar is visible dismisses the Snackbar; (d) waiting longer than `SnackbarDuration.Long` makes the Snackbar disappear without restoring; (e) rotating the device while the Snackbar is visible re-shows the Snackbar with the remaining lifetime, and Undo still works after rotation.
- [ ] T016 [P] [US2] Add a Spanish-locale assertion to `CompareScreenUndoTest.kt` (or a sibling `CompareScreenUndoLocaleEsTest.kt` if `Locale.setDefault` instability — see feature 001 T034 — recurs): the Snackbar reads "Comparación borrada" and the action label reads "Deshacer" when the device locale is `es-ES`.

### Implementation for US2

- [ ] T017 [US2] Add `undoReset()` and `dismissUndo()` methods to `CompareViewModel.kt`. `undoReset()` reads the active `UndoState`, restores the four `*Raw` keys in `SavedStateHandle`, clears the `UndoState` keys, and triggers `recomputeOutcome` so the result re-appears. `dismissUndo()` only clears the `UndoState` keys.
- [ ] T018 [US2] In `CompareScreen.kt`, add a `LaunchedEffect(state.undoState?.expiresAtEpochMillis)` that, when `undoState` becomes non-null, computes `remaining = max(0, expiresAtEpochMillis - System.currentTimeMillis())`, then calls `snackbarHostState.showSnackbar(message = R.string.comparison_cleared, actionLabel = R.string.undo_action, duration = SnackbarDuration.Long, withDismissAction = false)` inside a coroutine. Result of `showSnackbar` is checked: `SnackbarResult.ActionPerformed` → `viewModel.undoReset()`; `SnackbarResult.Dismissed` → `viewModel.dismissUndo()`. If `remaining` is shorter than the Material 3 Long default (~10 s), the effect uses `withTimeoutOrNull(remaining)` to truncate the wait so the post-rotation lifetime is honoured.
- [ ] T019 [US2] Replace the `// TODO(US2)` placeholder in `CompareViewModel.on*Change` (T012) with a real `dismissUndo()` call. Place it before mutating `*Raw` so spec FR-008.1 is satisfied even when `dismissUndo()` itself triggers downstream listeners.
- [ ] T020 [US2] Wire the host-activity-stop signal: in `MainActivity.kt`, observe `lifecycle` for `Lifecycle.Event.ON_STOP` and call `viewModel.dismissUndo()`. Use `LifecycleEventObserver` registered via `DisposableEffect(lifecycle)` from a `LifecycleEffect`-style helper to avoid leaks. (FR-008.3.)

**Checkpoint**: T013–T016 are green. The full reset-and-undo loop works. CI green. Constitution check re-verified — accessibility instrumented test still green, locale instrumented test still green, no new warnings from lint/detekt.

---

## Phase 4: Release prep & polish

**Purpose**: ship feature 002 to a tagged release.

- [ ] T021 Bump `versionCode` 4 → 5 and `versionName` "0.1.3" → "0.1.4" in `android/app/build.gradle.kts`.
- [ ] T022 [P] Add `fastlane/metadata/android/en-US/changelogs/5.txt` documenting the new Reset action and Undo affordance, in plain English under 500 chars.
- [ ] T023 [P] Add `fastlane/metadata/android/es-ES/changelogs/5.txt` mirroring T022 in Spanish under 500 chars.
- [ ] T024 Update `tasks.md` of feature 001 (the in-flight PR ledger) with a "PR L — `feat/016-reset-comparison`" entry summarising this feature.
- [ ] T025 Manual verification on a real device using the 9-step walkthrough from `plan.md`'s "Manual verification" section. Record cold-start regression (must stay below 2 s budget; expected delta is negligible because the new code path is < 200 LOC of UI Kotlin).
- [ ] T026 Tag `v0.1.4` from `main` after the implementation PR merges and CI is green. Verify the `Signed release APK` job publishes `app-release.apk` to the v0.1.4 GitHub Release.
- [ ] T027 Update `docs/fdroid.md` §3 build recipe and §5 chronology to point at v0.1.4: bump `Builds[0]` to `versionName: 0.1.4` / `versionCode: 5` / `commit: <full SHA of v0.1.4>`, and bump `CurrentVersion` / `CurrentVersionCode` accordingly. Coordinate with the GitLab MR edit in the same window.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Foundational (Phase 1)**: no dependencies on the rest of feature 002. Blocks US1 and US2.
- **User Story 1 (Phase 2)**: depends on Foundational. Independent of US2 in terms of deliverable, but US1 alone is **not shippable** because it leaves the user without recourse on accidental taps; do not tag a release between Phase 2 and Phase 3.
- **User Story 2 (Phase 3)**: depends on Foundational *and* US1 (US2 reuses US1's reset path).
- **Polish (Phase 4)**: depends on US1 + US2 being demo-able.

### Within a phase

- Tests marked `[P]` can be written in parallel before their implementation.
- All `[P]` implementation tasks can proceed in parallel (different files).
- Tasks without `[P]` must wait for the immediately preceding non-`[P]` task in the same phase.

### Parallel opportunities

Within Foundational, T002 / T003 / T005 are `[P]`. Within US1, T007–T009 (tests) and T010 (`resetComparison`) can be developed alongside T011 wiring. Within US2, T013–T016 (tests) and T017 (`undoReset` / `dismissUndo`) can be developed alongside T018–T020.

---

## Implementation Strategy (PRs)

To keep PRs small and reviewable, land feature 002 across three PRs,
mirroring feature 001's cadence:

### PR L — `002-reset-comparison` *(planning only — this PR)*

Covers `spec.md`, `plan.md`, `research.md`, and `tasks.md`. No source
code touched. This PR exists so Marco has a single, stable artifact to
review and sign off on before any Kotlin / XML lands. CI runs the
standard PR pipeline; the only relevant signal here is that lint /
detekt / unit tests still pass on a no-source-change PR (they should,
but the gate stays in force).

### PR M — `feat/016-reset-comparison-impl`

Covers Phases 1–3 and Phase 4 task T024 (the cross-feature ledger
update). Phase 4 T021–T023 (version bump + changelogs) ride along in
the same PR so the feature is releasable on merge. Branched off
`main` *after* PR L merges, so the planning artefacts are guaranteed
to be on `main` before implementation starts. The implementation diff
is bounded by the task list and is expected to land at ~600–900 LOC
including tests.

### PR N — `feat/017-prep-v0.1.4` *(release cut)*

Covers Phase 4 tasks T025–T027 (manual verification on device, tag
push, F-Droid `Builds:` sync). Mirrors the v0.1.3 cadence (PR J +
PR K from feature 001) so the F-Droid recipe never lags more than
one release behind `main`.

### Notes

- Each PR must keep the main branch green (lint, detekt, unit tests, JaCoCo coverage gate, instrumented tests).
- Commit after each task or logical group; keep the commit message in English, Conventional Commits prefix (`feat:`, `test:`, `chore:`, …).
- Stop at each phase checkpoint and validate before moving on.
- If a `[NEEDS CLARIFICATION]` surfaces while executing a task, STOP and raise it with Marco rather than inventing an answer.
