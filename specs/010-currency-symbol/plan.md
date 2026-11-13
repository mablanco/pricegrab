# Implementation Plan: Currency Symbol

**Branch**: `010-currency-symbol` | **Date**: 2026-11-13 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/010-currency-symbol/spec.md`

## Summary

Show a currency symbol on every money amount (price fields and the
absolute savings line), defaulting to the device region's currency and
overridable in Settings with a free-text custom symbol (issue #57). The
symbol is display-only: parsing, comparison math and form state do not
change.

Technical approach:

- **Preference**: a `CurrencyPreferences(mode, customSymbol)` pair stored
  as two new keys in the existing `appearance_preferences` DataStore, so
  the current backup rules already cover it. Read and written by a small
  `CurrencyPreferencesRepository` on the same `DataStore` instance.
- **Resolution**: pure `resolveCurrencySymbol(prefs, locale)` → effective
  symbol (`Currency.getInstance(locale).getSymbol(locale)` for Automatic,
  trimmed custom text for Custom, `""` when nothing resolves).
- **Placement**: pure `MoneyFormatter.affixes(symbol, locale)` → prefix /
  suffix taken from the locale's `DecimalFormat` currency pattern with the
  symbol substituted; `MoneyFormatter.format(amountText, affixes)` wraps an
  already-formatted number. No change to `ResultPresenter` rounding.
- **Plumbing**: `PriceGrabAppContent` resolves the symbol and provides it
  through a `LocalCurrencySymbol` `CompositionLocal` (default `""`), so
  `CompareScreen`'s public signature and the many test call sites stay
  unchanged.
- **UI**: price `OutlinedTextField` gets a `prefix` or `suffix` slot with
  the symbol; its content description appends the symbol. The savings line
  passes the wrapped amount into the existing `result_savings_per_*`
  templates (no template change). Settings gains a Currency section
  (Automatic / Custom radio rows + text field), following the Appearance
  section's patterns.

Ship via planning (**AK**) → implementation (**AL**) → shared release prep
with feature 009 (**AM**, 0.1.14).

## Technical Context

**Language/Version**: Kotlin 2.4.21 (built-in Kotlin in AGP 9.5.1), Gradle
9.9.0 — unchanged.

**Primary Dependencies**: None new. `java.util.Currency`,
`java.text.DecimalFormat` (ICU-backed on Android), DataStore Preferences
1.3.0 and Material 3 `OutlinedTextField` `prefix` / `suffix` slots already
in use.

**Storage**: DataStore file `appearance_preferences` (feature 007) gains
`currency_mode` (`"automatic"` | `"custom"`) and `currency_custom_symbol`
(string). Unknown / missing mode → Automatic. File already listed in
`backup_rules.xml` and `data_extraction_rules.xml`; no rule change.

**Testing**:

- JVM unit:
  - `CurrencyPreferencesTest`: defaults, storage mapping, trimming and
    4-character limit.
  - `CurrencyResolveTest`: Automatic for es-ES (€), en-US ($), en-GB (£),
    language-only locale (""); Custom trims, blank → "".
  - `MoneyFormatterTest`: es-ES suffix with space ("1 €"), en-US prefix
    ("$1"), custom `zł` in both, empty symbol → amount unchanged.
- Compose UI / instrumented:
  - New `CompareScreenCurrencyTest` (stateless `CompareScreen` inside
    `CompositionLocalProvider(LocalCurrencySymbol provides …)`): price
    fields show the symbol; savings line shows the wrapped amount; percent
    line has no symbol; price content description contains the symbol.
  - New `SettingsCurrencyTest`: Automatic selected by default and labelled
    with the resolved symbol; choosing Custom and typing `zł` persists and
    reaches Compare; length capped at 4; back to Automatic.
  - Update existing Activity-based savings assertions (`CompareScreenUnitsTest`,
    `CompareScreenSavingsTest`, `CompareScreenAccessibilityTest`,
    `CompareScreenMultiOfferTest`, …) to build the expected amount through a
    shared test helper that applies the same `MoneyFormatter` with the
    device's resolved symbol, since the real app now shows it.
  - `CompareScreenLargeFontTest`: 200% with a symbol provided — price field
    and amount visible.
  - `SettingsA11yTest` / `SettingsLargeFontTest`: Currency section labels
    and 200% layout.
- Manual: [quickstart.md](./quickstart.md).

**Target Platform**: Android, `minSdk` 24, `targetSdk` 35, `compileSdk` 37
— unchanged.

**Project Type**: mobile-app — unchanged.

**Performance Goals**: One DataStore read already happening at launch
(appearance); symbol resolution is O(1). Cold start ≤ 2 s unchanged.

**Constraints**:

- Offline, no permissions, no conversion (FR-010).
- Display-only: `OfferParser`, `PriceComparator`, `ResultPresenter`,
  `CompareViewModel` and saved-state keys untouched (FR-007).
- `core/**` stays free of Android imports; `MoneyFormatter` lives in
  `core/format`.
- F-Droid Mode B invariants untouched (no build config change).

**Scale/Scope**: ~150–250 LOC production (data + format + Settings section
+ Compare wiring) and ~300–400 LOC tests. No version bump in AL; release
prep **AM** ships 009 + 010 as **0.1.14** (versionCode 15).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle / section | Gate | Plan evidence | Status |
|---------------------|------|---------------|--------|
| I. Modern Mobile UX | M3, single-purpose flow, sensible defaults | Automatic default needs no setup; Settings section mirrors Appearance; symbol uses M3 text-field affix slots. | ✅ |
| II. Accessibility (NON-NEGOTIABLE) | TalkBack, 200% font, 48 dp targets | Price content description includes the symbol; result reads the visible line; Settings rows reuse the selectable-row semantics of Appearance; 200% tests on Compare and Settings. | ✅ |
| III. Internationalization (ES/EN) | Strings in resources, locale-aware formatting | New Settings strings in `values` + `values-es`; placement from the locale's currency pattern; numbers still via `LocaleNumberFormatter`. | ✅ |
| IV. Offline-First Performance | No network, ≤ 2 s cold start | Local DataStore keys only; no conversion; no new deps. | ✅ |
| V. Test-First Quality (NON-NEGOTIABLE) | Red → Green → Refactor; CI gates | JVM + instrumented tests written first per tasks.md; JaCoCo core gate covers `MoneyFormatter`. | ✅ |
| Privacy & Platform | No trackers; backup scope explicit | Preference lives in the already-allowlisted DataStore file; no new backup scope. | ✅ |

**Post-design re-check**: Passes. See [research.md](./research.md),
[data-model.md](./data-model.md),
[contracts/currency-display.md](./contracts/currency-display.md).

## Project Structure

### Documentation (this feature)

```text
specs/010-currency-symbol/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── currency-display.md
├── checklists/
│   └── requirements.md
├── spec.md
└── tasks.md
```

### Source Code (touch list)

```text
android/app/src/main/kotlin/com/mablanco/pricegrab/
├── core/format/MoneyFormatter.kt                 # NEW — affixes + wrap (pure)
├── data/currency/CurrencyPreferences.kt          # NEW — mode + custom symbol
├── data/currency/CurrencyPreferencesRepository.kt# NEW — same appearanceDataStore
├── data/currency/CurrencyResolve.kt              # NEW — effective symbol (pure)
├── data/appearance/AppearancePreferencesKeys.kt  # MODIFY — 2 keys (same file)
├── PriceGrabApp.kt                               # MODIFY — resolve + provide LocalCurrencySymbol
├── ui/settings/SettingsViewModel.kt              # MODIFY — expose/set currency
├── ui/settings/SettingsScreen.kt                 # MODIFY — Currency section
└── ui/compare/
    ├── CurrencyLocal.kt                          # NEW — LocalCurrencySymbol
    ├── CompareScreen.kt                          # MODIFY — price affix + cd
    └── CompareScreenResult.kt                    # MODIFY — wrap savings amount

android/app/src/main/res/values{,-es}/strings.xml # MODIFY — Settings copy

android/app/src/test/kotlin/com/mablanco/pricegrab/
├── core/format/MoneyFormatterTest.kt             # NEW
└── data/currency/{CurrencyPreferencesTest,CurrencyResolveTest}.kt  # NEW

android/app/src/androidTest/kotlin/com/mablanco/pricegrab/
├── ui/compare/CompareScreenCurrencyTest.kt       # NEW
├── ui/compare/CompareScreenTestHelpers.kt        # MODIFY — expected money helper
├── ui/compare/*Test.kt                           # MODIFY — savings expectations
└── ui/settings/SettingsCurrencyTest.kt           # NEW
```

**Structure Decision**: Single module. Currency keys share the existing
DataStore file (backup rules unchanged); the key object keeps its name to
avoid a storage migration, with a KDoc note that the file now holds all
user preferences.

## Complexity Tracking

> No constitution violations. Table left empty intentionally.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

## Implementation phases (preview for `/speckit.tasks`)

1. **Tests first (red)**: JVM tests for `MoneyFormatter`, preferences and
   resolution; instrumented Compare currency and Settings currency tests.
2. **Data + format**: keys, `CurrencyPreferences`, repository, resolver,
   `MoneyFormatter`.
3. **Compare (US1)**: `LocalCurrencySymbol`, provider in
   `PriceGrabAppContent`, price affix + content description, wrapped
   savings amount; update existing savings test expectations.
4. **Settings (US2)**: ViewModel API and Currency section with strings.
5. **A11y / large font (US3)** and regression: full suites, lint, detekt,
   JaCoCo; quickstart on device.
6. **Release prep (AM)**: joint 0.1.14 with 009 after `v0.1.13`.

## PR cadence

| Letter | Branch / PR | Contents |
|--------|-------------|----------|
| **AK** | `010-currency-symbol` (this branch) | Spec + plan + research + data-model + contracts + quickstart + tasks (planning only) |
| **AL** | `feat/0xx-currency-symbol-impl` | Implementation + tests |
| **AM** | `chore/0xx-fdroid-doc-sync-v0.1.14` | Release prep for 009 + 010 |

Feature 009's release letter moves from AK to **AM** (shared cut); 009
docs and the ledger are updated in this planning PR.
