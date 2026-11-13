# Contract: Length Units

Pins behaviour for feature 009. **Extends**
[`specs/004-quantity-units/contracts/quantity-units.md`](../../004-quantity-units/contracts/quantity-units.md);
every rule there applies to the new units unless overridden here.

## Locations

```
android/app/src/main/kotlin/com/mablanco/pricegrab/core/model/Dimension.kt
android/app/src/main/kotlin/com/mablanco/pricegrab/core/model/QuantityUnit.kt
android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenUnits.kt
android/app/src/main/kotlin/com/mablanco/pricegrab/ui/compare/CompareScreenResult.kt
android/app/src/main/res/values/strings.xml
android/app/src/main/res/values-es/strings.xml
```

No Android imports in `core/**` (unchanged rule).

## Model

```kotlin
enum class Dimension(val displayUnitScale: BigDecimal) {
    Mass(BigDecimal("1000")),
    Volume(BigDecimal("1000")),
    Length(BigDecimal("100")),
    Count(BigDecimal.ONE),
}

enum class QuantityUnit {
    Gram, Kilogram, Millilitre, Litre, Centimetre, Metre, Piece;
    // Centimetre.dimension == Length, toBaseMultiplier == 1
    // Metre.dimension      == Length, toBaseMultiplier == 100
}
```

- `QuantityUnit.entries.map { it.name }` MUST equal
  `[Gram, Kilogram, Millilitre, Litre, Centimetre, Metre, Piece]`.
- `QuantityUnit.DEFAULT == Gram`.

## Comparison

| Offers | Gate | Outcome |
|--------|------|---------|
| A = 49 / 5 **m**, B = 22 / 250 **cm** | Ready | B wins; display delta **1** per m; percent **10.2** |
| A = 49 / 5 **m**, B = 49 / 500 **cm** | Ready | Tie |
| A = 20 / 200 **cm**, B = 27 / 300 **cm** | Ready | B wins (0.09/cm vs 0.10/cm); delta **1** per m |
| any **cm/m** + any of **g, kg, ml, L, pcs** | IncompatibleUnits | No winner, no savings |
| three offers, all length | Ready | Cheapest per cm wins; savings vs second-cheapest |

## Result copy

| Dimension | String key | en | es |
|-----------|-----------|----|----|
| Length | `result_savings_per_m` | `Save %1$s per m vs %2$s` | `Ahorras %1$s por m frente a %2$s` |

Canonical rendering (en-US): "Save 1 per m vs Offer A" + "10.2% less".
(es-ES): "Ahorras 1 por m frente a Oferta A" + "10,2% menos".

The live-region summary is `headline. savingsLine. percentLine` (feature
008), so TalkBack reads "per m" / "por m" from the visible line.

## Selector copy and semantics

| Unit | Visible code (en / es) | Option content description (en / es) |
|------|------------------------|--------------------------------------|
| Centimetre | `cm` / `cm` | `centimetres` / `centímetros` |
| Metre | `m` / `m` | `metres` / `metros` |

## Incompatible-units message

| Locale | `error_incompatible_units` |
|--------|----------------------------|
| en | Both offers must use the same kind of measure (weight, volume, length, or count). |
| es | Ambas ofertas deben usar el mismo tipo de medida (peso, volumen, longitud o unidades). |

## Saved state, reset, undo

- Selecting `cm` / `m` survives rotation and process death (stored as
  enum name).
- Reset sets every unit to `Gram`; undo restores `Centimetre` / `Metre`
  if they were selected before reset.
