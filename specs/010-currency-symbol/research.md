# Research: Currency Symbol (Feature 010)

## 1. Automatic symbol source

**Decision**: `Currency.getInstance(locale).getSymbol(locale)` on the
locale already used for number formatting (`LocalLocale.current.platformLocale`).
Any exception (language-only locale, unknown region) or the ISO "no
currency" code `XXX` → `""`.

**Findings** (JDK 17, CLDR; Android uses ICU with the same CLDR data):

| Locale | Symbol |
|--------|--------|
| es-ES | € |
| en-US | $ |
| en-GB | £ |
| pl-PL | zł |
| de-CH | CHF |
| `en` / `es` (no region) | throws `IllegalArgumentException` → `""` |

**Rationale**: No list to maintain; follows the phone's region, which is
where most people shop.

**Alternatives considered**: Deriving from SIM / network country —
rejected (needs telephony APIs, wrong for Wi-Fi-only devices, privacy
smell). A curated picker — rejected in spec review (issue #57 asks for
free text).

## 2. Placement and spacing

**Decision**: Take prefix / suffix from the locale's currency
`DecimalFormat` after substituting the symbol, then wrap the
already-formatted amount: `prefix + amount + suffix`.

**Findings**: With symbol `zł` substituted:

| Locale | Prefix | Suffix | Example |
|--------|--------|--------|---------|
| es-ES | — | ` zł` (NBSP) | `1,00 zł` |
| en-US | `zł` | — | `zł1.00` |
| de-CH | `zł ` | — | `zł 1.00` |

**Rationale**: Uses platform locale data instead of hand-written rules,
handles NBSP spacing correctly, and keeps current rounding (we wrap our own
formatted number, so "1 €" stays "1 €", not "1,00 €").

**Alternatives considered**: `NumberFormat.getCurrencyInstance().format()`
directly — rejected: it forces the currency's fraction digits (two) and
would change how savings figures look (spec Assumptions). Per-language
hard-coded rules — rejected: brittle, wrong for regional variants.

## 3. Where the preference lives

**Decision**: Two new keys in the existing `appearance_preferences`
DataStore; separate `CurrencyPreferences` model and repository over the
same `DataStore` instance.

**Rationale**: Backup and data-extraction rules already include exactly
that file (security hardening, PR #60); a second file would need new
allowlist entries and a second DataStore singleton. Separate model keeps
appearance code untouched.

**Alternatives considered**: New `currency_preferences` file — rejected
for the backup-rule churn. Renaming the file to `user_preferences` —
rejected: needs a migration for existing installs.

## 4. Getting the symbol to Compare

**Decision**: `staticCompositionLocalOf { "" }` named `LocalCurrencySymbol`,
provided in `PriceGrabAppContent`.

**Rationale**: `CompareScreen` has a stateless overload with nine
callbacks and is constructed directly in a dozen instrumented tests and
previews. A CompositionLocal with an empty default means those call sites
keep compiling and keep today's no-symbol output unless a test opts in.
The value changes rarely (Settings), so static is fine.

**Alternatives considered**: New `currencySymbol` parameter on
`CompareScreen` — rejected: touches every call site for a display concern.
Putting the symbol in `CompareUiState` / ViewModel — rejected: it is a
preference, not form state, and must not enter saved state (FR-007).

## 5. Price field affix

**Decision**: Material 3 `OutlinedTextField` `prefix` / `suffix` slots,
chosen by which affix the locale pattern uses; content description becomes
"Price for Offer A, €".

**Rationale**: Affix slots are display-only (not part of the editable
text), so parsing is untouched and the cursor never lands inside the
symbol. Appending to the content description matches how quantity fields
append the unit name.

**Risk**: Compact single row at default font is the tightest layout
(price weight 1.15). A 1–4 character affix fits at `bodyLarge`; at large
font the adaptive layout already gives price the full width. Covered by
`CompareScreenCompactRowTest` + `CompareScreenLargeFontTest` with a
4-character symbol (`CHF` / `NT$`).

## 6. Savings line

**Decision**: Wrap `SavingsPresentation.perUnitDelta` with the affixes in
`CompareScreenResult` before passing it to the unchanged
`result_savings_per_*` templates. Percent line untouched.

**Rationale**: No template changes in either language; TalkBack reads the
visible line (as in features 008 / 009).

## 7. Settings UI

**Decision**: New "Currency" section below Appearance with two selectable
rows (`Automatic (€)` / `Custom`) and an `OutlinedTextField` enabled only
when Custom is selected. Input is `take(4)`; trimming happens at
resolution time so typing a space is not jarring. Each keystroke writes
the custom text to DataStore (cheap, no debounce needed at this size).

**Rationale**: Mirrors the Appearance radio rows (same semantics and
selected / not selected state descriptions), so a11y patterns are reused.

## 8. Existing instrumented tests

**Decision**: Add `expectedMoney(amount)` to `CompareScreenTestHelpers.kt`,
computing the symbol with the same resolver + formatter for the test
device's locale and default (Automatic) preferences, and use it in every
Activity-based savings assertion. Tests that render the stateless
`CompareScreen` keep plain amounts (empty default).

**Rationale**: The CI emulator is en-US, so the real app will show `$`;
hard-coding `$` would break on other locales.

## 9. Release

**Decision**: Ship with feature 009 as **0.1.14** (release-prep PR **AM**),
after `v0.1.13` is tagged on `d4dcde2` and processed by F-Droid.
