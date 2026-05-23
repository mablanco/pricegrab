---
description: "Task list for feature 004 — quantity units for offer comparison"
---

# Tasks: Quantity Units for Offer Comparison

**Input**: Design documents from `/specs/004-quantity-units/`
**Prerequisites**: spec.md (required), plan.md (required), research.md
(required), data-model.md, contracts/quantity-units.md

**Tests**: INCLUDED. The spec (FR-001..FR-009, SC-001..SC-005) and the
constitution (Principle V, NON-NEGOTIABLE) require tests. Every story below
lists test tasks **before** implementation tasks (TDD).

**Organization**: tasks grouped by user story from `spec.md`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on
  incomplete tasks).
- **[Story]**: `[US1]`, `[US2]`, or `[US3]`. Foundational and release
  phases carry no story label.
- File paths are absolute within the repository (under `android/`).

---

## Phase 1: Foundational (domain + parser + gate)

**Purpose**: Pure-Kotlin unit model, normalized `Offer.unitPrice`, extended
parser, dimension gate, and display scaling in `ResultPresenter`. No Compose
UI yet — both US1 and US2 depend on this phase.

**⚠️ CRITICAL**: no user-story UI work begins until this phase passes.

### Tests (write first, watch them fail)

- [ ] T001 [P] Write `android/app/src/test/kotlin/com/mablanco/pricegrab/core/model/QuantityUnitTest.kt` asserting every `QuantityUnit` maps to the correct `Dimension`, `toBaseMultiplier` matches [`data-model.md`](./data-model.md) (1 or 1000), and `QuantityUnit.Gram` is the documented default.
- [ ] T002 [P] Extend `android/app/src/test/kotlin/com/mablanco/pricegrab/core/format/OfferParserTest.kt` with cases that pass `quantityUnit` (`Kilogram`, `Millilitre`, `Litre`, `Piece`) and assert `Success.offer.quantityUnit` round-trips.
- [ ] T003 [P] Extend `android/app/src/test/kotlin/com/mablanco/pricegrab/core/calc/PriceComparatorTest.kt`: (a) re-run feature 001 cases #1–#11 with explicit `QuantityUnit.Gram`; (b) add contract case U1 (`2.50/500/g` vs `4.00/1/kg` → `BWins`, base Δ = 0.001); (c) add U4 tie and U6 free-offer with units.
- [ ] T004 [P] Write `android/app/src/test/kotlin/com/mablanco/pricegrab/ui/compare/CompareViewModelUnitsTest.kt` (JVM) covering `ComparisonGate.IncompatibleUnits` when units are g vs ml, `Ready` when kg vs g, and `Incomplete` when fields blank.
- [ ] T005 [P] Extend `android/app/src/test/kotlin/com/mablanco/pricegrab/ui/compare/ResultPresenterTest.kt` with dimension-specific display scaling: mass Δ 0.001 → formatted `"1"` (or locale equivalent) for per-kg template; volume and count cases from contract U2/U3.

### Implementation

- [ ] T006 [P] Create `android/app/src/main/kotlin/com/mablanco/pricegrab/core/model/Dimension.kt` with `displayUnitScale: BigDecimal` per [`data-model.md`](./data-model.md).
- [ ] T007 [P] Create `android/app/src/main/kotlin/com/mablanco/pricegrab/core/model/QuantityUnit.kt` enum with `dimension`, `toBaseMultiplier`, and `companion object { val DEFAULT = Gram }`.
- [ ] T008 Modify `android/app/src/main/kotlin/com/mablanco/pricegrab/core/model/Offer.kt`: add `quantityUnit: QuantityUnit`, `quantityInBaseUnits` derived property, change `unitPrice` to divide by `quantityInBaseUnits`.
- [ ] T009 Modify `android/app/src/main/kotlin/com/mablanco/pricegrab/core/format/OfferParser.kt` to accept `quantityUnit: QuantityUnit` and construct `Offer(..., quantityUnit)`.
- [ ] T010 Add dimension precondition guard to `android/app/src/main/kotlin/com/mablanco/pricegrab/core/calc/PriceComparator.kt` (throw `IllegalArgumentException` when dimensions differ) per [`contracts/quantity-units.md`](./contracts/quantity-units.md).
- [ ] T011 Modify `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/ResultPresenter.kt`: add `dimension: Dimension?` parameter; scale `perUnitDelta` for display; return `null` when dimension unknown.
- [ ] T012 Extend `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareUiState.kt` with `quantityUnitA`, `quantityUnitB` (default `Gram`), `incompatibleUnits: Boolean`, and extend `PreResetSnapshot` with both units. Update `isResetEnabled` to treat non-default units as non-empty.
- [ ] T013 Modify `android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareViewModel.kt`: SavedStateHandle keys for units; `onQuantityUnitAChange` / `onQuantityUnitBChange`; dimension gate in `recomputeOutcome`; pass units into `OfferParser.parse`; reset clears units to `Gram`; undo snapshot/restores units.
- [ ] T014 Run `./gradlew :app:testDebugUnitTest :app:jacocoTestReport` — T001–T005 green; JaCoCo gate on `core/calc/**` still ≥ 90%.

