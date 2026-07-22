# Research: Compact Offer Input Row (Feature 006)

## 1. Adaptive trigger: font scale threshold

**Decision**: Derive arrangement from `LocalDensity.current.fontScale`:

| Condition | Arrangement |
|-----------|-------------|
| `fontScale < 1.3f` | `CompactSingleRow` — price \| quantity \| unit |
| `fontScale >= 1.3f` | `AdaptiveTwoRow` — price on row 1; quantity \| unit on row 2 |

**Rationale**: Matches the spec (default = compact; **200%** = spacious) and
hooks cleanly into the existing `CompareScreenLargeFontTest` pattern
(`fontScale = 2f` via `LocalDensity`). 1.3 is a practical point where three
labeled `OutlinedTextField`s start to crowd on ~360–411 dp widths.

**Alternatives considered**:

- **Width-only `BoxWithConstraints`**: better for foldables/narrow devices,
  but default-scale phones must stay single-row per FR-001; width-only can
  flip unexpectedly. Rejected as sole signal.
- **Always single row with horizontal scroll**: fails constitution II
  (truncation / hard-to-tap at 200%). Rejected.
- **Threshold only at 2.0f**: leaves 130–190% cramped. Rejected.

## 2. Compact row weights

**Decision**: In `CompactSingleRow`, use a `Row` with:

- Price: `Modifier.weight(1.15f)`
- Quantity: `Modifier.weight(1f)`
- Unit: fixed `UNIT_SELECTOR_WIDTH` (100.dp today)

Vertical alignment: `Alignment.Top` so uneven `supportingText` heights do
not misalign baselines awkwardly when one field has an error.

**Rationale**: Price values (e.g. `12.99`) and quantities (e.g. `500`) are
similar length; slight extra weight on price matches typical label length
(“Price” vs “Quantity”). Unit stays fixed so codes (`kg`, `uds`) + chevron
do not shrink below a usable target.

**Alternatives considered**: Equal weights; fixed `widthIn` max on both
fields (rejected — less flexible across locales/font).

## 3. Field density (visual size)

**Decision**: Keep Material 3 `OutlinedTextField` (not a custom tiny field).
Achieve “compact” primarily by **sharing one row** instead of a full-width
price stack. Do **not** set a hard max character length; long values scroll
inside the field (`singleLine = true`, already true).

Optional polish (only if default M3 height still feels oversized in QA):
`TextFieldDefaults.contentPadding` slightly reduced — must keep ≥48 dp
touch height.

**Rationale**: Custom mini-fields risk a11y and focus regressions; the
user’s complaint was wasted horizontal/vertical chrome from stacking, not
the M3 component itself.

**Alternatives considered**: `BasicTextField` + custom chrome (rejected —
more a11y work); always-dense `Compact` Material theme override app-wide
(rejected — out of scope / brand risk).

## 4. Error message placement

**Decision**: Keep per-field `isError` + `supportingText` on price and
quantity (current behaviour). Do not invent a single error strip below the
row in v1 of this feature.

**Rationale**: Existing instrumented/a11y expectations and string wiring
stay valid; Top-aligned row handles asymmetric error text. Spec FR-006
requires actionable messages associated with the offer — per-field already
satisfies that.

**Alternatives considered**: Shared error below the row (cleaner geometry,
more refactor); hide `supportingText` and rely on snackbar (rejected —
silent/weak).

## 5. Test strategy for “same row”

**Decision**:

1. Add a stable test tag on the compact row container, e.g.
   `{prefix}_fields_row` (only present / used in compact mode), **or**
   assert that price and quantity share approximately the same vertical
   center/top within a small tolerance via `getBoundsInRoot()`.
2. Prefer **bounds assertion** at default scale (no new production tag
   required) plus keep existing `offer*_price|quantity|unit` tags.
3. At `fontScale = 2f`, assert price’s bottom is clearly above quantity’s
   top (two-row), and all tags still `assertIsDisplayed` after scroll.

**Rationale**: Geometry assertions encode the UX contract without coupling
to ephemeral modifiers; tags remain stable for the large existing suite.

## 6. Scope freeze

**Decision**: No changes to `PriceComparator`, `OfferParser`,
`ComparisonGate`, reset/undo snapshots, add/remove offer, or result hero
copy. No new units or strings unless QA forces a label abbreviation (then
ES+EN together).

**Rationale**: Spec Out of Scope + presentation-only framing.

## 7. Release / version

**Decision**: Implementation PR does not bump `versionCode`. Release-prep
PR after merge chooses **0.1.9** (or next free patch) and updates
fastlane + `docs/project-status.md`.

**Rationale**: Matches 002–005 cadence (planning → impl → release-prep).
