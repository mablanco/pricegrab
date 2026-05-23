# Implementation Plan: Quantity Units for Offer Comparison

**Branch**: `004-quantity-units` | **Date**: 2026-05-23 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/004-quantity-units/spec.md`

## Summary

Let shoppers label each offer's quantity with a real supermarket unit
(**g**, **kg**, **ml**, **L**, or **pcs**), normalize within the same
dimension before comparing unit prices, and show savings using shelf-style
reference units (**per kg**, **per L**, **per piece**) instead of the
generic "per unit". Reject cross-dimension comparisons (mass vs volume vs
count) with a localized screen-level error — no winner, no savings line.

Technical approach: add pure-Kotlin `QuantityUnit` and `Dimension` types
in `core/model/`, extend `Offer` with `quantityUnit` and base-unit
normalization, extend `OfferParser` and `CompareUiState` with per-offer
unit selection, add a dimension-compatibility gate in
`CompareViewModel.recomputeOutcome` before calling the unchanged
`PriceComparator.compare` shape (inputs are now unit-aware `Offer`s),
extend `ResultPresenter` to scale `perUnitDelta` for display and pick the
correct savings string template from `Dimension`, and add compact Material
3 `ExposedDropdownMenuBox` unit selectors beside each quantity field in
`CompareScreen`. Reset / Undo (feature 002) gains unit fields in
`PreResetSnapshot` and `SavedStateHandle`; Reset restores default **g**.
Release prep bundles `ART_SCALE` **0.86 → 0.82** icon regeneration in
the same tag (**v0.1.7**, `versionCode` **8**).

## Technical Context

**Language/Version**: Kotlin 2.0.21 — same toolchain as features 001–003.

**Primary Dependencies**: no additions. Material 3 Compose BOM already
provides `ExposedDropdownMenuBox`, `DropdownMenuItem`, and the existing
`OutlinedTextField` / `Card` / hero result components from feature 003.

**Storage**: None beyond existing `SavedStateHandle` keys. Two new keys
(`quantityUnitA`, `quantityUnitB`) persist the selected units across
rotation and process death; cold launch defaults both to `Gram` (FR-002).
No disk persistence across app restarts beyond that default.

**Testing**:

- JVM unit tests for `QuantityUnit` conversion constants,
  `Offer.quantityInBaseUnits` / `unitPrice`, extended `OfferParser`,
  dimension-compatibility logic in `CompareViewModel` (or a small pure
  helper if extracted), and extended `ResultPresenter` display scaling.
- Extend `PriceComparatorTest` canonical suite: existing cases remain valid
  when both offers use `Gram` with the same numeric quantities as today
  (backward-compatible dimensionless behaviour becomes explicit `g`).
- New JVM cases: 500 g vs 1 kg conversion, ml vs L, incompatible-dimension
  gate (ViewModel-level), display delta scaling (€/g → €/kg).
- Compose UI tests: unit selector visibility and default, canonical 500 g vs
  1 kg scenario, cross-dimension error (g vs ml), savings string names
  per kg / per L / per piece, reset restores default `g`, undo restores
  pre-reset units, TalkBack semantics include unit names (US3).
- Instrumented tests in both `en-US` and `es-ES`; 200% font scale layout
  on 411 dp width with the added dropdown controls.
- Constitution coverage gate on `core/calc/**` unchanged in threshold;
  `Offer.unitPrice` derivation moves but `PriceComparator` logic stays pure.

**Target Platform**: Android, `minSdk` 24, `targetSdk` 35,
`compileSdk` 35 — unchanged.

**Project Type**: mobile-app (single Android application, no backend) —
unchanged.

**Performance Goals**:

- Unit conversion is O(1) `BigDecimal` multiply/divide on four-field
  recompute — same main-thread budget as feature 001 live updates.
- Two dropdown menus add negligible composition cost; no new coroutines or
  I/O.
- Cold start to interactive remains ≤ 2 s (constitution IV); no new
  heavyweight assets in the hot path (icon PNGs change only in release prep).

**Constraints**:

- Fully offline; no new permissions or network use.
- F-Droid Mode B reproducibility preserved; icon PNG regeneration is
  committed output from `branding/regenerate-icons.py`, not build-time.
- No density-based g ↔ ml conversion (spec Out of Scope).
- Currency remains bare decimals (no symbol).

**Scale/Scope**: 2 new domain types, 1 enum extension on UI state, ~6–10
new strings per locale, 2 dropdown controls, `ResultPresenter` signature
extension, reset/undo snapshot extension. Estimated ~250–400 LOC
production + ~350–500 LOC test.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Gates derived from `.specify/memory/constitution.md` v1.0.1.

| Principle / section                          | Gate                                                                                                   | Plan evidence                                                                                                                                                                                                                                       | Status |
|----------------------------------------------|--------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|
| I. Modern Mobile UX                          | Material 3, ≥48dp touch, no scroll in main flow, non-color cues, single-purpose UI                     | Unit selectors use M3 `ExposedDropdownMenuBox` with ≥48dp anchor; offer cards stay in portrait without horizontal scroll on 411 dp (manual verification § below); incompatible-units error uses text + existing result-region layout, not color alone. |   ✅   |
| II. Accessibility (NON-NEGOTIABLE)           | TalkBack labels, 200% font scale, WCAG AA contrast, non-color-only cues, keyboard / D-pad reachability | Quantity field semantics append selected unit name; savings announcement includes "per kilogram" / localized equivalent; dropdown items reachable; instrumented a11y tests extend feature 003 hero card pattern.                                      |   ✅   |
| III. Internationalization (ES/EN)            | All strings in resources, locale-aware behaviour, both locales tested                                  | Unit option labels, incompatible-units error, and three savings templates (`per kg`, `per L`, `per piece`) in `values/` and `values-es/`; tests in both locales.                                                                                     |   ✅   |
| IV. Offline-First Performance & Reliability  | No network in core flow, cold start ≤ 2 s, APK ≤ 15 MB, no ANR, state preserved on config change       | No network; conversion is synchronous O(1); unit + raw string keys in `SavedStateHandle`; APK growth from strings + dropdown UI ≪ 15 MB cap.                                                                                                           |   ✅   |
| V. Test-First Quality (NON-NEGOTIABLE)       | TDD, ≥ 90% coverage on calculator, instrumented tests for the happy path, CI blocks merge on red       | Calculator tests extended first; new domain tests for conversion; ViewModel dimension gate tested on JVM; instrumented canonical scenario before merge.                                                                                                |   ✅   |
| Privacy & Platform Constraints               | No analytics/tracking SDKs, minimal permissions, latest stable target SDK, F-Droid compatible          | No new SDKs or manifest permissions; icon script output committed deterministically.                                                                                                                                                                  |   ✅   |
| Distribution (v1.0.1)                        | Signed APKs on GitHub Releases; reproducible build; no non-free blobs                                  | Release prep bumps version only; signing workflow unchanged.                                                                                                                                                                                         |   ✅   |
| Development Workflow & Quality Gates         | Spec-Driven Development; feature branch; PR with spec reference; lint + tests green                    | Branch `004-quantity-units`; planning PR #29; implementation PR S follows merged plan.                                                                                                                                                              |   ✅   |

**Outcome**: all gates pass. No entries needed in Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/004-quantity-units/
├── spec.md           # Feature specification
├── plan.md           # This file
├── research.md         # Phase 0 output (decisions backing this plan)
├── data-model.md       # QuantityUnit, Dimension, Offer extension, comparison gate
├── contracts/
│   └── quantity-units.md   # Extended comparator + presentation contract
└── tasks.md            # Phase 2 output (implementation task breakdown)
```

`quickstart.md` is intentionally absent: feature 001's
[`quickstart.md`](../001-unit-price-comparison/quickstart.md) remains the
canonical project entry point.

### Source code (changes extend features 001–003)

```text
android/app/src/main/kotlin/com/mablanco/pricegrab/core/model/
├── QuantityUnit.kt          # NEW — enum + Dimension + base multipliers
├── Dimension.kt             # NEW — Mass | Volume | Count (+ display scale)
├── Offer.kt                 # MODIFY — +quantityUnit, quantityInBaseUnits, unitPrice on base qty
├── OfferParseResult.kt      # unchanged shape; Success.offer carries unit
└── ComparisonOutcome.kt     # unchanged — winner/tie shape; deltas in base units

android/app/src/main/kotlin/com/mablanco/pricegrab/core/format/
└── OfferParser.kt           # MODIFY — parse(..., quantityUnit: QuantityUnit)

android/app/src/main/kotlin/com/mablanco/pricegrab/core/calc/
└── PriceComparator.kt       # unchanged signature — operates on normalized Offer.unitPrice

android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/
├── CompareUiState.kt        # MODIFY — quantityUnitA/B, ComparisonGate / incompatible flag
├── CompareViewModel.kt      # MODIFY — unit change handlers, SavedStateHandle keys,
                             #           dimension gate, reset/undo unit snapshot
├── CompareScreen.kt         # MODIFY — ExposedDropdownMenuBox per quantity field,
                             #           incompatible-units result branch, a11y semantics
└── ResultPresenter.kt       # MODIFY — present(..., dimension), display delta scaling

android/app/src/main/res/values/strings.xml       # +unit labels, savings templates, error
android/app/src/main/res/values-es/strings.xml    # mirror

android/app/src/test/kotlin/com/mablanco/pricegrab/core/model/
└── QuantityUnitTest.kt      # NEW — multipliers, dimension mapping

android/app/src/test/kotlin/com/mablanco/pricegrab/core/format/
└── OfferParserTest.kt       # MODIFY — cases with kg / ml / L units

android/app/src/test/kotlin/com/mablanco/pricegrab/core/calc/
└── PriceComparatorTest.kt   # MODIFY — explicit Gram units; add conversion case

android/app/src/test/kotlin/com/mablanco/pricegrab/ui/compare/
├── ResultPresenterTest.kt           # MODIFY — per-kg / per-L / per-piece templates
├── CompareViewModelUnitsTest.kt     # NEW — dimension gate, reset/undo units
└── CompareViewModelResetTest.kt     # MODIFY — snapshot includes units

android/app/src/androidTest/kotlin/com/mablanco/pricegrab/ui/compare/
├── CompareScreenUnitsTest.kt              # NEW — canonical 500 g vs 1 kg, defaults
├── CompareScreenIncompatibleUnitsTest.kt  # NEW — g vs ml, error clears on fix
├── CompareScreenAccessibilityTest.kt      # MODIFY — unit in quantity semantics
├── CompareScreenResetTest.kt              # MODIFY — reset restores g; undo restores units
└── CompareScreenLargeFontTest.kt          # MODIFY — dropdown + fields at 200% scale

branding/regenerate-icons.py   # MODIFY — ART_SCALE 0.86 → 0.82 (Phase 4 only)
android/app/src/main/res/mipmap-*/          # REGENERATE — Phase 4
fastlane/metadata/android/{en-US,es-ES}/images/icon.png  # REGENERATE — Phase 4
```

**Structure decision**: keep conversion math in `core/model/Offer.kt` and
dimension gating in `CompareViewModel` (or a one-file
`ComparisonGate.kt` in `core/calc/` if the ViewModel test becomes awkward
— prefer inline gate unless tests force extraction). UI dropdowns stay
inside `CompareScreen.kt` as private composables mirroring
`LabeledNumberField`, not a new package.

### Compare screen layout (Phase 1 design)

```text
┌─────────────────────────────────────────┐
│  [glyph]  PriceGrab              [↻]    │
├─────────────────────────────────────────┤
│  Offer A                                │
│  ┌────────────────────────────────────┐ │
│  │ Price: [______________]            │ │
│  │ Quantity: [________] [g ▼]         │ │  ← numeric field + unit dropdown
│  └────────────────────────────────────┘ │
│  Offer B                                │
│  ┌────────────────────────────────────┐ │
│  │ Price: [______________]            │ │
│  │ Quantity: [________] [g ▼]         │ │
│  └────────────────────────────────────┘ │
│                                         │
│  ╔═════════════════════════════════════╗│
│  ║  ✓ Offer B is cheaper               ║│
│  ║  Save 1.00 per kg                   ║│  ← dimension-specific savings line
│  ╚═════════════════════════════════════╝│
│                                         │
│  — or when g vs ml —                    │
│  Both offers must use the same kind     │
│  of measure (weight, volume, or count). │
└─────────────────────────────────────────┘
```

On 411 dp width: quantity row uses `Row(Modifier.fillMaxWidth())` with
the `OutlinedTextField` weighted (`Modifier.weight(1f)`) and a fixed-min
width dropdown anchor (~72–88 dp) so five unit labels (`g`, `kg`, `ml`,
`L`, `pcs`) fit without horizontal page scroll.

### Normalization and display (Phase 1 design)

| Shopper input unit | Dimension | Base unit | `toBaseMultiplier` | Display reference | Display scale on `perUnitDelta` |
|--------------------|-----------|-----------|--------------------|-------------------|---------------------------------|
| g                  | Mass      | g         | 1                  | per kg            | × 1000                          |
| kg                 | Mass      | g         | 1000               | per kg            | × 1000                          |
| ml                 | Volume    | ml        | 1                  | per L             | × 1000                          |
| L                  | Volume    | ml        | 1000               | per L             | × 1000                          |
| pcs                | Count     | pcs       | 1                  | per piece         | × 1                             |

`Offer.unitPrice = price / (quantity × toBaseMultiplier)` — always in
**base-unit price** (€/g, €/ml, €/pc). `PriceComparator` compares those
base prices. `ResultPresenter` multiplies `perUnitDelta` by the display
scale for the savings line only; percent delta is unchanged.

### Reset / Undo integration

`PreResetSnapshot` gains `quantityUnitA: QuantityUnit` and
`quantityUnitB: QuantityUnit`. `resetComparison()` clears units to
`QuantityUnit.Gram` in both `SavedStateHandle` and in-memory state.
`undoReset()` restores the snapshot units exactly. `isResetEnabled` treats
a non-default unit selection as non-empty form state (changing only the
dropdown from default `g` to `kg` enables Reset even if numeric fields are
blank — matches "shopper touched the form").

## Complexity Tracking

No violations. Table intentionally empty.

## Manual verification

After landing implementation (PR S) and before tagging **v0.1.7**, the
following walkthrough must pass on a signed release APK. Feature 002
reset/undo and feature 003 hero card behaviour must remain intact.

1. **Cold launch defaults**: Both unit selectors show **g**; result
   placeholder visible; Reset disabled.
2. **Canonical mass conversion**: Offer A = 2.50 / 500 **g**; Offer B =
   4.00 / 1 **kg**. Offer B wins; savings line says **per kg** (not "per
   unit"); headline unchanged from feature 003.
3. **Volume display**: 1.20 / 500 **ml** vs 2.00 / 1 **L** — fair
   comparison; savings references **per L** / **por litro**.
4. **Count display**: 3.00 / 6 **pcs** vs 5.00 / 12 **pcs** — savings
   references **per piece** / **por pieza**.
5. **Cross-dimension block**: Offer A **g**, Offer B **ml**, valid numbers
   — no winner; localized incompatible-units message; changing B to **kg**
   clears error and shows a normal result.
6. **Live recompute**: Change unit selector with valid numbers — result
   updates without extra button tap.
7. **Reset / Undo**: Enter values + change units away from default; Reset
   clears numbers **and** restores both selectors to **g**; Undo brings
   back numbers **and** unit selections.
8. **Rotation / locale**: Repeat step 2; rotate device — values, units, and
   result survive. Switch to **es-ES**; dropdown labels and savings string
   in Spanish.
9. **Accessibility**: TalkBack on quantity field announces value **and**
   unit (e.g. "500 grams"); result region announces "per kilogram" in the
   savings phrase.
10. **Font scale**: 200% system font — no truncation, no horizontal scroll
    on Pixel 4a-class width.
11. **Icon padding (release prep)**: After `ART_SCALE = 0.82` regeneration,
    launcher icon on circular mask shows slightly more inset; no clipping
    of corner elements vs v0.1.6.

If any step fails, do not tag **v0.1.7**.

## Release prep (Phase 4 — ships in PR S implementation + PR T doc sync)

| Item | Value |
|------|-------|
| `versionName` | `0.1.7` |
| `versionCode` | `8` |
| Functional changelog | Quantity units, cross-dimension guard, per kg/L/piece savings |
| Icon change | `branding/regenerate-icons.py` — `ART_SCALE` **0.86 → 0.82**; rerun script; commit all `mipmap-*` + fastlane `icon.png` outputs |
| Tag | `v0.1.7` pushed from `main` after PR S merges and manual verification passes |
| F-Droid | PR T updates `docs/fdroid.md` §3 / §5 to v0.1.7 full SHA; GitLab YAML edit by hand in same window |

Current baseline before this feature: **v0.1.6** / `versionCode` **7**
(`android/app/build.gradle.kts` on `main` at planning time).
