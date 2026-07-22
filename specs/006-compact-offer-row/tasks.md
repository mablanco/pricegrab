---
description: "Task list for feature 006 — compact offer input row"
---

# Tasks: Compact Offer Input Row

**Input**: Design documents from `/specs/006-compact-offer-row/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md,
contracts/offer-row-layout.md, quickstart.md

**Tests**: REQUIRED. Constitution Principle V and plan.md mandate
test-first (Red → Green → Refactor). Geometry / arrangement tests MUST
fail before the `OfferCard` refactor lands.

**Organization**: Tasks grouped by user story from `spec.md`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on
  incomplete tasks).
- **[Story]**: `[US1]` / `[US2]` / `[US3]`. Setup, Foundational, and Polish
  phases carry no story label.
- Paths are relative to the git root (`pricegrab/`).

---

## Phase 1: Setup (Planning hygiene)

**Purpose**: Keep global ledger and status aligned. No Android source yet
on the planning PR (**X**).

- [x] T001 [P] Append PR **X** / **Y** / **Z** cadence notes for feature 006
      to `specs/001-unit-price-comparison/tasks.md` (global letter ledger).
- [x] T002 [P] Confirm `docs/project-status.md` Active Spec Kit pointer is
      `specs/006-compact-offer-row` and In progress lists 006 (already drafted
      in planning; fix if drift).

**Checkpoint**: Planning docs coherent; implementation branch can start after
PR X merges (or continue on a follow-up `feat/…-impl` branch from `main`).

---

## Phase 2: Foundational (Arrangement helper)

**Purpose**: Pure arrangement mapping used by every story. Blocks UI work.

**⚠️ CRITICAL**: No `OfferCard` layout change until T004’s unit test is green
for the mapping (UI stories still write failing Compose tests first).

- [x] T003 [P] Add JVM unit tests for `arrangementFor(fontScale)` covering
      `< 1.3f → CompactSingleRow` and `≥ 1.3f → AdaptiveTwoRow` in
      `android/app/src/test/kotlin/com/mablanco/pricegrab/ui/compare/OfferInputArrangementTest.kt`
      (fail until T004 exists).
- [x] T004 Implement `OfferInputArrangement` enum + `arrangementFor(fontScale: Float)`
      in `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/OfferInputArrangement.kt`
      per `data-model.md` / `research.md`; make T003 green.

**Checkpoint**: Arrangement rule is unit-tested and ready for Compose wiring.

---

## Phase 3: User Story 1 — Compact single row (Priority: P1) 🎯 MVP

**Goal**: At default font scale, each offer shows price | quantity | unit on
one horizontal row with compact weighted fields.

**Independent Test**: Cold launch at default scale → Offer A/B fields share
one row → enter short values → comparison still correct; Offer C same layout.

### Tests for User Story 1 ⚠️

> Write FIRST; ensure FAIL against today’s stacked layout.

- [x] T005 [P] [US1] Add Compose instrumented test asserting CompactSingleRow
      geometry at `fontScale = 1f` for Offer A (and B): shared vertical
      centers within tolerance; left(price) < left(quantity) < left(unit) —
      per `contracts/offer-row-layout.md` — in
      `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenCompactRowTest.kt`.
- [x] T006 [P] [US1] Extend or add assertion that Offer C (after add) uses the
      same single-row geometry in
      `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenCompactRowTest.kt`
      (or `CompareScreenMultiOfferTest.kt` if cleaner).

### Implementation for User Story 1

- [x] T007 [US1] Refactor `OfferCard` in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreen.kt`
      to render `CompactSingleRow`: one `Row` with price `weight(1.15f)`,
      quantity `weight(1f)`, unit fixed width; `Alignment.Top`; keep existing
      test tags `{prefix}_price|_quantity|_unit`.
- [x] T008 [US1] Wire `arrangementFor(LocalDensity.current.fontScale)` so
      CompactSingleRow is selected at default scale (AdaptiveTwoRow stub may
      temporarily reuse today’s two-row Column for non-compact until US2).
- [x] T009 [US1] Verify IME order Next (price) → Done (quantity) and decimal
      keyboard unchanged in `CompareScreen.kt` / `LabeledNumberField`.
- [x] T010 [US1] Run
      `./gradlew :app:connectedDebugAndroidTest --tests '*CompareScreenCompactRowTest*'`
      (and unit test T003) until green.

**Checkpoint**: US1 MVP — compact row at default scale; existing happy-path
compare still works.

---

## Phase 4: User Story 2 — Adaptive layout at large font (Priority: P1)

**Goal**: At large font scale (incl. 200%), offer inputs use AdaptiveTwoRow
without truncation, overlap, or missing controls.

**Independent Test**: `fontScale = 2f` (instrumented) or system 200% → two-row
arrangement → complete a comparison → no clipped controls.

### Tests for User Story 2 ⚠️

- [x] T011 [P] [US2] Extend
      `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenLargeFontTest.kt`
      with AdaptiveTwoRow geometry:
      `bottom(price) ≤ top(quantity) + slack`; quantity left of unit; all
      tags still displayed (scroll allowed) — per contract.

### Implementation for User Story 2

- [x] T012 [US2] Implement AdaptiveTwoRow branch in `OfferCard`
      (`CompareScreen.kt`): full-width price; then quantity | unit row
      (same controls/tags as today).
- [x] T013 [US2] Confirm switching arrangement on recomposition preserves
      entered values (no state held in arrangement enum); rely on existing
      config-change coverage + spot-check.
