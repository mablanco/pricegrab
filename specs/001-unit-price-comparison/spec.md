# Feature Specification: Unit-Price Comparison Screen

**Feature Branch**: `001-unit-price-comparison`
**Created**: 2026-04-24
**Status**: Draft
**Input**: User description: "Pantalla principal de comparación de precio por unidad: el usuario introduce precio y cantidad del producto A y del producto B; la app calcula el precio por unidad de cada uno y muestra cuál sale más barato, por cuánta diferencia por unidad, y cuánto se ahorra al comprar N unidades de la opción más barata en lugar de la otra."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Decide which of two products is cheaper per unit (Priority: P1)

A shopper standing in a supermarket aisle is looking at two variants of the same
type of product that come in different package sizes and at different prices.
They open PriceGrab, type in the price and the quantity of product A, then the
price and the quantity of product B, and the app immediately tells them which
option is cheaper per unit.

**Why this priority**: This is the entire value proposition of the app. Without
this story there is no product; with just this story shipped the app already
solves the user's core problem (the MVP).

**Independent Test**: A tester can open the app on a cold install, enter four
numeric values (price A, quantity A, price B, quantity B), and receive a clear,
unambiguous answer identifying which option is cheaper per unit — without any
other feature of the app being present.

**Acceptance Scenarios**:

1. **Given** an empty screen,
   **When** the shopper enters price A = 2.50, quantity A = 500, price B = 4.00, quantity B = 1000,
   **Then** the screen states that product B is cheaper per unit (A = 0.005/unit, B = 0.004/unit)
   and identifies B as the better deal.
2. **Given** an empty screen,
   **When** the shopper enters price A = 3.00, quantity A = 1, price B = 5.00, quantity B = 2,
   **Then** the screen states that product B is cheaper per unit and identifies B as the better deal.
3. **Given** values have been entered for A and B,
   **When** the shopper edits any of the four values,
   **Then** the result updates immediately (or at the next user action, depending on UX decision in plan),
   without requiring the user to manually re-trigger a calculation.
4. **Given** an empty screen,
   **When** the shopper enters values that make A and B equal per unit (e.g., 2.00/100 and 4.00/200),
   **Then** the screen indicates the two options cost the same per unit (a tie) and neither is marked as cheaper.

---

### User Story 2 — See *how much* cheaper the better option is (Priority: P2)

Once the shopper knows which option is cheaper, they want to know whether the
difference is meaningful or negligible so they can weigh it against other
factors (brand preference, expiry date, etc.).

**Why this priority**: Knowing the winner without knowing the magnitude is half
an answer. This story materially improves decision-making and is a minimal
extension of the P1 calculation. It is not blocking for MVP release but should
be in v1.0.

**Independent Test**: Given the same inputs as User Story 1, the screen
displays both the absolute per-unit saving (e.g., "saves €0.001 per unit") and
the relative saving as a percentage (e.g., "20% cheaper per unit"). The tester
verifies the two figures mathematically match the inputs.

**Acceptance Scenarios**:

1. **Given** price A = 2.50, quantity A = 500, price B = 4.00, quantity B = 1000,
   **When** the result is computed,
   **Then** the screen shows that B is 20% cheaper per unit than A and that choosing B saves
   approximately 0.001 currency units per quantity-unit.
2. **Given** a winning option,
   **When** the per-unit difference rounds to 0 at the displayed precision,
   **Then** the screen shows the winner and states that the difference is negligible
   (rather than displaying "0% cheaper").

---

### Edge Cases

- **Empty or partial input**: the shopper has not yet entered all four values.
  The screen MUST NOT display a winner or a nonsensical result; it should show a
  neutral prompt to complete the missing fields.
- **Zero quantity**: a quantity of zero makes the per-unit price undefined
  (division by zero). The screen MUST reject this input with an explicit
  message next to the offending field and MUST NOT display a comparison result.
- **Zero price**: a price of zero is legal input (e.g., a free sample) and
  results in a per-unit price of 0 for that option; that option wins unless the
  other option also has price 0.
- **Negative values**: negative prices and negative quantities are invalid and
  MUST be rejected with an explicit message. On platforms where a numeric
  keypad can prevent the minus sign, prevention is preferred over post-hoc
  validation.
- **Non-numeric or malformed input**: must be prevented at the keyboard level.
- **Locale decimal separator**: in `es` the decimal separator is `,` and in
  `en` it is `.`. The screen MUST accept the locale's separator transparently
  and MUST NOT require the shopper to adapt their habit.
- **Very large or very small numbers**: the screen MUST either display the
  result gracefully (switching to an appropriate notation) or reject the input
  with an explicit "out of range" message. It MUST NOT silently overflow.
- **Rotation / theme / locale change mid-entry**: user-entered values and the
  current result MUST be preserved across configuration changes.
- **Accessibility**: TalkBack users MUST receive an announcement of the result
  when it becomes available or changes, and every input and label MUST have a
  meaningful content description.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The screen MUST expose four input fields clearly labelled as
  "Price A", "Quantity A", "Price B", "Quantity B" (in the active locale).
- **FR-002**: The screen MUST accept non-negative decimal numbers in the four
  input fields and MUST use the active locale's decimal separator.
