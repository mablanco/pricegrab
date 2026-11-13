---
description: "Task list for feature 009 — length units (cm, m)"
---

# Tasks: Length Units (cm, m)

**Input**: Design documents from `/specs/009-length-units/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md,
contracts/length-units.md, quickstart.md

**Tests**: REQUIRED. Constitution Principle V and plan.md mandate
test-first (Red → Green → Refactor). New JVM and instrumented tests MUST
fail (or not compile) before the enums and strings are added.

**Organization**: Tasks grouped by user story from `spec.md`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependencies on
  incomplete tasks).
- **[Story]**: `[US1]` / `[US2]` / `[US3]`. Setup, Foundational, and Polish
  phases carry no story label.
- Paths are relative to the git root (`pricegrab/`). Kotlin paths are
  abbreviated: `main/…` = `android/app/src/main/kotlin/com/mablanco/pricegrab/…`,
  `test/…` = `android/app/src/test/kotlin/com/mablanco/pricegrab/…`,
  `androidTest/…` = `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/…`,
  `res/…` = `android/app/src/main/res/…`.

---

## Phase 1: Setup (Shared infrastructure)

**Purpose**: Point Spec Kit / docs / PR letter ledger at 009 (planning PR
**AI**). No Android source changes.

- [x] T001 [P] Append PR **AI** cadence entry for feature 009 to
      `specs/001-unit-price-comparison/tasks.md` — done in planning commit.
- [x] T002 [P] Point `docs/project-status.md` Active Spec Kit feature at
      `specs/009-length-units` and add the 009 row (AI / AJ / AM → v0.1.14)
      to the cadence table — done in planning commit.
- [x] T003 [P] Point `.specify/feature.json` and the SPECKIT block in
      `.cursor/rules/specify-rules.mdc` at `specs/009-length-units` — done
      in planning commit.

**Checkpoint**: Pointer / ledger / status coherent; no `android/` source in
PR AI.

---

## Phase 2: Foundational (Model)

**Purpose**: The enum values every story depends on. Exhaustive `when`
expressions will stop compiling until Phase 3 adds UI branches, so T004 –
T007 land together in one commit with the US1 branches.

**⚠️ CRITICAL**: `PriceComparator`, `ComparisonGate`, `OfferParser`,
`ResultPresenter`, `CompareViewModel` and `CompareViewModelKeys` stay
untouched (research §1). If a change there seems necessary, stop and
re-check the plan.

- [x] T004 [P] Write failing JVM tests in `test/core/model/QuantityUnitTest.kt`:
      `Centimetre` / `Metre` map to `Dimension.Length` with multipliers 1 /
      100; `QuantityUnit.entries` names equal
      `[Gram, Kilogram, Millilitre, Litre, Centimetre, Metre, Piece]`;
      `DEFAULT == Gram`; `Dimension.Length.displayUnitScale == 100`.
- [x] T005 [P] Write failing JVM tests in `test/core/model/OfferTest.kt`:
      `22 / 250 cm` and `22 / 2.5 m` have equal `quantityInBaseUnits`
      (250) and equal `unitPrice` (0.088).
- [x] T006 Add `Length(BigDecimal("100"))` after `Volume` in
      `main/core/model/Dimension.kt`.
- [x] T007 Add `Centimetre, Metre` between `Litre` and `Piece` in
      `main/core/model/QuantityUnit.kt`; extend `dimension`
      (`Centimetre, Metre -> Dimension.Length`) and `toBaseMultiplier`
      (`Centimetre` → 1, `Metre` → 100).

**Checkpoint**: T004 / T005 green once Phase 3 branches make the module
compile.

---

## Phase 3: User Story 1 — Compare offers sold by length (Priority: P1) 🎯 MVP

**Goal**: cm / m selectable; length comparisons show savings per m.

**Independent Test**: Offer A 49 / 5 m, Offer B 22 / 250 cm → Offer B
wins, "Save 1 per m vs Offer A", "10.2% less" (quickstart §Issue #56).

### Tests for User Story 1 (write first, must fail)

- [x] T008 [P] [US1] Add comparator cases in
      `test/core/calc/PriceComparatorTest.kt`: issue #56 canonical (B wins,
      `perUnitDelta` 0.01 per cm, `percentDelta` ≈ 10.2); tie 5 m @ 49 vs
      500 cm @ 49; three offers 20/200 cm, 27/300 cm, 12/1 m → B wins vs A.
- [x] T009 [P] [US1] Add presenter case in
      `test/ui/compare/ResultPresenterTest.kt`: `Dimension.Length`,
      `perUnitDelta = 0.01` → display `"1"`, percent `"10.2"` (en-US).
- [x] T010 [P] [US1] Add ViewModel cases in
      `test/ui/compare/CompareViewModelUnitsTest.kt`: cm vs m produces an
      outcome; Reset returns `Metre` slots to `Gram`; Undo restores
      `Metre` / `Centimetre`.
- [x] T011 [P] [US1] Add instrumented cases in
      `androidTest/ui/compare/CompareScreenUnitsTest.kt`: selector lists
      g, kg, ml, L, cm, m, pcs in order and defaults to g; issue #56
      scenario shows `Save 1 per m vs Offer A` (en-US) and the es-ES
      equivalent if the suite already runs es-ES.
- [x] T012 [P] [US1] Add a rotation / process-death case for `Metre` in
      `androidTest/ui/compare/CompareScreenConfigChangeTest.kt` (or
      `test/ui/compare/CompareViewModelSavedStateHardeningTest.kt` for
      saved-state restore at JVM level).

### Implementation for User Story 1

- [x] T013 [P] [US1] Add EN strings in `res/values/strings.xml`:
      `unit_code_cm` = `cm`, `unit_code_m` = `m`,
      `unit_name_centimetre` = `centimetres`, `unit_name_metre` = `metres`,
      `result_savings_per_m` = `Save %1$s per m vs %2$s`.
- [x] T014 [P] [US1] Add ES strings in `res/values-es/strings.xml`:
      `cm`, `m`, `centímetros`, `metros`,
      `Ahorras %1$s por m frente a %2$s`.
- [x] T015 [US1] Add `Centimetre` / `Metre` branches to `codeRes()` and
      `nameRes()` in `main/ui/compare/CompareScreenUnits.kt`.
- [x] T016 [US1] Map `Dimension.Length -> R.string.result_savings_per_m` in
      `formatSavingsLine` in `main/ui/compare/CompareScreenResult.kt`.
- [x] T017 [US1] Run `./gradlew :app:testDebugUnitTest` (from `android/`):
      T004, T005, T008–T010, T012 green; then the instrumented T011.

**Checkpoint**: US1 independently testable — the issue #56 scenario works
end to end.

---

## Phase 4: User Story 2 — Block length vs other measures (Priority: P1)

**Goal**: Any length unit mixed with g, kg, ml, L or pcs shows the updated
incompatible message and no winner.

**Independent Test**: Offer A m, Offer B kg → no winner, message mentions
"length" / "longitud" (quickstart §Block mixed measures).

### Tests for User Story 2 (write first, must fail)

- [x] T018 [P] [US2] Add ViewModel cases in
      `test/ui/compare/CompareViewModelUnitsTest.kt`: `Metre` vs each of
      `Kilogram`, `Litre`, `Piece` (and `Centimetre` vs `Gram`) →
      `incompatibleUnits == true`, `outcome == null`.
- [x] T019 [P] [US2] Update
      `androidTest/ui/compare/CompareScreenIncompatibleUnitsTest.kt`: m vs
      kg shows the new EN text (and ES if covered); switching B to cm
      clears the error. Update any existing assertion on the old text.

### Implementation for User Story 2

- [x] T020 [P] [US2] Update `error_incompatible_units` in
      `res/values/strings.xml` to "Both offers must use the same kind of
      measure (weight, volume, length, or count)."
- [x] T021 [P] [US2] Update `error_incompatible_units` in
      `res/values-es/strings.xml` to "Ambas ofertas deben usar el mismo
      tipo de medida (peso, volumen, longitud o unidades)."

**Checkpoint**: US1 + US2 green; mixed measures never show a winner.

---

## Phase 5: User Story 3 — Accessible, localized length labels (Priority: P2)

**Goal**: New options announced with full names; layout holds at 200%.

**Independent Test**: TalkBack in each locale announces "metres" /
"metros" on the option and "per m" / "por m" in the result
(quickstart §Accessibility).

### Tests for User Story 3

- [x] T022 [P] [US3] Add a case in
      `androidTest/ui/compare/CompareScreenAccessibilityTest.kt`: the `m` and
      `cm` menu options expose content descriptions `metres` /
      `centimetres`; the result live region contains `per m` for the
      issue #56 scenario.
- [x] T023 [P] [US3] Add a 200% font case in
      `androidTest/ui/compare/CompareScreenLargeFontTest.kt` (or
      `CompareScreenCompactRowTest.kt`, whichever already parametrises
      units): offers set to `cm` and `m` display their codes without
      clipping and keep the adaptive layout from feature 006.

### Implementation for User Story 3

- [x] T024 [US3] No production change expected (T013–T016 provide the
      strings and branches). If T022 / T023 fail, fix in
      `main/ui/compare/CompareScreenUnits.kt` without changing
      `UNIT_SELECTOR_WIDTH` unless a test proves it necessary.

**Checkpoint**: All three stories green.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [x] T025 [P] *(Skipped: previews do not cover per-dimension variants.)* Add a length-unit preview (e.g. issue #56 winner) in
      `main/ui/compare/CompareScreenPreviews.kt` if previews cover other
      dimensions.
- [ ] T026 Run the full gate from `android/`: `./gradlew lint detekt
      testDebugUnitTest jacocoTestReport` plus
      `connectedDebugAndroidTest`; JaCoCo core threshold must hold.
- [ ] T027 Manual [quickstart.md](./quickstart.md) on Marco's device
      (en + es, TalkBack, 200%, rotation, reset / undo, regression on
      kg / L / pcs).
- [ ] T028 Release prep (separate PR **AM**, after `v0.1.13` is tagged):
      versionCode 15 / **0.1.14** in `android/app/build.gradle.kts`;
      `fastlane/metadata/android/{en-US,es-ES}/changelogs/15.txt`;
      `docs/fdroid.md` history + twin metadata; `docs/project-status.md`
      (shipped row 009, versionCode map, cadence row); ledger entries
      AJ / AM in `specs/001-unit-price-comparison/tasks.md`.
- [ ] T029 After the `v0.1.14` tag: comment on issue #56 that length units
      shipped, and close it.

---

## Dependencies & Execution Order

- **Phase 1** done in the planning PR.
- **Phase 2** (T004–T007) blocks everything. T006 / T007 make exhaustive
  `when`s fail to compile until T015 / T016 land, so commit T006, T007,
  T013–T016 together (one green commit after the red tests).
- **US1** (Phase 3) is the MVP. **US2** (Phase 4) depends only on Phase 2
  (the gate is already generic) and can run in parallel with US1's tests.
- **US3** (Phase 5) depends on T013–T016 (strings and branches).
- **Phase 6** after all stories; T028 / T029 in later PRs.

## Parallel Example: User Story 1

```text
# Red tests, different files, in parallel:
T008 PriceComparatorTest   T009 ResultPresenterTest
T010 CompareViewModelUnitsTest   T011 CompareScreenUnitsTest
# Strings in parallel:
T013 values/strings.xml    T014 values-es/strings.xml
```

## Implementation Strategy

1. **MVP**: Phases 2 + 3 → the issue #56 scenario works. Already shippable
   in principle, but US2's message update must ship with it (spec: "must
   land together with US1").
2. **Same PR (AJ)**: Phases 4 + 5 + T025–T027. Small enough for one PR.
3. **Release prep (AM)**: T028 after `v0.1.13` is tagged and F-Droid has
   processed it; then tag `v0.1.14` and T029.

## Notes

- Commit after each logical group; English Conventional Commits, GPG-signed.
- Total: 29 tasks (3 done in planning). US1: 10, US2: 4, US3: 3.
- If a `[NEEDS CLARIFICATION]` surfaces while executing, stop and ask Marco.
