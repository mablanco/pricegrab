# Implementation Plan: Length Units (cm, m)

**Branch**: `009-length-units` | **Date**: 2026-11-13 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/009-length-units/spec.md`

## Summary

Add **centimetres (`cm`)** and **metres (`m`)** to every offer's unit
selector so shoppers can compare goods sold by length (boards, cable,
rope, fabric — GitHub issue #56). Length becomes a fourth `Dimension`
with `cm` as the base unit; results always show savings **per m**, and
mixing length with mass, volume or count is rejected by the existing
incompatible-units gate with an updated message.

Technical approach: the feature 004 model already does all the work
generically. Add `Dimension.Length` (`displayUnitScale = 100`) and
`QuantityUnit.Centimetre` (×1) / `QuantityUnit.Metre` (×100), declared
between `Litre` and `Piece` so `QuantityUnit.entries` yields the FR-001
selector order. Add the `when` branches the compiler then requires
(`codeRes`, `nameRes`, `formatSavingsLine`), new `en`/`es` strings, and
update `error_incompatible_units`. `PriceComparator`, `ComparisonGate`,
`OfferParser`, `ResultPresenter`, `CompareViewModel` and saved-state
handling need **no** code change. Ship via planning (**AI**) →
implementation (**AJ**) → release prep (**AM**).

## Technical Context

**Language/Version**: Kotlin 2.4.21 (built-in Kotlin in AGP 9.5.1),
Gradle 9.9.0 — the toolchain from PR #69, unchanged.

**Primary Dependencies**: None new. Compose BOM 2026.09.00 / Material 3
`ExposedDropdownMenuBox` already renders the selector from
`QuantityUnit.entries`.

**Storage**: N/A. Units persist only through the existing
`SavedStateHandle` keys, which store `QuantityUnit.name`; the two new enum
names round-trip with no key or migration change. Unknown names still fall
back to `Gram` (`CompareViewModelKeys.readUnit`).

**Testing**:

- JVM unit:
  - `QuantityUnitTest`: cm/m dimension and multipliers; `entries` order is
    exactly g, kg, ml, L, cm, m, pcs.
  - `OfferTest`: `250 cm` and `2.5 m` normalise to the same base quantity
    and unit price.
  - `PriceComparatorTest`: issue #56 canonical case (5 m @ 49 vs
    250 cm @ 22 → B wins), tie (5 m vs 500 cm @ 49), three offers in length.
  - `ResultPresenterTest`: `Dimension.Length` scales a per-cm delta ×100
    (0.01/cm → "1" per m).
  - `CompareViewModelUnitsTest`: length vs mass/volume/count → incompatible;
    cm vs m → compares; reset returns to `Gram`, undo restores `Metre`.
  - `CompareViewModelSavedStateHardeningTest` (or units test): `Metre`
    survives saved-state restore.
- Compose UI / instrumented (en-US and es-ES where the suite already does):
  - `CompareScreenUnitsTest`: selector lists `cm` and `m` in order; canonical
    scenario shows "per m" / "por m".
  - `CompareScreenIncompatibleUnitsTest`: m vs kg shows updated message that
    names length / longitud.
  - `CompareScreenAccessibilityTest`: quantity unit option for metres has
    content description "metres" / "metros".
  - `CompareScreenLargeFontTest` / `CompareScreenCompactRowTest`: a row set
    to `cm` at 200% font is not clipped (`cm` is the widest new code; same
    width class as `kg`).
- Manual: [quickstart.md](./quickstart.md) on Marco's device, TalkBack + 200%.
- Coverage: JaCoCo core gate threshold unchanged; new branches are in
  `core/model` and are covered by the JVM tests above.

**Target Platform**: Android, `minSdk` 24, `targetSdk` 35, `compileSdk` 37
— unchanged.

**Project Type**: mobile-app (single Android application, no backend) —
unchanged.

**Performance Goals**: O(1) `BigDecimal` arithmetic as today; two more
dropdown items. Cold start ≤ 2 s unchanged (constitution IV).

**Constraints**:

- Fully offline; no new permissions; F-Droid Mode B reproducibility
  invariants (`docs/fdroid.md` §4) untouched — no build config change.
- ES + EN for every new or changed string.
- Selector width stays `UNIT_SELECTOR_WIDTH` (100 dp); new codes (`cm`,
  `m`) are no wider than existing ones (`kg`, `uds`).
- Do not change comparison math, rounding, tie rules or the 3-offer cap.

**Scale/Scope**: ~30–50 LOC production (2 enums, 3 `when` branches,
strings) + ~120–200 LOC tests. No version bump in the implementation PR;
release prep targets **0.1.14** (versionCode 15), after `v0.1.13` is
tagged. Stay on 0.1.x unless Marco decides otherwise.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Gates derived from `.specify/memory/constitution.md`.

| Principle / section | Gate | Plan evidence | Status |
|---------------------|------|---------------|--------|
| I. Modern Mobile UX | M3, single-purpose flow, clear invalid states | Same dropdown, two more options; mixed measures reuse the existing quiet error state. | ✅ |
| II. Accessibility (NON-NEGOTIABLE) | TalkBack, 200% font, 48 dp targets | New options get full-name content descriptions (`unit_name_*`); result live region reuses the visible savings line, as for kg/L; 200% layout test covers `cm`. | ✅ |
| III. Internationalization (ES/EN) | All copy in resources, locale numerals | New `unit_code_*`, `unit_name_*`, `result_savings_per_m` in `values` + `values-es`; updated incompatible message in both. | ✅ |
| IV. Offline-First Performance | No network, ≤ 2 s cold start, config-change safe | No I/O or deps; saved state stores enum names, new values round-trip. | ✅ |
| V. Test-First Quality (NON-NEGOTIABLE) | Red → Green → Refactor; CI gates | Failing JVM + instrumented tests written first (tasks Phase 2); coverage gate unchanged. | ✅ |
| Privacy & Platform | No trackers, F-Droid-compatible | No permissions, storage, network or build changes. | ✅ |

**Post-design re-check**: Passes. Decisions in [research.md](./research.md),
entities in [data-model.md](./data-model.md), behaviour pinned in
[contracts/length-units.md](./contracts/length-units.md).

## Project Structure

### Documentation (this feature)

```text
specs/009-length-units/
├── plan.md              # This file
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1 manual verification
├── contracts/
│   └── length-units.md  # Phase 1
├── checklists/
│   └── requirements.md
├── spec.md
└── tasks.md             # /speckit.tasks
```

### Source Code (touch list)

```text
android/app/src/main/kotlin/com/mablanco/pricegrab/
├── core/model/Dimension.kt          # MODIFY — add Length(100)
├── core/model/QuantityUnit.kt       # MODIFY — add Centimetre, Metre
└── ui/compare/
    ├── CompareScreenUnits.kt        # MODIFY — codeRes / nameRes branches
    ├── CompareScreenResult.kt       # MODIFY — Length → result_savings_per_m
    └── CompareScreenPreviews.kt     # OPTIONAL — a length preview