**Checkpoint**: Domain + ViewModel gate work without UI. All JVM tests green.

---

## Phase 2: User Story 1 — Label each quantity with a real unit (P1) 🎯 MVP

**Goal**: Unit dropdown beside each quantity field; default **g**; canonical
500 g vs 1 kg scenario shows Offer B winning with **per kg** savings; volume
and count savings strings use **per L** and **per piece**.

**Independent test**: spec US1 acceptance scenario 2 (500 g @ €2.50 vs
1 kg @ €4.00) in both locales.

### Tests for US1 (write first, watch them fail)

- [ ] T015 [P] [US1] Write `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenUnitsTest.kt`: (a) cold launch — both dropdowns show `g`; (b) enter canonical mass scenario — hero card headline + savings contain "per kg" / "por kg"; (c) ml/L and pcs scenarios assert per L / per piece strings.
- [ ] T016 [P] [US1] Extend `CompareScreenUnitsTest.kt` or add focused test: changing unit selector with valid numbers recomputes result without extra action (live update).

### Implementation for US1

- [ ] T017 [P] [US1] Add English strings to `android/app/src/main/res/values/strings.xml`: `unit_code_*` (closed dropdown), `unit_name_*` (menu items), `result_savings_per_kg`, `result_savings_per_L`, `result_savings_per_piece`, `cd_quantity_unit`. Deprecate/remove `result_savings` references in CompareScreen once migrated.
- [ ] T018 [P] [US1] Mirror T017 in `android/app/src/main/res/values-es/strings.xml` (e.g. gramos, kilogramos, mililitros, litros, piezas; "por kg", "por L", "por pieza").
- [ ] T019 [US1] Add private `QuantityUnitSelector` composable in `CompareScreen.kt` using `ExposedDropdownMenuBox`; place in `OfferCard` row beside quantity field (`Row` + `weight(1f)` on text field).
- [ ] T020 [US1] Wire selectors to `CompareViewModel.onQuantityUnitAChange` / `onQuantityUnitBChange`; pass `quantityUnitA/B` from state into `OfferCard`.
- [ ] T021 [US1] Update `ResultRegion` / `HeroResultCard` to pick savings template from `Dimension` via `ResultPresenter.present(outcome, dimension, locale)`; remove generic "per unit" body line.

**Checkpoint**: US1 demo-able. T015–T016 green. CI unit tests green.

---

## Phase 3: User Story 2 + User Story 3 — Safety, a11y, i18n (P1 + P2)

**Goal**: Block g vs ml with localized error; TalkBack announces unit on
quantity fields and shelf unit in savings; 200% font scale still fits.

### Tests for US2 / US3 (write first, watch them fail)

- [ ] T022 [P] [US2] Write `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenIncompatibleUnitsTest.kt`: g vs ml → no winner, `error_incompatible_units` visible; change B to kg → error clears, winner shown.
- [ ] T023 [P] [US3] Extend `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenAccessibilityTest.kt`: quantity field merged semantics include unit name; savings live region includes "per kilogram" (or es equivalent) for mass result.
- [ ] T024 [P] [US3] Extend `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenLargeFontTest.kt`: assert unit dropdown + quantity row fit at 200% in en-US and es-ES on 411 dp width.

### Tests / updates for Reset integration (feature 002)

