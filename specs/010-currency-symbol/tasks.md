---
description: "Task list for feature 010 — currency symbol"
---

# Tasks: Currency Symbol

**Input**: Design documents from `/specs/010-currency-symbol/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md,
contracts/currency-display.md, quickstart.md

**Tests**: REQUIRED. Constitution Principle V: Red → Green → Refactor.

**Organization**: Tasks grouped by user story from `spec.md`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: parallelizable (different files, no dependencies on incomplete
  tasks).
- **[Story]**: `[US1]` / `[US2]` / `[US3]`.
- Path abbreviations: `main/…` = `android/app/src/main/kotlin/com/mablanco/pricegrab/…`,
  `test/…` = `android/app/src/test/kotlin/com/mablanco/pricegrab/…`,
  `androidTest/…` = `android/app/src/androidTest/kotlin/com/mablanco/pricegrab/…`,
  `res/…` = `android/app/src/main/res/…`.

---

## Phase 1: Setup (Shared infrastructure)

- [x] T001 [P] Add PR **AK** entry to
      `specs/001-unit-price-comparison/tasks.md`; move feature 009's
      release letter to **AM** (shared cut) — done in planning commit.
- [x] T002 [P] Point `docs/project-status.md`, `.specify/feature.json` and
      `.cursor/rules/specify-rules.mdc` at `specs/010-currency-symbol`;
      add the 010 cadence row — done in planning commit.

---

## Phase 2: Foundational (pure format + preference model)

**Purpose**: Pure building blocks every story uses.

- [x] T003 [P] Write failing `test/core/format/MoneyFormatterTest.kt`
      covering every row of the `affixes` / `wrap` table in
      `contracts/currency-display.md` (es-ES, en-US, custom `zł`, empty).
- [x] T004 [P] Write failing `test/data/currency/CurrencyPreferencesTest.kt`:
      defaults (Automatic, `""`), `CurrencyMode` storage round-trip and
      unknown → Automatic, custom symbol capped at 4.
- [x] T005 [P] Write failing `test/data/currency/CurrencyResolveTest.kt`
      covering the `resolveCurrencySymbol` table (es-ES €, en-US $, en-GB
      £, `en` → `""`, custom trimmed, blank → `""`).
- [x] T006 [P] Implement `main/core/format/MoneyFormatter.kt`
      (`MoneyAffixes`, `affixes`, `wrap`) using the locale's currency
      `DecimalFormat` with the symbol substituted (research §2).
- [x] T007 [P] Implement `main/data/currency/CurrencyPreferences.kt`
      (`CurrencyMode`, `CurrencyPreferences`, `MAX_CUSTOM_SYMBOL_LENGTH`)
      and `main/data/currency/CurrencyResolve.kt`.
- [x] T008 Add `CURRENCY_MODE` / `CURRENCY_CUSTOM_SYMBOL` keys to
      `main/data/appearance/AppearancePreferencesKeys.kt` (KDoc: the file
      holds all user preferences) and implement
      `main/data/currency/CurrencyPreferencesRepository.kt` over
      `Context.appearanceDataStore` (`preferences` flow, `setMode`,
      `setCustomSymbol`).

**Checkpoint**: T003–T005 green.

---

## Phase 3: User Story 1 — Money amounts carry a symbol (P1) 🎯 MVP

**Independent Test**: es-ES fresh install → `€` on price fields and
"Ahorras 1 € por kg frente a Oferta A"; en-US → `$`.

### Tests (write first)

- [x] T009 [P] [US1] Create `androidTest/ui/compare/CompareScreenCurrencyTest.kt`
      rendering stateless `CompareScreen` inside
      `CompositionLocalProvider(LocalCurrencySymbol provides "€")`: price
      fields show the symbol; winner state shows the wrapped savings amount;
      percent line has no symbol; quantity fields have no symbol; empty
      symbol renders as today.
- [x] T010 [P] [US1] Add `expectedMoney(amount: String)` to
      `androidTest/ui/compare/CompareScreenTestHelpers.kt` (resolver +
      formatter with default prefs and the activity locale) and use it in
      every Activity-based `result_savings_per_*` assertion
      (`CompareScreenUnitsTest`, `CompareScreenSavingsTest`,
      `CompareScreenAccessibilityTest`, `CompareScreenMultiOfferTest`, and
      any other hit of `grep -rn result_savings_per androidTest`).

### Implementation

- [x] T011 [US1] Create `main/ui/compare/CurrencyLocal.kt` with
      `LocalCurrencySymbol = staticCompositionLocalOf { "" }`.
- [x] T012 [US1] In `main/PriceGrabApp.kt`, collect currency preferences
      (via `SettingsViewModel`, T016), resolve with the current
      `LocalLocale.current.platformLocale`, and provide
      `LocalCurrencySymbol` around the screen `when`.
- [x] T013 [US1] In `main/ui/compare/CompareScreen.kt`, pass the affixes to
      the price `LabeledNumberField` (prefix or suffix `Text`) in both
      `CompactOfferFieldsRow` and `AdaptiveOfferFields`; append the symbol
      to `priceCd` when non-empty.
- [x] T014 [US1] In `main/ui/compare/CompareScreenResult.kt`, wrap
      `savings.perUnitDelta` with the affixes before `formatSavingsLine`.

**Checkpoint**: US1 tests green; existing Compare suites green with
updated expectations.

---

## Phase 4: User Story 2 — Custom symbol in Settings (P1)

**Independent Test**: Settings → Custom `zł` → Compare shows `zł`;
persists across restart; Automatic restores region symbol.

### Tests (write first)

- [x] T015 [P] [US2] Create `androidTest/ui/settings/SettingsCurrencyTest.kt`
      (clear DataStore before each test like the existing Settings tests):
      Automatic selected and labelled with the resolved symbol; select
      Custom, type `zł`, go back → Compare price field shows `zł`; typing 5
      characters keeps 4; empty custom → no symbol; Automatic again →
      region symbol.

### Implementation

- [x] T016 [US2] Extend `main/ui/settings/SettingsViewModel.kt` with
      `currencyPreferences: StateFlow<CurrencyPreferences>`,
      `setCurrencyMode`, `setCustomCurrencySymbol` (capped at 4).
- [x] T017 [P] [US2] Add EN strings in `res/values/strings.xml`:
      `settings_currency_section`, `settings_currency_automatic`
      (`Automatic (%1$s)`), `settings_currency_automatic_none`,
      `settings_currency_custom`, `settings_currency_symbol_label`,
      `settings_currency_symbol_hint`.
- [x] T018 [P] [US2] Add ES equivalents in `res/values-es/strings.xml`
      (contract table).
- [x] T019 [US2] Add the Currency section to
      `main/ui/settings/SettingsScreen.kt` (+ `PriceGrabAppContent`
      parameters): two selectable rows reusing the Appearance row pattern
      and an `OutlinedTextField` enabled only for Custom.

**Checkpoint**: US1 + US2 green.

---

## Phase 5: User Story 3 — Accessibility and large font (P2)

- [x] T020 [P] [US3] *(Covered in `CompareScreenCurrencyTest.kt` instead of
      editing the shared layout tests.)* Extend `androidTest/ui/compare/CompareScreenLargeFontTest.kt`
      and `CompareScreenCompactRowTest.kt` with a 4-character symbol
      (`CHF`): price field and typed amount visible; compact row geometry
      holds at default scale.
- [x] T021 [P] [US3] Extend `androidTest/ui/settings/SettingsA11yTest.kt`
      and `SettingsLargeFontTest.kt` for the Currency rows (selected state,
      labels, 200% layout).
- [x] T022 [US3] *(None needed so far; re-check after CI.)* Fix any layout or semantics gaps found by T020 / T021.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [x] T023 [P] Add a Compare preview with `€` in
      `main/ui/compare/CompareScreenPreviews.kt` and update the
      `PriceGrabAppPreview` parameters.
- [ ] T024 Full gate from `android/`: `lintDebug`, `detekt`,
      `testDebugUnitTest`, `jacocoTestReport`, `connectedDebugAndroidTest`.
- [ ] T025 Manual [quickstart.md](./quickstart.md) on Marco's device.
- [ ] T026 Release prep **AM** (with 009): 0.1.14 / versionCode 15,
      changelogs `15.txt` en/es mentioning length units and currency,
      `docs/fdroid.md`, `docs/project-status.md`, ledger AL / AM.
- [ ] T027 After `v0.1.14`: comment on issues #56 and #57 and close them.

---

## Dependencies & Execution Order

- Phase 2 blocks all stories. T006–T008 after their red tests T003–T005.
- US1 needs T016's `currencyPreferences` flow for T012; implement T016
  first if working sequentially (or provide a default and wire it in US2).
- US2 depends on Phase 2 and T011 / T012.
- US3 after US1 + US2.
- T026 / T027 happen in later PRs.

## Parallel Example

```text
T003 MoneyFormatterTest   T004 CurrencyPreferencesTest   T005 CurrencyResolveTest
T017 values/strings.xml   T018 values-es/strings.xml
```

## Implementation Strategy

1. MVP: Phase 2 + US1 with Automatic only (region symbol everywhere).
2. Same PR (AL): US2 Settings, US3 a11y / large font, polish.
3. Release prep AM together with 009.

## Notes

- Total 27 tasks (2 done in planning). US1: 6, US2: 5, US3: 3.
- Display-only: if any task seems to need changes in `OfferParser`,
  `PriceComparator`, `ResultPresenter` or `CompareViewModel`, stop and
  re-check the plan.
