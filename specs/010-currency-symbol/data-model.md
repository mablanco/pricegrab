# Data Model: Currency Symbol (Feature 010)

## `CurrencyMode` (enum, `data/currency/CurrencyPreferences.kt`)

| Value | Storage | Meaning |
|-------|---------|---------|
| `Automatic` | `"automatic"` | Symbol from the device locale's currency |
| `Custom` | `"custom"` | Symbol typed by the shopper |

Missing / unknown storage value → `Automatic`.

## `CurrencyPreferences` (data class)

| Field | Type | Default | Rules |
|-------|------|---------|-------|
| `mode` | `CurrencyMode` | `Automatic` | — |
| `customSymbol` | `String` | `""` | Stored as typed, capped at `MAX_CUSTOM_SYMBOL_LENGTH = 4` chars; kept while in Automatic so switching back restores it |

## DataStore keys (`appearance_preferences` file)

| Key | Type | Values |
|-----|------|--------|
| `currency_mode` | string | `automatic` / `custom` |
| `currency_custom_symbol` | string | 0–4 chars |

Existing keys `theme_mode`, `material_you_enabled` unchanged.

## Effective symbol (derived, not stored)

`resolveCurrencySymbol(prefs, locale): String`

| Mode | Result |
|------|--------|
| Automatic | `Currency.getInstance(locale).getSymbol(locale)`; `""` on failure or `XXX` |
| Custom | `customSymbol.trim()` (may be `""`) |

## `MoneyAffixes` (derived, `core/format/MoneyFormatter.kt`)

| Field | Type | Example es-ES / € | Example en-US / $ |
|-------|------|-------------------|-------------------|
| `prefix` | String | `""` | `"$"` |
| `suffix` | String | `" €"` | `""` |

`MoneyFormatter.affixes("", locale)` → both empty.
`MoneyFormatter.wrap(amount, affixes) = prefix + amount + suffix`.

## Unchanged

`Offer`, `QuantityUnit`, `ComparisonOutcome`, `CompareUiState`,
`OfferSlotState`, saved-state keys, `SavingsPresentation`.