- [ ] T025 [P] Extend `android/app/src/test/kotlin/com/mablanco/pricegrab/ui/compare/CompareViewModelResetTest.kt`: snapshot captures units; reset sets `Gram`; undo restores units.
- [ ] T026 [P] Extend `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenResetTest.kt`: reset clears dropdowns to `g`; undo restores prior unit selections.

### Implementation for US2 / US3

- [ ] T027 [P] [US2] Add `error_incompatible_units` to `values/strings.xml` and `values-es/strings.xml` per spec (weight / volume / count explanation).
- [ ] T028 [US2] Render incompatible-units message in `ResultRegion` when `state.incompatibleUnits` (no hero card); keep polite live region semantics with error text only.
- [ ] T029 [US3] Update quantity `contentDescription` / semantics to append localized unit name from selected `QuantityUnit` (not just numeric value).
- [ ] T030 [US3] Verify dropdown menu items use localized `unit_name_*` strings; closed selector shows short `unit_code_*`.

**Checkpoint**: US1 + US2 + US3 complete. `./gradlew :app:connectedDebugAndroidTest` green on CI.

---

## Phase 4: Release prep

**Purpose**: Version bump, changelogs, icon padding, master ledger, manual
verification gate before tag. Doc-only F-Droid sync lands in PR T.

- [x] T031 [P] Bump `android/app/build.gradle.kts`: `versionCode = 8`, `versionName = "0.1.7"`.
- [x] T032 [P] Create `fastlane/metadata/android/en-US/changelogs/8.txt` — quantity units, cross-dimension guard, per kg/L/piece savings, icon inset tweak.
- [x] T033 [P] Mirror T032 in `fastlane/metadata/android/es-ES/changelogs/8.txt`.
- [x] T034 [P] Set `ART_SCALE = 0.82` in `branding/regenerate-icons.py`; run `python3 branding/regenerate-icons.py`; commit regenerated `mipmap-*` and `fastlane/metadata/android/{en-US,es-ES}/images/icon.png`.
- [x] T035 Update master PR ledger in `specs/001-unit-price-comparison/tasks.md` with PR R / S / T entries (if not already done in planning PR O).
- [ ] T036 Manual verification: 11-step walkthrough from [`plan.md`](./plan.md) § Manual verification on signed v0.1.7 APK.
- [ ] T037 Tag `v0.1.7` from `main` after PR S merges and T036 passes; confirm GitHub Release APK attached.
- [x] T038 Update `docs/fdroid.md` §3 / §5 for v0.1.7 (PR T — `chore/021-fdroid-doc-sync-v0.1.7`).

**Checkpoint**: v0.1.7 tagged; F-Droid playbook synced in PR T.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1** blocks all UI stories.
- **Phase 2 (US1)** depends on Phase 1.
- **Phase 3 (US2 + US3 + reset)** depends on Phase 2 (shared CompareScreen).
- **Phase 4** depends on Phases 1–3.

### Parallel opportunities

Within Phase 1, T001–T005 (tests) parallel; T006–T007 parallel before T008.
Within Phase 2, T015–T016 parallel; T017–T018 parallel before T019.

---

## PR strategy

Same three-PR cadence as features 002–003. Global ledger letters **R / S / T**.

### PR R — `004-quantity-units` *(planning only — PR #29)*

Ships `specs/004-quantity-units/` (spec, plan, research, data-model,
contracts, tasks) and `.specify/feature.json` pointer. **No source code.**
Marco reviews FR-001..FR-009 and SC-001..SC-005 before implementation.

### PR S — `feat/020-quantity-units-impl` *(implementation — PR #30, merged)*

Phases 1–3 (T001–T030). Release-prep T031–T034 deferred to PR T so the
implementation PR stays reviewable without icon-byte churn. Merged to
`main` at `4d94807`.

### PR T — `chore/021-fdroid-doc-sync-v0.1.7` *(release prep + doc sync)*

T031–T034 + T038: version bump to 0.1.7 / `versionCode` 8, en/es
changelogs, `ART_SCALE` 0.82 icon regen, and `docs/fdroid.md` §3 / §5
sync. Tag push (T037) happens outside this PR; same cadence as PR Q.

### Notes

- Each PR keeps `main` green (lint, detekt, unit tests, JaCoCo, instrumented).
- T036 manual verification gates T037 tag push, not the planning PR.
- Do not ship incompatible-units guard without unit selectors (US1 + US2
  same release per spec P1 co-priority).
