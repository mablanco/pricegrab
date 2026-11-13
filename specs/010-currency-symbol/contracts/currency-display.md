# Contract: Currency Display

Pins behaviour for feature 010. Extends
[`specs/008-richer-result-card/contracts/result-savings-display.md`](../../008-richer-result-card/contracts/result-savings-display.md)
for the absolute savings amount; everything else there is unchanged.

## Pure API

```kotlin
// core/format/MoneyFormatter.kt (no Android imports)
data class MoneyAffixes(val prefix: String, val suffix: String)

object MoneyFormatter {
    fun affixes(symbol: String, locale: Locale): MoneyAffixes
    fun wrap(amount: String, affixes: MoneyAffixes): String
}

// data/currency/CurrencyResolve.kt
fun resolveCurrencySymbol(prefs: CurrencyPreferences, locale: Locale): String
```

| `symbol` | `locale` | `affixes` | `wrap("1", …)` |
|----------|----------|-----------|----------------|
| `€` | es-ES | `("", " €")` | `1 €` |
| `$` | en-US | `("$", "")` | `$1` |
| `zł` | es-ES | `("", " zł")` | `1 zł` |
| `zł` | en-US | `("zł", "")` | `zł1` |
| `""` | any | `("", "")` | `1` |

| prefs | locale | `resolveCurrencySymbol` |
|-------|--------|-------------------------|
| Automatic | es-ES | `€` |
| Automatic | en-US | `$` |
| Automatic | en-GB | `£` |
| Automatic | `en` (no region) | `""` |
| Custom `" zł "` | any | `zł` |
| Custom `""` / `"  "` | any | `""` |

## Compose

- `LocalCurrencySymbol: ProvidableCompositionLocal<String>`, default `""`.
- `PriceGrabAppContent` provides
  `resolveCurrencySymbol(currencyPrefs, LocalLocale.current.platformLocale)`.

## Compare screen

| Surface | With `€` in es-ES | With `$` in en-US | With `""` |
|---------|-------------------|-------------------|-----------|
| Price field | suffix `€` | prefix `$` | no affix (today) |
| Price content description | `Precio de Oferta A, €` | `Price for Offer A, $` | today's text |
| Quantity field | unchanged | unchanged | unchanged |
| Savings line (2.50/500 g vs 4.00/1 kg) | `Ahorras 1 € por kg frente a Oferta A` | `Save $1 per kg vs Offer A` | today's text |
| Percent line | `20% menos` | `20% less` | unchanged |
| Tie / empty / incompatible | no amount | no amount | unchanged |

The affix is never part of the editable text; `onPriceChange` receives
digits only, exactly as today.

## Settings screen

| Element | en | es |
|---------|----|----|
| Section title | Currency | Moneda |
| Automatic row | Automatic (%1$s) / Automatic (none) | Automática (%1$s) / Automática (ninguna) |
| Custom row | Custom | Personalizada |
| Field label | Symbol | Símbolo |
| Field supporting text | Leave empty to hide the symbol | Déjalo vacío para no mostrar símbolo |

- Rows are single-choice with the same selected / not selected state
  descriptions as Appearance.
- The field is enabled only when Custom is selected and accepts at most 4
  characters.
- Changes apply immediately and persist (DataStore).