- **FR-003**: The screen MUST present a decimal numeric keypad (no letters, no
  minus sign) when any of the four fields is focused.
- **FR-004**: When all four inputs are present and valid, the screen MUST
  compute `unit_price_A = price_A / quantity_A` and
  `unit_price_B = price_B / quantity_B` and MUST display, in an area visually
  distinct from the inputs:
  1. The per-unit price of A and of B, formatted per the active locale.
  2. Which option is cheaper, or that they are tied.
  3. The absolute per-unit difference between the two options.
  4. The relative per-unit difference as a percentage, computed against the
     more expensive option.
- **FR-005**: The screen MUST update the result whenever any of the four input
  values changes, without requiring the shopper to press an additional button.
  (The "Calcular" button used by the legacy prototype is not a requirement for
  v1; the plan phase may still include one if research indicates it improves
  discoverability, but the result MUST NOT be gated behind an explicit button.)
- **FR-006**: While inputs are incomplete, invalid, or cleared, the screen
  MUST NOT display a stale or misleading result. It MUST either hide the
  result area or display a neutral prompt inviting the shopper to complete the
  missing fields.
- **FR-007**: Any input validation error MUST be surfaced next to the
  offending field with a message understandable to a non-technical shopper.
- **FR-008**: The screen MUST NOT rely on color alone to communicate the
  comparison outcome. The winning option MUST also be identified by text and
  by an icon, symbol, or position.
- **FR-009**: All user-facing text on the screen MUST be available in English
  and in Spanish, selected from the device's active locale.
- **FR-010**: The screen MUST preserve all user-entered values and the current
  result across configuration changes (rotation, theme change, locale change
  within the OS settings, app backgrounding).
- **FR-011**: The screen MUST be fully operable with TalkBack. Every
  interactive element MUST have a non-empty content description, the result
  changes MUST be announced, and keyboard / D-pad navigation MUST reach every
  control in a logical order.
- **FR-012**: The screen MUST remain functional with the system font scale set
  up to 200% without truncation, overlap, or loss of information.
- **FR-013**: The screen MUST function fully offline; no operation on this
  screen MAY require network access.
- **FR-014**: The screen MUST NOT collect, persist, or transmit any of the
  values entered by the shopper beyond the lifecycle needed to display the
  current result and survive configuration changes.

### Key Entities

- **Offer**: A single side of the comparison (A or B). Attributes: a `price`
  (non-negative decimal) and a `quantity` (positive decimal). Derived
  attribute: `unit_price = price / quantity`, defined only when `quantity > 0`.
- **Comparison Result**: Produced from two Offers. Attributes: the `winner`
  (Offer A, Offer B, or `tie`), the `absolute_per_unit_difference`
  (non-negative decimal), the `relative_per_unit_difference` (percentage of
  the loser's unit price). Defined only when both Offers are valid.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A first-time shopper can obtain a correct comparison result in
  under 15 seconds from the moment they open the app, measured on a mid-tier
  phone with the device language set to Spanish or English.
- **SC-002**: 100% of the canonical calculation test cases (including equal
  unit prices, A cheaper, B cheaper, very large numbers, very small numbers,
  and locale-specific decimal parsing) produce a result that is correct to
  within the displayed precision.
- **SC-003**: 95% of shoppers in a usability test correctly identify the
  cheaper option on their first attempt without reading any external
  instructions.
- **SC-004**: A tester using TalkBack with eyes closed can complete the full
  comparison flow and report the correct winner, in both Spanish and English.
- **SC-005**: With the system font scale at 200%, no user-visible text on the
  screen is truncated or obscured, and the user can still reach every input
  and read the full result.
- **SC-006**: No functionality on this screen requires network access, and
  turning the device to airplane mode has no observable effect on the flow.
- **SC-007**: Zero values entered by the shopper are retained across rotation
  and theme changes in 100% of test runs.

## Assumptions

- The shopper is responsible for ensuring that the quantity of A and the
  quantity of B are expressed in the same unit of measure (e.g., both in grams,
  or both in millilitres, or both in "pieces"). The app treats the two
  quantities as dimensionless numbers that are comparable to each other; it
  does not display or enforce a unit of measure in v1.
- Prices are assumed to share a single currency within one comparison; the app
  does not perform currency conversion and does not display a currency symbol
  in v1 (the per-unit difference is shown as a bare decimal and as a
  percentage).
- The "how much is saved when buying N units" framing from the original prompt
  is modelled in v1 as (a) the absolute per-unit saving and (b) the relative
  per-unit saving in percent. An explicit "how many units do you plan to buy?"
  input is deferred to a future feature.
- Supported locales for v1 are English (`en`) and Spanish (`es`). Additional
  locales can be added later without code changes by providing translated
  string resources.
- The comparison is always between exactly two offers. A future feature may
  extend this to three or more; such extension is out of scope for v1.
- No persistence, history, or account concept is in scope for v1. Each app
  launch starts from an empty screen.
- No data collection, analytics, telemetry, or advertising identifiers are in
  scope. Ever. (Governed by the constitution's Privacy & Platform Constraints.)
