# Feature Specification: Compact Offer Input Row

**Feature Branch**: `006-compact-offer-row`
**Created**: 2026-07-22
**Status**: Draft
**Input**: User description: "Redesign the main comparison screen so each
offer’s three fields (price, quantity/units, and unit magnitude) sit on a
single compact row to use space better. Text fields feel too large for
typical 4–5 character numbers. At 200% system font scale, use an adaptive
layout so nothing truncates or overlaps."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Enter an offer on one compact row (Priority: P1)

A shopper in the aisle opens the app and sees each offer as a titled card
whose price, quantity, and unit controls sit on **one horizontal row** at
the default system font scale. The numeric fields are sized for short
values (a few characters), so two or three offers fit more comfortably on
a typical phone without scrolling past the result.

**Why this priority**: Direct response to density feedback; supports the
constitution goal that the primary compare flow fits without scrolling on
a typical portrait phone, especially with up to three offers.

**Independent Test**: Cold launch at default font scale → Offer A and B
each show price, quantity, and unit on a single row → enter typical short
values → comparison still works as today.

**Acceptance Scenarios**:

1. **Given** cold launch at the default system font scale, **When** the
   shopper views Offer A (and B), **Then** price, quantity, and unit
   appear on one row under the offer title (not stacked as full-width
   price above a separate quantity/unit row).
2. **Given** that single-row layout, **When** the shopper enters short
   numeric values (e.g. price `2.50`, quantity `500`) and picks a unit,
   **Then** the comparison result updates exactly as before (same winner
   and savings rules).
3. **Given** the shopper adds Offer C, **When** all three cards are
   visible at default font scale, **Then** each card uses the same
   single-row field layout.

---

### User Story 2 — Stay usable at large system font scale (Priority: P1)

A shopper who uses a large system font (up to at least 200%) must still
enter price, quantity, and unit without truncation, overlap, or loss of
controls. The layout **adapts**: when a single row would crowd or clip
content, the offer falls back to a more spacious arrangement (price on
its own row; quantity and unit on the row below) while keeping the same
fields and behavior.

**Why this priority**: Accessibility is a release blocker; density must
not trade away large-text usability.

**Independent Test**: Set system font scale to 200% → open compare screen
→ verify each offer uses the adaptive (non-cramped) arrangement → enter
values and complete a comparison → no truncation, overlap, or missing
controls.

**Acceptance Scenarios**:

1. **Given** system font scale at 200%, **When** the shopper views an
   offer card, **Then** the three controls are arranged so labels, typed
   values, and the unit control remain fully readable and tappable (no
   clipping or overlap).
2. **Given** that large-font arrangement, **When** the shopper completes
   a valid two-offer comparison, **Then** the result (winner + savings)
   still appears and remains understandable.
3. **Given** the shopper switches between default and large font scale
   (or the app recreates after a font-scale change), **When** previously
   entered values are present, **Then** those values are preserved and
   the layout matches the current scale’s arrangement.

---

### User Story 3 — Errors and assistive tech still clear (Priority: P2)

Invalid input and TalkBack announcements must remain clear after the
density change. Field errors stay actionable; each control keeps a
meaningful accessibility name that includes the offer identity.

**Why this priority**: Compact layout must not regress Principle II
(Accessibility) or empty/invalid messaging from Principle I.

**Independent Test**: Trigger a field error on price or quantity in the
compact row; verify message is visible. With TalkBack, focus price,
quantity, and unit and confirm localized descriptions naming the offer.

**Acceptance Scenarios**:

1. **Given** an invalid price or quantity on an offer, **When** the error
   is shown, **Then** the shopper sees an explicit, actionable message
   associated with that offer (not a silent failure).
2. **Given** TalkBack enabled, **When** focus moves to price, quantity,
   or unit on Offer A/B/C, **Then** each control announces a meaningful
   localized description that identifies the offer.
3. **Given** either layout arrangement (compact single row or large-font
   adaptive), **When** an error is shown, **Then** the message does not
   obscure adjacent controls or become unreadable.

## Edge Cases

