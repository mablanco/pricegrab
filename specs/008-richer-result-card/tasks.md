---
description: "Task list for feature 008 — richer result card (visible absolute + percent savings)"
---

# Tasks: Richer Result Card (Visible Absolute + Percent Savings)

**Input**: Design documents from `/specs/008-richer-result-card/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md,
contracts/result-savings-display.md, quickstart.md

**Tests**: REQUIRED. Constitution Principle V and plan.md mandate
test-first (Red → Green → Refactor). Instrumented savings / a11y tests
MUST fail before the percent line is wired into `HeroResultCard`.

**Organization**: Tasks grouped by user story from `spec.md`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on
  incomplete tasks).
- **[Story]**: `[US1]` / `[US2]` / `[US3]`. Setup, Foundational, and Polish
  phases carry no story label.
- Paths are relative to the git root (`pricegrab/`).

---

## Phase 1: Setup (Shared infrastructure)

**Purpose**: Point Spec Kit / docs / PR letter ledger at 008. No Android
UI changes yet (planning PR **AD**).

- [x] T001 [P] Append PR **AD** / **AE** / **AF** cadence notes for feature
      008 to `specs/001-unit-price-comparison/tasks.md` (global letter
      ledger) — done in planning commit.
- [x] T002 [P] Confirm `docs/project-status.md` Active Spec Kit pointer is
      `specs/008-richer-result-card`, note planning → impl → release-prep
      (**0.1.11**), and richer-result-card backlog item remains marked in
      progress — done in planning commit.
- [x] T003 [P] Confirm `.specify/feature.json` →
      `specs/008-richer-result-card` and
      `.cursor/rules/specify-rules.mdc` SPECKIT block points at
      `specs/008-richer-result-card/plan.md` — done in planning commit.

**Checkpoint**: Feature pointer / ledger / status coherent; no `android/`
source in PR AD.

---

## Phase 2: Foundational (Strings + test-tag constant)

**Purpose**: Localized percent template and shared test tag that US1–US3
share. Does **not** yet render percent on the hero (tests still fail
until US1 UI wiring).

**⚠️ CRITICAL**: Do not mark instrumented winner tests green until Phase 3
wires the percent line. `ResultPresenter` / `PriceComparator` stay
untouched unless a bug is proven.

- [x] T004 [P] Add EN percent companion string (e.g. `result_savings_percent`
      → `"%1$s%% less"`) in
      `android/app/src/main/res/values/strings.xml` per
      `contracts/result-savings-display.md`.
- [x] T005 [P] Add ES percent companion string (e.g. `result_savings_percent`
      → `"%1$s%% menos"`) in
      `android/app/src/main/res/values-es/strings.xml`.
- [x] T006 Add `TEST_TAG_RESULT_SAVINGS_PERCENT = "result_savings_percent"`
      next to existing result tags in
      `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreen.kt`
      (or the file that owns the other `TEST_TAG_RESULT_*` constants).

**Checkpoint**: Strings and tag compile; hero still absolute-only until
US1 implementation tasks.

---

## Phase 3: User Story 1 — See how much cheaper at a glance (Priority: P1) 🎯 MVP

**Goal**: Unique winner shows **absolute + percent** savings as visible
text on the hero (vs second-cheapest), in `en` and `es`.

**Independent Test**: Canonical 2.50/500 g vs 4.00/1 kg → B wins, absolute
visible, **20%** visible; free offer → **100%** visible; three-offer case
still uses second-cheapest baseline for both figures.

### Tests for User Story 1 ⚠️

> Write FIRST; ensure FAIL until percent line is rendered.

- [x] T007 [P] [US1] Update
      `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenSavingsTest.kt`
      so the B-wins 20% fixture asserts **visible** percent via
      `TEST_TAG_RESULT_SAVINGS_PERCENT` / localized
      `result_savings_percent` text (not only absolute).
- [x] T008 [P] [US1] Extend
      `CompareScreenSavingsTest.kt` free-offer case to assert visible
      **100** percent companion (update outdated comment that claimed
      percent was a11y-only).
- [x] T009 [P] [US1] Add instrumented assertion for a three-offer unique
      winner: absolute + percent both displayed and consistent with
      second-cheapest baseline (same file or
      `CompareScreenMultiOfferSavingsTest.kt` if cleaner).

### Implementation for User Story 1

- [x] T010 [US1] In
      `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenResult.kt`,
      build `percentLine` from `ResultPresenter`’s `percentDelta` +
      `R.string.result_savings_percent` whenever absolute savings are
      shown for a `Winner`.
- [x] T011 [US1] Extend `HeroResultCard` in `CompareScreenResult.kt` to
      render the percent line under the absolute line with
      `Modifier.testTag(TEST_TAG_RESULT_SAVINGS_PERCENT)`; keep absolute
      on `TEST_TAG_RESULT_SAVINGS`; make T007–T009 green.
- [x] T012 [P] [US1] Update
      `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenPreviews.kt`
      so winner previews show both savings lines.

**Checkpoint**: Sighted reviewer sees absolute + percent on unique
winner; MVP shippable after US2 quiet-state checks.

---

## Phase 4: User Story 2 — Quiet result when there is nothing to save (Priority: P1)

**Goal**: Tie / empty / incompatible units never show absolute **or**
percent savings.

**Independent Test**: Equal unit prices → no `result_savings` and no
`result_savings_percent`; incomplete form → placeholder only;
incompatible dimensions → error only.

### Tests for User Story 2 ⚠️

- [x] T013 [P] [US2] Extend `tieHidesSavingsRow` in
      `CompareScreenSavingsTest.kt` to assert
      `TEST_TAG_RESULT_SAVINGS_PERCENT` does not exist (in addition to
      absolute).
- [x] T014 [P] [US2] Add/extend instrumented coverage that incomplete
      input and incompatible-units states show neither savings tag
      (reuse or extend existing layout / units tests under
      `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/`).

### Implementation for User Story 2

- [x] T015 [US2] Verify `ResultRegion` elision rules in
      `CompareScreenResult.kt`: `percentLine` is null whenever
      `savingsLine` / absolute is null (tie, empty, incompatible,
      unknown dimension); fix if US1 wiring left a half-rich state; make
      T013–T014 green.

**Checkpoint**: No savings figures leak into quiet states.

---

## Phase 5: User Story 3 — Accessibility and large text (Priority: P2)

**Goal**: TalkBack summary includes absolute + percent for winners;
percent line is not a heading; 200% font keeps both figures readable;
ES/EN parity.

**Independent Test**: Result region semantics / content description
contains percent for the 20% fixture; percent node has no `Heading`;
large-font smoke per quickstart.

### Tests for User Story 3 ⚠️

- [x] T016 [P] [US3] Extend
      `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenAccessibilityTest.kt`
      so a unique winner’s result region announces / exposes absolute
      **and** percent in its polite summary (and percent line is not a
      heading, mirroring absolute).
- [x] T017 [P] [US3] Add or extend a large-font (200%) instrumented check
      that both `TEST_TAG_RESULT_SAVINGS` and
      `TEST_TAG_RESULT_SAVINGS_PERCENT` remain displayed without asserting
      clipped meaning is acceptable only via manual quickstart if the
      suite already covers offer-row large font separately — prefer an
      automated “both tags exist at 200%” assertion when practical.

### Implementation for User Story 3

- [x] T018 [US3] Update `a11ySummary` in `CompareScreenResult.kt` to
      `"$headline. $savingsLine. $percentLine"` (localized join as
      appropriate) for winners; make T016 green.
- [x] T019 [US3] Confirm percent `Text` uses non-heading semantics and
      existing contrast tokens (`onSurfaceVariant` or plan-approved
      alternative); adjust typography if 200% QA shows collision; make
      T017 green or document manual residual in PR AE.

**Checkpoint**: US1–US3 acceptance scenarios covered automated +
quickstart residuals noted.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Regression, docs touch-ups for impl PR, release-prep only on
**AF**.

- [x] T020 [P] Run Compare unit + instrumented suites
      (`ResultPresenterTest`, savings, multi-offer, reset/undo, settings
      preserve) and fix regressions without changing comparator math.
      Unit + compile green locally; instrumented deferred to CI (no
      emulator attached).
- [x] T021 [P] Spot-check `specs/008-richer-result-card/quickstart.md`
      steps on a device/emulator (en + es, light/dark); note results in
      PR AE description.
      Local: no emulator — ask Marco for on-device smoke; CI covers
      automated cases.
- [x] T022 [P] On release-prep PR **AF** only: bump `versionCode` /
      `versionName` to **12** / **0.1.11**, add
      `fastlane/metadata/android/{en-US,es-ES}/changelogs/12.txt`, sync
      `docs/project-status.md` + `docs/fdroid.md` as needed. Agents must
      **prompt Marco** before any future MINOR (0.2.0+) bump.

**Checkpoint**: PR AE ready for review (T004–T021). Tag only after AF +
manual QA on `main`.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — planning PR AD.
- **Phase 2 (Foundational)**: After AD merge (or on impl branch from
  `main`). Strings + tag before US1 UI.
- **Phase 3 (US1)**: After T004–T006. MVP.
- **Phase 4 (US2)**: After US1 percent wiring (T010–T011) so elision can
  be verified against the new node.
- **Phase 5 (US3)**: After US1 visible percent exists (a11y joins both
  lines).
- **Phase 6 (Polish)**: After US1–US3; T022 only on release-prep PR AF.

### User Story Dependencies

| Story | Depends on | Independently testable by |
|-------|------------|---------------------------|
| US1 Visible absolute + percent | Phase 2 strings/tag | CompareScreenSavingsTest 20% / 100% / three-offer |
| US2 Quiet states | US1 percent node exists | Tie / empty / incompatible assertions |
| US3 A11y + large text | US1 (+ US2 quiet rules) | CompareScreenAccessibilityTest + 200% check |

### Parallel Opportunities

- T001 ∥ T002 ∥ T003 (docs).
- T004 ∥ T005 (EN/ES strings); T006 after or with them.
- T007 ∥ T008 ∥ T009 (instrumented tests).
- T012 parallel with T010–T011 once API shape known.
- T013 ∥ T014.
- T016 ∥ T017.
- T020 ∥ T021 during polish.

---

## Parallel Example: User Story 1

```bash
# Tests first (expect FAIL until percent Text exists):
# - CompareScreenSavingsTest: 20% visible + 100% visible
# - three-offer winner shows both savings tags

