# Quickstart: Currency Symbol (Feature 010)

Manual checks after implementation. Automated coverage: JVM
`MoneyFormatterTest`, `CurrencyResolveTest`, `CurrencyPreferencesTest`;
instrumented `CompareScreenCurrencyTest`, `SettingsCurrencyTest` and the
updated Compare suites.

## Build & install

```bash
cd android
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Automatic default (US1 / SC-001)

1. Device in Spanish (Spain). Open Compare: price fields show `€` after
   the input.
2. 2.50 / 500 g vs 4.00 / 1 kg → "Ahorras 1 € por kg frente a Oferta A",
   "20% menos" (no symbol on percent).
3. Device in English (United States): price fields show `$` before the
   input; "Save $1 per kg vs Offer A".

## Custom symbol (US2 / SC-002)

1. Settings → Currency shows "Automática (€)" selected.
2. Select Personalizada, type `zł`. Back to Compare: `zł` on price fields
   and savings line.
3. Try typing `ABCDE`: stops at `ABCD`.
4. Clear the field: no symbol anywhere.
5. Kill and relaunch the app: the last choice persists.
6. Select Automática: `€` returns; Personalizada still remembers the text.

## Accessibility and large font (US3 / SC-004)

1. TalkBack: price field reads "Precio de Oferta A, euro" (or the symbol);
   result reads the savings line with the currency.
2. Settings Currency rows announce selected / not selected; the field has
   its label.
3. Font 200% with custom `CHF`: price field shows symbol and the typed
   amount without clipping; Settings section readable.

## Regression (SC-003)

- Same inputs give the same winner, savings figure and percent as 0.1.13 /
  feature 009, only with the symbol added (g/kg, ml/L, pcs, cm/m, three
  offers, free offer, tie).
- Reset / undo, rotation and theme settings unchanged.