- [x] T014 [US2] Ensure touch targets remain ≥ 48dp for unit selector /
      fields in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenUnits.kt`
      (adjust padding/width only if large-font test or manual QA fails).
- [x] T015 [US2] Run large-font + compact-row instrumented tests green.

**Checkpoint**: Default = compact; ≥1.3 fontScale = adaptive two-row.

---

## Phase 5: User Story 3 — Errors & TalkBack (Priority: P2)

**Goal**: Per-field errors stay actionable; TalkBack labels remain clear in
both arrangements.

**Independent Test**: Invalid price/quantity shows supporting text; TalkBack
names offer on price/quantity/unit in compact and adaptive modes.

### Tests for User Story 3 ⚠️

- [x] T016 [P] [US3] Extend
      `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenAccessibilityTest.kt`
      if needed so CD assertions still pass with compact row (Offer A
      price/quantity/unit).
- [x] T017 [P] [US3] Add or extend a focused test that an invalid quantity
      shows error supporting text without hiding sibling controls (default
      scale compact row) — prefer
      `CompareScreenCompactRowTest.kt` or existing validation UI test file.

### Implementation for User Story 3

- [x] T018 [US3] Keep per-field `isError` + `supportingText` on
      `LabeledNumberField` in `CompareScreen.kt`; fix clipping/overlap if
      compact-row error layout fails T017.
- [x] T019 [US3] Confirm semantics/`contentDescription` wiring unchanged for
      price, quantity (+ unit name), and unit selector; fix only if T016 fails.
- [ ] T020 [US3] Manual TalkBack smoke (both arrangements) per
      `specs/006-compact-offer-row/quickstart.md`; note result in PR Y
      description.

**Checkpoint**: A11y and errors meet FR-006/FR-007 in both layouts.

---

## Phase 6: Polish & Cross-Cutting

**Purpose**: Full regression and docs; release bump stays on PR **Z**.

- [x] T021 [P] Run full
      `./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest`
      and fix any regressions in existing compare suites (tags must stay
      stable).
- [x] T022 [P] Detekt / assembleDebug clean for touched files.
- [ ] T023 Walk `specs/006-compact-offer-row/quickstart.md` on a device or
      emulator (default + 200% font; 2 and 3 offers).
- [x] T024 [P] Update `@Preview`s in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenPreviews.kt`
      if the compact row is not visible in previews.
- [x] T025 Release-prep (PR **Z**, after Y merges): bump versionName/versionCode,
      fastlane changelogs en/es, refresh `docs/project-status.md` +
      `docs/fdroid.md` as needed — **do not** do this on the impl PR.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: Planning-only; no code dependency.
- **Phase 2 (Foundational)**: T003 → T004; blocks US1–US3 implementation
  (tests for stories may be written in parallel once T004 API shape is known).
- **Phase 3 (US1)**: T005/T006 fail → T007–T009 → T010 green. MVP.
- **Phase 4 (US2)**: Depends on US1 `OfferCard` structure; T011 fail →
  T012–T014 → T015.
- **Phase 5 (US3)**: Depends on both arrangements existing; polish errors/a11y.
- **Phase 6 (Polish)**: After US1–US3; T025 only on release-prep PR.

### User Story Dependencies

| Story | Depends on | Independently testable by |
|-------|------------|---------------------------|
| US1 Compact row | Phase 2 | Default-scale geometry + compare happy path |
| US2 Adaptive 200% | US1 OfferCard switch point | LargeFontTest geometry + compare |
| US3 Errors / TalkBack | US1 (+ US2 for both modes) | A11y + invalid-input UI tests |

### Parallel Opportunities

- T001 ∥ T002 (docs).
- T005 ∥ T006 (instrumented tests, same or split files carefully).
- T011 can be sketched while US1 impl is in progress (will fail until T012).
- T016 ∥ T017 after layouts exist.
- T021 ∥ T022 ∥ T024 during polish.

---

## Parallel Example: User Story 1

```bash
# Tests first (expect FAIL on current main UI):
# - CompareScreenCompactRowTest: single-row geometry @ fontScale 1
# - Offer C same-row assertion

# Then implement:
# - OfferInputArrangement.kt (if not done in Phase 2)
# - OfferCard CompactSingleRow in CompareScreen.kt
```

---

## Implementation Strategy

### MVP First (US1 only)

1. Phase 2 helper + T003/T004 green.
2. Phase 3 compact row + T005/T006 → T010 green.
3. **STOP and VALIDATE** on a phone: two offers, one-row fields, result OK.
4. Continue US2/US3 before opening PR Y as “ready for review”.

### PR mapping

| Letter | Scope |
|--------|--------|
| **X** | This branch: spec, plan, research, data-model, contracts, quickstart, tasks, status/ledger docs. **No** `android/` source. |
| **Y** | `feat/0xx-compact-offer-row-impl`: T003–T024 (implementation + tests). |
| **Z** | Release-prep: T025 + tag after Marco QA. |

### Incremental delivery

1. Planning PR X merge.
2. Impl PR Y: US1 → US2 → US3 → polish/regression.
3. Release-prep PR Z → tag from `main` after manual QA.

---

## Notes

- Do not rename `{offerA|B|C}_price|_quantity|_unit` test tags.
- Do not change `core/` comparison math.
- Threshold `1.3f` is the research decision; change only with spec/research update.
- Commit in English Conventional Commits; chat with Marco in Spanish.