- Very long typed values (beyond typical 4–5 characters) must remain
  editable; the field may scroll horizontally within itself rather than
  breaking the card layout.
- Narrow phones at default font scale must still keep all three controls
  on one row without clipping the unit control or making touch targets
  unusable.
- With three offers plus the result hero, default font scale on a typical
  phone should keep the primary compare flow usable with minimal or no
  scrolling; large font scale may scroll, but must not lose functionality.
- Configuration changes (rotation, theme, locale, font scale) preserve
  entered price/quantity/unit values.
- Adding/removing Offer C must not change which layout rule applies; only
  font scale / available space drives the adaptive arrangement.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: At the default system font scale on a typical phone width,
  each offer card MUST present price, quantity, and unit magnitude on a
  **single horizontal row** under the offer title.
- **FR-002**: Numeric price and quantity fields MUST be visually compact
  relative to today’s full-width stacking, sized for short values
  (approximately 4–5 characters) while remaining fully editable for
  longer input.
- **FR-003**: When system font scale is large enough that a single row
  would truncate, overlap, or drop below usable touch targets (including
  at least **200%** font scale), the offer card MUST switch to an
  **adaptive** arrangement: price on one row; quantity and unit on the
  following row (same fields and labels; no loss of controls).
- **FR-004**: Layout adaptation MUST NOT change comparison math, unit
  compatibility rules, add/remove offer behavior, reset/undo, or result
  wording — only the arrangement and density of the offer inputs.
- **FR-005**: Touch targets for price, quantity, and unit controls MUST
  remain at least 48×48 dp in both arrangements.
- **FR-006**: Empty, partial, and invalid input states MUST continue to
  show explicit, actionable messages for the affected offer.
- **FR-007**: Every interactive control on the offer card MUST keep a
  meaningful accessibility label that identifies the offer (TalkBack).
- **FR-008**: Both English and Spanish UI strings MUST remain complete;
  no new hardcoded user-facing text.
- **FR-009**: Entered values MUST survive configuration changes,
  including font-scale changes that switch layout arrangement.

### Key Entities

- **Offer card (presentation)**: titled slot showing price, quantity, and
  unit controls; arrangement depends on available space / font scale
  (compact single row vs adaptive two-row).
- **Offer slot (data)**: unchanged — raw price/quantity strings, selected
  quantity unit, and per-field validation errors.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: At default system font scale on a typical phone (~360 dp
  wide), each offer’s three controls appear on one row, and a two-offer
  compare (fields + result) completes without requiring the shopper to
  scroll past the result on a common mid-size portrait device.
- **SC-002**: At 200% system font scale, a shopper can enter two valid
  offers and read the comparison result with no truncated labels, no
  overlapping controls, and no missing fields.
- **SC-003**: After the layout change, shoppers still get correct
  winners and savings, same-dimension unit compares, add/remove of a
  third offer, and reset/undo — with no regressions versus today’s
  shipped behavior.
- **SC-004**: Manual TalkBack smoke on an offer card announces distinct,
  localized names for price, quantity, and unit in both layout
  arrangements.

## Assumptions

- This is a **presentation-only** redesign of the offer input chrome;
  multi-offer compare (feature 005), quantity units (004), and reset/undo
  (002) stay in place.
- “Adaptive” means switching between the compact single-row and the
  two-row (price; then quantity + unit) arrangements based on font scale
  / space — not a permanent return to today’s full-width price field at
  default scale.
- Exact visual breakpoint may be chosen during planning/implementation as
  long as default scale uses the single row and **200%** uses the
  spacious adaptive arrangement without clipping.
- Landscape / tablet / foldable redesign remains out of scope (still
  portrait-first).
- No new units, currencies, offer labels, or persistence features.

## Out of Scope

- Changing comparison formulas, savings vs second-cheapest, or max offer
  count (still 3).
- Free-text shop labels, favorites/templates, share/export, widgets.
- Custom fonts, SplashScreen branding, Material You dynamic-color toggle.
- Redesigning the result hero card beyond what incidental spacing needs.
