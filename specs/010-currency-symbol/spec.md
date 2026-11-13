# Feature Specification: Currency Symbol

**Feature Branch**: `010-currency-symbol`
**Created**: 2026-11-13
**Status**: Draft
**Input**: User description: "Show a currency symbol on prices and savings,
configurable in Settings, per GitHub issue #57: 'It would be nice, if in
settings there would be an input box, where the user can input his local
currency from the keyboard. When a currency has been entered, it should be
shown everywhere [prices are shown].'"

## User Scenarios & Testing *(mandatory)*

### User Story 1 — See money amounts with a currency symbol (Priority: P1)

A shopper compares two offers. Today every amount is a bare number ("Save
1 per kg vs Offer A"), so it is not obvious at a glance which figures are
money and which are quantities. After this feature, the price fields and
the absolute savings line carry the shopper's currency symbol, placed the
way their language writes money (Spanish "1 €", English "€1" / "$1").
With no configuration, the symbol is the currency of the device's region.

**Why this priority**: It is the whole visible value of the feature and
the request in issue #57. A sensible automatic default means most shoppers
get it without opening Settings.

**Independent Test**: On a cold install with the device set to es-ES,
compare 2.50 / 500 g vs 4.00 / 1 kg and verify that both price fields show
`€` and the savings line reads "Ahorras 1 € por kg frente a Oferta A".
Repeat with en-US: `$` and "Save $1 per kg vs Offer A".

**Acceptance Scenarios**:

1. **Given** a fresh install on a device in region Spain (es-ES),
   **When** the Compare screen opens,
   **Then** every price field shows the `€` symbol next to the input, and
   quantity fields show no symbol.
2. **Given** the es-ES canonical comparison (2.50 / 500 g vs 4.00 / 1 kg),
   **When** the result appears,
   **Then** the savings line reads "Ahorras 1 € por kg frente a Oferta A"
   and the percent line stays "20% menos" (no symbol on percentages).
3. **Given** a device in region United States (en-US),
   **When** the same comparison completes,
   **Then** price fields show `$` and the savings line reads "Save $1 per
   kg vs Offer A".
4. **Given** a device region with no specific currency (or one Android
   cannot resolve),
   **When** the Compare screen opens,
   **Then** amounts show no symbol, exactly as today.
5. **Given** a tie, an incomplete form or incompatible units,
   **When** the result region updates,
   **Then** no savings amount is shown (unchanged quiet states).

---

### User Story 2 — Choose my own currency symbol in Settings (Priority: P1)

A shopper whose phone region does not match where they shop (a traveller,
or a phone set to English while shopping in Poland) opens Settings and
types the symbol they want, e.g. `zł`, `Kč`, `CHF` or `£`. From then on
every amount uses that symbol. They can return to the automatic default
at any time.

**Why this priority**: This is exactly what issue #57 asks for ("an input
box where the user can input his local currency"). A free-text symbol
covers every currency without the app maintaining a list.

**Independent Test**: In Settings, set a custom symbol `zł`, go back to
Compare, run any winning comparison, and verify price fields and savings
line use `zł`; restart the app and verify it persists; switch back to
Automatic and verify the region symbol returns.

**Acceptance Scenarios**:

1. **Given** Settings is open,
   **When** the shopper looks at the new Currency section,
   **Then** it offers **Automatic** (showing the symbol it resolves to,
   e.g. "Automatic (€)") and **Custom**, with Automatic selected by
   default.
2. **Given** the shopper selects Custom and types `zł`,
   **When** they return to Compare,
   **Then** price fields and the savings line use `zł`, positioned by the
   app language's money convention.
3. **Given** a custom symbol was saved,
   **When** the app is killed and relaunched,
   **Then** the custom symbol is still in use.
4. **Given** Custom is selected with an empty field,
   **When** the shopper returns to Compare,
   **Then** amounts show no symbol (an explicit way to turn symbols off).
5. **Given** the shopper selects Automatic again,
   **When** they return to Compare,
   **Then** the region symbol is used again; the previously typed custom
   symbol may be remembered for convenience but is not shown.
6. **Given** the shopper types more than the allowed length,
   **When** they keep typing,
   **Then** input stops at the limit (see FR-006); no error dialog.

---

### User Story 3 — Accessible, localized currency display (Priority: P2)

Currency must not break TalkBack, large fonts or the compact offer row
from feature 006.

**Why this priority**: Constitution principles II (accessibility) and III
(i18n) are release blockers.

**Independent Test**: With TalkBack on and font scale at 200%, in each
locale, run a winning comparison and verify the price fields and result
announcement include the currency, and nothing is clipped.

**Acceptance Scenarios**:

1. **Given** TalkBack is enabled,
   **When** focus lands on a price field,
   **Then** the announcement includes the currency symbol along with the
   existing label ("Price for Offer A").
2. **Given** a winning comparison,
   **When** TalkBack reads the result region,
   **Then** the savings line is read with its currency symbol (as shown).
3. **Given** system font scale at 200%,
   **When** the Compare screen shows two or three offers,
   **Then** the price field keeps the symbol visible without truncating
   the typed amount, using the adaptive layout from feature 006.
4. **Given** the Settings Currency section,
   **When** navigated with TalkBack,
   **Then** each option and the custom field have localized labels and
   selection state, like the existing Appearance options.

---

### Edge Cases

- **Symbol length**: custom symbols up to 4 visible characters (`CHF`,
  `zł`, `R$`, `kr`, `NT$`) are accepted; longer input is cut at 4.
- **Whitespace**: leading and trailing spaces in the custom symbol are
  ignored; a symbol of only spaces counts as empty (no symbol).
- **Language vs region**: position follows the **app language** (es →
  after the amount with a space, en → before without a space); the
  automatic symbol follows the **device region**. An en-GB device shows
  "£1", an es-US device shows "1 US$" or "1 $" as Android formats it.
- **Language change while running**: switching the device language
  updates placement and the automatic symbol on next composition, like
  other localized text.
- **Free offer / 100% savings**: the savings amount still shows the
  symbol.
- **Reset / undo**: unaffected; the symbol is a preference, not part of
  the form state.
- **Price input parsing**: the symbol is display-only; it is never part of
  the typed text, and typing a symbol into the price field is not
  required or supported.
- **Backup / restore**: the currency preference is backed up and restored
  like the existing appearance preference.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The app MUST display a currency symbol on every money amount
  it shows: each offer's price field and the absolute savings line. It
  MUST NOT add a symbol to quantities or percentages.
- **FR-002**: By default (**Automatic**), the symbol MUST be the local
  symbol of the device region's currency as provided by the platform. If
  none can be resolved, no symbol is shown.
- **FR-003**: Settings MUST offer a **Currency** section with two choices,
  **Automatic** (labelled with the symbol it resolves to) and **Custom**
  (with a text field).
- **FR-004**: The choice and the custom symbol MUST persist across app
  restarts and be included in the existing backup rules.
- **FR-005**: Placement (before/after the amount, spacing) MUST follow the
  app language's currency format, for both automatic and custom symbols.
- **FR-006**: The custom symbol MUST be trimmed and limited to 4
  characters. An empty custom symbol means "no symbol".
- **FR-007**: The symbol MUST be display-only. Price parsing, validation,
  comparison math, rounding, tie handling and saved form state MUST NOT
  change.
- **FR-008**: All new copy (section title, options, field label, content
  descriptions) MUST exist in `en` and `es`; TalkBack MUST announce the
  currency on price fields and in the result.
- **FR-009**: At 200% font scale, price fields MUST keep both the symbol
  and the typed amount visible in the compact / adaptive offer layout.
- **FR-010**: No new permissions or network access. No currency
  conversion or exchange rates.

### Key Entities

- **Currency preference**: mode (Automatic | Custom) plus the custom
  symbol text (possibly empty). Stored with the existing appearance
  preferences.
- **Effective currency symbol**: the symbol actually displayed — resolved
  from the device region (Automatic) or the custom text (Custom); may be
  empty.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: On a fresh install in es-ES and en-US, a tester sees the
  region's symbol on price fields and the savings line without opening
  Settings.
- **SC-002**: A tester sets a custom symbol in Settings in under 30
  seconds, and it persists across an app restart.
- **SC-003**: Comparison results (winner, tie, percentages, savings
  values) are identical to the previous release for the same inputs; only
  the symbol is added.
- **SC-004**: TalkBack and 200% font-scale smoke tests pass on a real
  device for Compare and Settings, with no regression in existing
  scenarios.
- **SC-005**: Cold start and offline behaviour are unchanged
  (constitution IV).

## Assumptions

- **Free-text custom symbol instead of a fixed list**: issue #57 asks for
  an input box, and free text covers every currency without maintaining a
  list. Automatic default covers the common case.
- **Position follows language, not the symbol**: the same symbol is
  placed differently in Spanish and English text, matching how each
  language writes money. The app does not ask the shopper where to put it.
- **Amounts keep current precision rules**: adding a symbol does not
  force two decimals; "1 €" stays "1 €", not "1,00 €".
- **Percent line unchanged**: percentages never carry a currency.
- **Release**: ships together with feature 009 in **0.1.14** if it is
  ready before that release is cut; otherwise in the next one.

## Out of Scope

- Currency conversion, exchange rates or any network use.
- ISO currency codes as a separate display mode (`EUR` vs `€`); a shopper
  can type `EUR` as a custom symbol if they want it.
- Per-offer currencies (comparing offers in different currencies).
- A curated currency picker list.
- Changing the number of decimals shown for amounts.
