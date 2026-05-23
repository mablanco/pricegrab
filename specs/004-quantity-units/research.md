# Research: Quantity Units for Offer Comparison (Feature 004)

Phase 0 research consolidating decisions the plan depends on. Format:
**Decision / Rationale / Alternatives considered**.

## 1. Unit set and defaults (resolves spec clarifications 2026-05-23)

**Decision**: Five selectable units — **g**, **kg**, **ml**, **L**,
**pcs** — with **g** as the default for both offers on cold launch and
after Reset.

**Rationale**:

- Covers the three dimensions shoppers actually compare in EU
  supermarkets: mass (yogurt, rice), volume (milk, juice), count
  (eggs, multipacks).
- Default **g** matches the most common package label format in ES/EU
  without forcing an extra tap for the canonical 500 g scenario.
- **pcs** avoids anglicism "units" in UI copy; localized full word in
  dropdown ("piezas" / "pieces") with short code in the closed selector.

**Alternatives considered**:

- *(a) Global "dimension picker" first (weight / volume / count)*:
  one less control per field but hides flexibility (500 g vs 1 kg still
  needs per-field scale). Rejected — per-field unit is more direct.
- *(b) Include oz / fl oz / lb*: useful for US locale later; out of
  scope for v1 (spec Out of Scope).
- *(c) Default kg*: fewer taps when labels show kg, but most small
  packages show grams; Marco chose g.

## 2. Display unit for results (per kg / per L / per piece)

**Decision**: Internal math normalizes to base units (**g**, **ml**,
**pcs**). Result savings strings always use the **shelf reference
unit**: **per kg**, **per L**, **per piece** — even when inputs used g
or ml.

**Rationale**:

- EU shelf labels and price tags quote €/kg and €/L; shoppers think in
  those references.
- Keeps one savings string shape per dimension; `ResultPresenter` picks
  the template from `Dimension`, not from the raw selector value.
- Example: 500 g @ €2.50 → €5.00/kg internally; savings vs 1 kg @ €4.00
  shows "Save €1.00 per kg".

**Alternatives considered**:

- *(a) Display in the same unit the user typed*: "per g" for gram
  inputs — technically correct but unfamiliar on shelf labels.
- *(b) Smart switch (per g below 1 kg, per kg above)*: clever but
  inconsistent between two offers and harder to test/announce in
  TalkBack.

## 3. Cross-dimension rejection

**Decision**: If Offer A and Offer B units map to **different
dimensions**, the comparison is **rejected** with a localized screen-level
error (`IncompatibleUnits`); no winner, no savings line.

**Rationale**:

- g ↔ ml conversion requires density; guessing would produce wrong
  winners (spec Out of Scope).
- Failing closed is safer than a silent wrong answer.
- Error clears automatically when the shopper aligns dimensions.

**Alternatives considered**:

- *(a) Force both offers to share one global dimension*: reduces errors
  but prevents legitimate mixed-scale same-dimension entry (g vs kg).
- *(b) Disable unit options that don't match the first pick*: hides
  options and confuses undo/reset; rejected for transparency.

## 4. Domain model shape

**Decision**: Add a pure-Kotlin `QuantityUnit` enum (or sealed hierarchy)
in `core/model/` with:

- `dimension: Dimension` (`Mass`, `Volume`, `Count`)
- `toBaseMultiplier: BigDecimal` (e.g. `Kilogram → 1000`, `Litre → 1000`)

Extend `Offer` with `quantityUnit: QuantityUnit`. **`unitPrice`** becomes
`price / quantityInBaseUnits` where `quantityInBaseUnits =
quantity * unit.toBaseMultiplier`.

**Rationale**:

- Keeps conversion out of Compose; `PriceComparator` stays pure Kotlin
  and JVM-testable (constitution V).
- Single place for conversion constants; no magic numbers in UI.

**Alternatives considered**:

- *(a) Store only base-unit quantity in ViewModel*: loses "what the user
  typed" fidelity for undo; still need parallel unit state.
- *(b) Separate `NormalizedOffer` type*: extra layer; `Offer` extension
  is sufficient.

## 5. UI control for unit selection

**Decision**: Material 3 **`ExposedDropdownMenuBox`** (compact) adjacent
to each quantity `OutlinedTextField` inside the offer card row — same
card layout, no new screen.

**Rationale**:

- Fits portrait without horizontal scroll on 411 dp (verified in plan
  manual verification section).
- 48 dp touch target via menu anchor + menu items.
- TalkBack: menu button gets unit name; field semantics append selected
  unit (US3).

**Alternatives considered**:

- *(a) Segmented buttons for all five units*: too wide for five options.
- *(b) Single shared unit for both offers*: breaks g vs kg scenario.

## 6. Reset / Undo integration (feature 002)

**Decision**: Extend `PreResetSnapshot` and `SavedStateHandle` keys with
`quantityUnitA` and `quantityUnitB` (stored as enum name strings).
Reset restores units to default **g**; Undo restores pre-reset units
character-for-character with raw strings.

**Rationale**:

- Matches feature 002 contract: undo restores exact pre-reset state.
- Spec FR-007 explicitly requires unit state in snapshot.

## 7. Result copy and `ResultPresenter`

**Decision**: Replace generic `result_savings` ("Save X per unit") with
three dimension-specific string keys:

- `result_savings_per_kg` / `result_savings_per_L` /
  `result_savings_per_piece`

`ResultPresenter.present(outcome, locale, dimension)` returns savings
text components; headline strings unchanged from feature 003.

**Rationale**:

- ES/EN parity with explicit shelf units.
- TalkBack reads full phrase from merged content description on result
  region (existing pattern).

## 8. Icon padding bundled in release prep

**Decision**: Phase 4 release prep includes `ART_SCALE` **0.86 → 0.82**
in `branding/regenerate-icons.py`, regenerate all mipmaps + fastlane
icons, manual check on circular launcher — **same tag as feature 004**,
no standalone icon release.

**Rationale**: Marco requested avoiding a v0.1.7 icon-only bump; visual
asset change ships with the next functional release.

## 9. Version and F-Droid cadence

**Decision**: Target **v0.1.7** (versionCode **8**). F-Droid picks up
via existing `AutoUpdateMode: Version`; no `fdroiddata` MR unless recipe
shape changes (PR T syncs `docs/fdroid.md` only).