android/app/src/main/res/values/strings.xml      # MODIFY
android/app/src/main/res/values-es/strings.xml   # MODIFY

android/app/src/test/kotlin/com/mablanco/pricegrab/
├── core/model/QuantityUnitTest.kt
├── core/model/OfferTest.kt
├── core/calc/PriceComparatorTest.kt
└── ui/compare/{ResultPresenterTest,CompareViewModelUnitsTest}.kt

android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/
├── CompareScreenUnitsTest.kt
├── CompareScreenIncompatibleUnitsTest.kt
├── CompareScreenAccessibilityTest.kt
└── CompareScreenLargeFontTest.kt (or CompactRowTest)
```

**Structure Decision**: Single existing module `android/app`. No new files
in production code. `PriceComparator`, `ComparisonGate`, `OfferParser`,
`ResultPresenter`, `CompareViewModel`, `CompareViewModelKeys` stay
untouched.

## Complexity Tracking

> No constitution violations. Table left empty intentionally.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

## Implementation phases (preview for `/speckit.tasks`)

1. **Tests first (red)**: JVM tests for the new enums, conversion,
   comparator canonical case and presenter scale; ViewModel gate tests;
   instrumented selector / result / incompatible / a11y assertions.
2. **Model**: `Dimension.Length`, `QuantityUnit.Centimetre` / `Metre`.
3. **UI + strings**: `codeRes`, `nameRes`, `formatSavingsLine` branches;
   new and updated strings in `en` / `es`.
4. **Regression + manual**: full unit + instrumented suites, lint, detekt,
   JaCoCo; quickstart on device (TalkBack, 200%).
5. **Release prep** (separate PR): **0.1.14** / versionCode 15, changelogs
   en/es for code 15, `docs/fdroid.md`, `docs/project-status.md`; comment
   on issue #56 after the tag.

## PR cadence

| Letter | Branch / PR | Contents |
|--------|-------------|----------|
| **AI** | `009-length-units` (this branch) | Spec + plan + research + data-model + contracts + quickstart + tasks (planning only; no app source) |
| **AJ** | `feat/0xx-length-units-impl` | Implementation + tests |
| **AM** | `chore/0xx-fdroid-doc-sync-v0.1.14` | Version, changelogs, status / F-Droid docs |

Global ledger: `specs/001-unit-price-comparison/tasks.md` gains the AI entry
in this planning PR.
