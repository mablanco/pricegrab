# Feature Specification: Length Units (cm, m)

**Feature Branch**: `009-length-units`
**Created**: 2026-11-13
**Status**: Planning (2026-11-13) — plan, research, data-model,
contracts, quickstart and tasks on branch `009-length-units` (PR AI)
**Input**: User description: "Add length units (centimetre, metre) for
comparing goods sold by length, per GitHub issue #56: 'When you're shopping
at a construction market, it would be nice to have length units. As for
e.g. in my country wood price is measured in meters -> e.g. 5m for 49€'."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Compare offers sold by length (Priority: P1)

A shopper in a hardware / DIY store compares two lengths of the same
material — a 5 m board for 49.00 and a 250 cm board for 22.00, or two
rolls of cable, rope, trim or fabric. Today PriceGrab only offers weight,
volume and piece units, so the shopper must either pick "pieces" (wrong
for different lengths) or convert by hand. After this feature, each
quantity selector also offers **centimetres (`cm`)** and **metres (`m`)**,
and the app compares the offers per metre, converting between cm and m
internally.

**Why this priority**: It is the whole feature and the only request from
an external user (issue #56). It reuses the comparison model from feature
004 (dimension + base unit), so it is small and self-contained.

**Independent Test**: On a cold install, set Offer A to `m` (price 49.00,
quantity 5) and Offer B to `cm` (price 22.00, quantity 250), and verify
that Offer B wins (A = 9.80 per m, B = 8.80 per m) with a savings line
that names **per m**, without any manual conversion.

**Acceptance Scenarios**:

1. **Given** an empty Compare screen,
   **When** the shopper opens any offer's unit selector,
   **Then** `cm` and `m` are listed alongside the existing units, and
   the default unit is still **grams (`g`)**.
2. **Given** Offer A = 49.00 for 5 **m** and Offer B = 22.00 for 250 **cm**,
   **When** the comparison completes,
   **Then** Offer B is cheaper and the savings line reads
   "Save 1 per m vs Offer A" (ES: "Ahorras 1 por m frente a
   Oferta A"), and the percent line reads "10.2% less" (ES: "10,2%
   menos") under the same rules as feature 008.
3. **Given** both offers use **cm** (e.g. 200 cm vs 300 cm),
   **When** the comparison completes,
   **Then** the savings line still references **per m** (shelf-style
   display convention, as kg / L for mass / volume).
4. **Given** three offers (feature 005) all in length units,
   **When** the comparison completes,
   **Then** the cheapest per metre wins and savings are named vs the
   second-cheapest, exactly as for other dimensions.

---

### User Story 2 — Block length vs other measures (Priority: P1)

Metres cannot be compared with kilograms, litres or pieces. If the
shopper mixes a length unit with any other kind of measure, the result
region shows the existing localized incompatible-units message (updated
to mention length) and no winner.

**Why this priority**: Without this guard the new units would make wrong
answers easier. It must land together with US1.

**Independent Test**: Set Offer A to `m` and Offer B to `kg`, enter valid
numbers, and verify that no winner is shown and the message names length
among the measure kinds, in both `en-US` and `es-ES`.

**Acceptance Scenarios**:

1. **Given** Offer A unit = **m** and Offer B unit = **kg** (or `L`, or
   `pcs`),
   **When** all numeric fields are valid,
   **Then** no winner is shown and the message reads "Both offers must
   use the same kind of measure (weight, volume, length, or count)."
   (ES: "…(peso, volumen, longitud o unidades).").
2. **Given** Offer A unit = **cm** and Offer B unit = **m**,
   **When** all numeric fields are valid,
   **Then** the comparison proceeds (same dimension, different scale).

---

### User Story 3 — Accessible, localized length labels (Priority: P2)

The new options must be announced and labelled correctly in English and
Spanish for TalkBack users and fit the compact offer row at large font
sizes.

**Why this priority**: Constitution principles II (accessibility) and III
(i18n) are release blockers for any new control or copy.

**Independent Test**: With TalkBack on, in each locale, select `m` on an
offer and complete a winning comparison; the quantity field announces the
unit name ("metres" / "metros") and the result announces the visible
savings line with "per m" / "por m", as it does for kg / L.

**Acceptance Scenarios**:

1. **Given** TalkBack is enabled,
   **When** focus lands on a quantity field set to `cm` or `m`,
   **Then** the announcement includes the full unit name (centimetres /
   metres; ES centímetros / metros).
   The result announcement reuses the visible savings line ("per m" /
   "por m"), consistent with the other dimensions.
2. **Given** the device locale is `es-ES`,
   **When** the unit selector is open,
   **Then** the new options show `cm` and `m` and their accessible names
   are in Spanish.
3. **Given** system font scale at 200%,
   **When** an offer uses `cm` or `m`,
   **Then** the offer row layout behaves as today (adaptive layout from
   feature 006), with no clipped or truncated unit labels.

---

### Edge Cases

- **Reset / Undo (feature 002)**: length units are cleared to the default
  (`g`) on reset and restored by undo, like every other unit.
- **Process death / rotation**: a selected `cm` / `m` survives
  configuration changes and process death like existing units.
- **Unknown saved unit**: a saved state written by a newer or older build
  with an unrecognised unit name falls back to the default unit (current
  behaviour).
- **Very small or very large lengths**: 0.5 cm and 10000 m compare
  correctly; the per-metre display uses the same rounding rules as other
  dimensions.
- **Tie**: 5 m @ 49.00 vs 500 cm @ 49.00 is a tie.
- **Free offer**: zero-price semantics unchanged; the savings line still
  names "per m".

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Each offer's unit selector MUST add **cm** and **m** to the
  existing options, so the full list is **g, kg, ml, L, cm, m, pcs** in
  that order.
- **FR-002**: The default unit on cold launch and after Reset MUST stay
  **g** (feature 004 FR-002 unchanged).
- **FR-003**: Length MUST be a new measure kind, with **centimetres** as
  the base unit and 1 m = 100 cm for normalisation.
- **FR-004**: A comparison mixing length with mass, volume or count MUST
  be rejected with no winner, using the incompatible-units message updated
  to list length in both locales.
- **FR-005**: For length comparisons, the savings line MUST name **per m**
  ("Save X per m vs Offer Y" / "Ahorras X por m frente a Oferta Y"),
  regardless of which of cm / m the shopper typed.
- **FR-006**: The percent line, tie handling, three-offer behaviour and
  hero card (features 005 and 008) MUST work for length exactly as for the
  other dimensions.
- **FR-007**: Unit labels and accessible names for cm and m MUST exist in
  `en` and `es`; TalkBack MUST announce the full unit name on the unit
  selector and its options, and the result MUST announce the visible
  savings line ("per m" / "por m"), as for the other dimensions.
- **FR-008**: No new permissions, network access or persisted data;
  existing reset / undo / saved-state behaviour extends to the new units.

### Key Entities

- **Measure kind (Dimension)**: gains **Length**, displayed per metre
  (100 × the base unit).
- **QuantityUnit**: gains **Centimetre** (base, ×1) and **Metre** (×100),
  both mapping to Length.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A tester completes the issue #56 scenario (5 m @ 49.00 vs
  250 cm @ 22.00) in both locales and sees Offer B win with a **per m**
  savings line, with no manual conversion.
- **SC-002**: Every pairing of a length unit with a non-length unit shows
  no winner and the updated incompatible-units message.
- **SC-003**: TalkBack and 200% font-scale smoke tests pass on a real
  device for the new options and result copy, with no regression on
  existing units.
- **SC-004**: All existing unit, compare, reset/undo and three-offer tests
  stay green; new tests cover cm/m conversion, length rejection, and the
  per-m strings.
- **SC-005**: Cold start and offline behaviour are unchanged
  (constitution IV).

## Assumptions

- **Unit set `{cm, m}`**: matches the issue ("centimeter, meter").
  Millimetres and kilometres are left out: goods are rarely priced per mm
  in stores, and km is not a shopping unit. Either can be added later
  without changing the model.
- **Display per m**: mirrors the shelf-style convention of feature 004
  (per kg, per L) and how DIY stores label prices (€/m).
- **Selector order**: length goes after volume and before pieces, so
  "pcs" stays last as the generic fallback.
- **Default stays `g`**: most comparisons are still groceries; length is
  one tap away.
- **Imperial lengths (in, ft, yd)**: out of scope, as with oz / lb in
  feature 004.

## Out of Scope

- Area and other derived units (m², m³), and conversion between length
  and area (e.g. flooring sold per m² vs per board).
- Millimetres, kilometres and imperial length units.
- Currency symbol (tracked separately in issue #57).
- Remembering the last selected unit across app restarts.
