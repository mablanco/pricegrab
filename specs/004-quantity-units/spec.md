# Feature Specification: Quantity Units for Offer Comparison

**Feature Branch**: `004-quantity-units`
**Created**: 2026-05-23
**Status**: Planning complete (2026-05-23) — plan, research, data-model,
contracts, and tasks on branch `004-quantity-units` (PR #29)
**Input**: User description: "Quantity units for offer comparison: let the shopper label each quantity with g, kg, ml, L, or pieces; convert within the same dimension so 500 g vs 1 kg compares fairly; show savings per normalized unit in results. Icon padding bump (ART_SCALE) ships in this feature's release prep, not as a standalone version."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Label each quantity with a real unit (Priority: P1)

A shopper in the supermarket aisle picks up two packages of the same
product type — one labelled "500 g" and another "1 kg" — and wants to
know which is cheaper **per kilogram**, not per abstract "unit". Today
PriceGrab accepts two bare numbers for quantity and assumes they are
comparable; the result says "Save X **per unit**", which forces the
shopper to mentally convert grams to kilograms (or millilitres to
litres) before trusting the answer. After this feature, each quantity
field has a compact unit selector (grams, kilograms, millilitres,
litres, or pieces) so the shopper types what they read on the package
label and the app handles the rest.

**Why this priority**: This is the core functional gap left from feature
001's assumptions ("quantities are dimensionless numbers"). Without it,
the app is easy to misuse (comparing 500 against 1000 without knowing
whether those are grams or millilitres) and the result wording is vague
for a general audience.

**Independent Test**: On a cold install, the tester selects `g` for Offer
A and `kg` for Offer B, enters price A = 2.50 / quantity A = 500, price
B = 4.00 / quantity B = 1, and receives a result that identifies B as
cheaper with a savings line that names a concrete unit (e.g. per kg),
without manually converting 500 g → 0.5 kg.

**Acceptance Scenarios**:

1. **Given** an empty Compare screen,
   **When** the shopper opens either quantity field,
   **Then** a unit selector is visible and defaults to **grams (`g`)**
   for Offer A and Offer B (see Assumptions).
2. **Given** Offer A quantity = 500 **g** and Offer B quantity = 1 **kg**,
   price A = 2.50 and price B = 4.00,
   **When** the comparison completes,
   **Then** Offer B is cheaper (A = €5.00/kg, B = €4.00/kg) and the
   savings line references **per kg**, not "per unit".
3. **Given** both quantities use **millilitres** (500 ml vs 1000 ml),
   **When** the comparison completes,
   **Then** the savings line references **per litre** (volume display
   convention — see Assumptions).
4. **Given** both quantities use **pieces** (e.g. 6 vs 12),
   **When** the comparison completes,
   **Then** the savings line references **per piece** (or the agreed
   count label in ES/EN).
5. **Given** the shopper changes a unit selector,
   **When** the four fields still parse,
   **Then** the result recomputes immediately without requiring a manual
   refresh (same live-update behaviour as today).

---

### User Story 2 — Block meaningless cross-dimension comparisons (Priority: P1)

Two offers are only comparable when their quantities measure the **same
kind of thing** — mass vs mass, volume vs volume, or count vs count.
Comparing "500 g" of yogurt with "1 L" of milk is undefined without
density information the app does not have. After this feature, if the
shopper picks incompatible units (e.g. grams on Offer A, millilitres on
Offer B), the screen shows a clear, localized error and **does not**
declare a winner.

**Why this priority**: Shipping unit labels without this guard would
make wrong answers easier, not harder. This story is co-equal with US1
for safety; both must land in the first implementation PR.

**Independent Test**: Set Offer A to `g` and Offer B to `ml`, enter any
valid prices and quantities, and verify the result region shows an
incompatible-units message (not a winner) in both `en-US` and `es-ES`.

**Acceptance Scenarios**:

1. **Given** Offer A unit = **g** and Offer B unit = **ml**,
   **When** all four numeric fields are valid,
   **Then** no winner is shown; a message explains that both offers
   must use the same kind of measure (weight, volume, or count).
2. **Given** Offer A unit = **kg** and Offer B unit = **g**,
   **When** all four numeric fields are valid,
   **Then** the comparison **proceeds** (same dimension, different
   scale — conversion handled internally).
3. **Given** an incompatible-units error is showing,
   **When** the shopper changes one unit selector so both offers share
   a dimension,
   **Then** the error clears and a normal result appears as soon as the
   numeric inputs still parse.

---

### User Story 3 — Accessible, localized unit labels (Priority: P2)

The new unit selectors and updated result strings must work for TalkBack
and for Spanish/English shoppers equally. A blind shopper should hear
"Quantity for Offer A: 500 grams" (not just "500"), and the savings
announcement should include the normalized unit ("save 1 euro per
kilogram").

**Why this priority**: Constitution principle II (accessibility) and
III (i18n) are release blockers. Unit labels are new interactive
controls and new result copy; they cannot land without a11y + locale
parity.

**Independent Test**: With TalkBack enabled in `es-ES`, enter a winning
comparison using kg/L/pcs units and verify the announcement order and
wording include unit names in Spanish; repeat in `en-US`.

**Acceptance Scenarios**:

1. **Given** TalkBack is enabled,
   **When** focus lands on a quantity field,
   **Then** the field's semantics include the selected unit name (not
   only the numeric value).
2. **Given** a completed comparison with a mass-based result,
   **When** TalkBack reads the result region,
   **Then** the savings line includes the display unit (e.g. "per
   kilogram" / "por kilogramo").
3. **Given** the device locale is `es-ES`,
   **When** the unit selector is opened,
   **Then** every unit option label is in Spanish (abbreviation may
   stay `g` / `kg` where universal).

---

### Edge Cases

- **Partial input**: unit selectors are always visible; if numeric fields
  are incomplete, behaviour stays as today (neutral prompt, no winner).
- **Zero / invalid quantity**: unchanged from feature 001 — explicit
  field error, no comparison.
- **Tie after conversion**: 500 g @ €2.50 vs 500 g @ €2.50 (different
  unit scales that normalize equally) → tie, same as today.
- **Very small quantities**: 250 ml vs 500 ml must still compare fairly;
  internal math uses base units (ml), display uses per litre.
- **Reset (feature 002)**: reset clears numeric fields **and** restores
  unit selectors to the default (`g`) for both offers.
- **Undo after reset**: restored values **and** restored unit selections
  from the snapshot taken at reset time.
- **Rotation / locale change**: selected units and entered values
  survive config changes (same bar as feature 001/002).
- **Free offer**: zero-price semantics unchanged; savings line still
  names the display unit.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Each offer's quantity input MUST expose a unit selector
  with exactly these options: **g**, **kg**, **ml**, **L**, **pcs**
  (pieces / units). Labels MUST be localized (ES/EN).
- **FR-002**: On cold launch and after Reset, both unit selectors MUST
  default to **g** until the shopper changes them.
- **FR-003**: The comparison engine MUST normalize both quantities to
  a base unit within their dimension before computing unit price:
  - mass → grams (`g`)
  - volume → millilitres (`ml`)
  - count → pieces (`pcs`)
- **FR-004**: A comparison MUST be rejected (no winner, localized
  error) when Offer A and Offer B use units from **different
  dimensions** (mass vs volume vs count).
- **FR-005**: The result savings line MUST name the **display unit**
  instead of the generic "per unit":
  - mass → **per kg** (`/kg`)
  - volume → **per L** (`/L`)
  - count → **per piece** (`/pc` or full word — see Assumptions)
- **FR-006**: Tie and winner headlines remain as in feature 003; only
  the savings body line and any unit-specific helper text change.
- **FR-007**: Reset/Undo (feature 002) MUST include unit selector
  state in the snapshot and restore it on undo; reset MUST return
  units to the FR-002 default.
- **FR-008**: All new controls MUST meet constitution accessibility
  gates (TalkBack labels, 48 dp touch targets, 200% font scale).
- **FR-009**: No new runtime permissions, no network, no persistence
  beyond the current ViewModel scope (units are not saved across cold
  starts unless FR-002 default counts as "saved").

### Key Entities

- **QuantityUnit**: one of `Gram`, `Kilogram`, `Millilitre`, `Litre`,
  `Piece`; each maps to a **Dimension** (`Mass`, `Volume`, `Count`) and
  a **base-unit multiplier** (e.g. `Kilogram → 1000 g`).
- **Offer** (extended): existing `price` + `quantity` plus
  `quantityUnit: QuantityUnit`; `unitPrice` computed on normalized
  quantity in base units, then interpreted for display using FR-005.
- **ComparisonOutcome**: unchanged shape (`Tie`, `AWins`, `BWins`); only
  presentation and preconditions gain unit-awareness.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A tester can complete the canonical scenario "500 g @
  €2.50 vs 1 kg @ €4.00" and see Offer B win with a **per kg** savings
  line, in both locales, without manual conversion.
- **SC-002**: Cross-dimension inputs (g vs ml) never show a winner;
  error copy is understandable to a non-technical shopper in user
  testing (Marco sign-off).
- **SC-003**: TalkBack smoke test passes on a real device for unit
  selectors + updated result strings (no regression vs feature 003 hero
  card semantics).
- **SC-004**: All existing instrumented tests for compare / reset / undo
  remain green after adaptation; new tests cover conversion, rejection,
  and display strings.
- **SC-005**: Cold start budget (≤ 2 s) and offline behaviour unchanged
  (constitution IV).

## Assumptions

- **Display convention (resolves open product question)**: Supermarket
  shoppers expect shelf-style reference units — **€/kg** for mass,
  **€/L** for volume, **€/piece** for count — even when they typed grams
  or millilitres in the inputs. Internal math normalizes to base units;
  only the **result copy** uses kg / L / piece.
- **Default unit `g`**: Most packaged foods in ES/EU show grams on the
  label; kg/L/pcs remain one tap away. A future feature may add
  "remember last unit" if Marco requests it.
- **No cross-dimension conversion**: Density-based conversion (g ↔ ml)
  is out of scope; the app explains why instead of guessing.
- **Currency**: Still no currency symbol in v1; bare decimals remain.
- **Two offers only**: Still A vs B (feature 001 scope unchanged).
- **Icon padding**: Increasing launcher/F-Droid icon inset
  (`ART_SCALE` 0.86 → ~0.82) is **not** part of this spec's user
  stories but **will** ship in this feature's release-prep phase (same
  tag, no icon-only micro-release) per Marco's direction.

## Initial clarifications resolved (2026-05-23)

Marco confirmed during spec review:

1. **Unit set** — `{g, kg, ml, L, pcs}` is correct for v1.
2. **Default unit** — **grams (`g`)** on cold launch and after Reset.
3. **Display convention** — result savings always name **per kg** (mass),
   **per L** (volume), or **per piece** (count), regardless of which
   scale the shopper typed in the inputs.

## Out of Scope

- Density-based conversion (weight ↔ volume).
- Custom units (oz, fl oz, lb) — may come in a later i18n/units feature.
- Currency selection or formatting with symbols.
- Persisting unit preference across app restarts.
- Changing the comparison math for prices (still two offers, same
  `PriceComparator` contract shape — extended inputs, not replaced).
