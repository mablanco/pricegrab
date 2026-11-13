# Research: Length Units (Feature 009)

## 1. How much of feature 004 can be reused

**Decision**: Extend the two enums; leave the comparison pipeline alone.

**Findings**:

- `Offer.quantityInBaseUnits = quantity × quantityUnit.toBaseMultiplier`
  and `unitPrice = price / quantityInBaseUnits` are dimension-agnostic.
- `ComparisonGate` and `PriceComparator.compare` reject mixed dimensions by
  comparing `quantityUnit.dimension` values, so a new `Dimension.Length` is
  incompatible with Mass / Volume / Count automatically.
- `ResultPresenter` scales `perUnitDelta` by `dimension.displayUnitScale`.
- `CompareScreenUnits` builds the menu from `QuantityUnit.entries`; enum
  declaration order is the selector order.
- `CompareViewModelKeys.readUnit` stores and restores `QuantityUnit.name`
  with a `Gram` fallback; new names round-trip with no new keys.
- Exhaustive `when` over `QuantityUnit` (`codeRes`, `nameRes`, `dimension`,
  `toBaseMultiplier`) and over `Dimension` (`formatSavingsLine`) make the
  compiler list every place that needs a branch.

**Rationale**: Smallest correct change; the 004 design anticipated more
dimensions.

**Alternatives considered**: A generic unit table / registry replacing the
enums — rejected as speculative for two units (YAGNI).

## 2. Base unit and display unit

**Decision**: Base unit **cm** (`Centimetre.toBaseMultiplier = 1`,
`Metre = 100`); display **per m** (`Length.displayUnitScale = 100`).

**Rationale**: Mirrors mass (base g, display kg) and volume (base ml,
display L): base is the smaller selectable unit so every multiplier is an
integer ≥ 1, and display matches DIY shelf labels (€/m). Issue #56's
example is priced per metre.

**Alternatives considered**:

- Base **mm** — rejected: mm is not offered (spec Assumptions), and an
  unselectable base unit adds nothing.
- Base **m** with cm = 0.01 — rejected: breaks the "multiplier ≥ 1"
  pattern and puts fractional factors in the hot path for no gain
  (`BigDecimal` would handle it, but consistency wins).
- Display per cm — rejected: per-cm figures are tiny (0.098) and unlike
  any store label.

## 3. Selector order

**Decision**: Declare `Centimetre, Metre` between `Litre` and `Piece`:
g, kg, ml, L, cm, m, pcs.

**Rationale**: Groups physical measures small → large per dimension and
keeps `pcs` last as the generic fallback (spec FR-001). Saved state uses
names, not ordinals, so reordering is safe.

**Alternatives considered**: Appending after `Piece` — rejected; splits
physical measures around the count option.

## 4. Codes and accessible names

**Decision**:

| Key | en | es |
|-----|----|----|
| `unit_code_cm` | `cm` | `cm` |
| `unit_code_m` | `m` | `m` |
| `unit_name_centimetre` | `centimetres` | `centímetros` |
| `unit_name_metre` | `metres` | `metros` |
| `result_savings_per_m` | `Save %1$s per m vs %2$s` | `Ahorras %1$s por m frente a %2$s` |

`error_incompatible_units` becomes "Both offers must use the same kind of
measure (weight, volume, length, or count)." / "Ambas ofertas deben usar
el mismo tipo de medida (peso, volumen, longitud o unidades)."

**Rationale**: SI symbols are identical in both locales (unlike `g`/`gr`,
`L`/`l`, which follow existing Spanish shelf usage). British spelling
matches existing `unit_name_millilitre` / `unit_name_litre`.

## 5. TalkBack wording in the result

**Decision**: The result live region keeps reading the **visible** savings
line ("Save 1 per m vs Offer A"), exactly as kg / L today. No separate
a11y-only "per metre" string.

**Findings**: `buildResultA11ySummary` concatenates headline + visible
savings line + percent line; there are no a11y-only variants for kg / L /
piece. The full unit name ("metres") is already announced on the selector
and its options via `unit_name_*` content descriptions.

**Rationale**: One code path for all dimensions; Android TTS reads unit
symbols after numbers in context. Spec FR-007 / US3 are aligned with this
decision (result announces "per m" / "por m"). Manual TalkBack check in
the quickstart confirms it is intelligible; if it is not, a follow-up
would add a11y variants for **all** dimensions at once, not just length.

**Alternatives considered**: An a11y-only `result_savings_per_metre` —
rejected for now: inconsistent with kg / L and more strings to keep in
sync.

## 6. Formatting of the canonical scenario

**Decision**: Contract and tests use the presenter's actual output, e.g.
"Save 1 per m vs Offer A" (not "1.00").

**Findings**: `ResultPresenter` formats the display delta with
`minFractionDigits = 0` (see `ResultPresenterTest`: 0.001/g → "1" per kg).
Issue #56: A = 49 / 500 cm = 0.098/cm → 9.80/m; B = 22 / 250 cm =
0.088/cm → 8.80/m; delta 0.01/cm × 100 = **1** per m; percent =
1 / 9.8 = 10.2 %.

## 7. Release and versioning

**Decision**: Release prep targets **0.1.14** / versionCode 15, cut only
after `v0.1.13` (toolchain upgrade) is tagged and F-Droid has processed it.

**Rationale**: Keeps F-Droid Mode B verification failures attributable to
one change at a time (same reasoning as deferring `v0.1.13`). A MINOR bump
(0.2.0) is Marco's call; the plan does not invent one.