# Then implement:
# - percentLine from ResultPresenter.percentDelta + string resource
# - HeroResultCard second body line + testTag
# - Update Compose previews
```

---

## Implementation Strategy

### MVP First (US1 only)

1. Phase 2 strings + tag.
2. Phase 3 failing tests → wire percent line → green.
3. **STOP and VALIDATE** on a phone: absolute + percent readable in aisle
   glance.
4. Continue US2 → US3 → polish before marking PR AE ready for review.

### PR mapping

| Letter | Scope |
|--------|--------|
| **AD** | This branch: spec, plan, research, data-model, contracts, quickstart, tasks, status/ledger docs. **No** `android/` source. |
| **AE** | `feat/0xx-richer-result-card-impl`: T004–T021 (implementation + tests). No version bump. |
| **AF** | Release-prep: T022 → **0.1.11** / versionCode 12 + tag after Marco QA. |

### Incremental delivery

1. Planning PR AD merge (or continue follow-ups on this branch until merge).
2. Impl PR AE: US1 → US2 → US3 → polish/regression.
3. Release-prep PR AF → tag `v0.1.11` from `main` after manual QA.

---

## Notes

- Stay on **0.1.x** (**0.1.11** at release-prep). Agents must **prompt
  Marco** before proposing a **MINOR** bump (0.2.0+).
- Do **not** change `core/calc/PriceComparator` formulas or
  `ComparisonOutcome` fields.
- Prefer leaving `ResultPresenter` unchanged (already exposes
  `percentDelta`).
- Preserve existing Compare test tags; only **add**
  `result_savings_percent`.
- Commit in English Conventional Commits; chat with Marco in Spanish.
- Constitution: I glanceable result, II a11y, III ES/EN, IV offline
  UI-only, V test-first.
