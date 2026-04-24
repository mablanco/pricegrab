# Data Model: Unit-Price Comparison Screen (Feature 001)

The feature is stateless across app launches. There is no persistence layer,
no database, no network model. The "data model" below is the in-memory
domain model consumed by the calculation core and produced for the UI.

All types live in `com.mablanco.pricegrab.core.model` (framework-agnostic,
pure Kotlin, JVM-testable).

## `Offer`

A single side of the comparison (A or B).

**Fields**

| Field      | Type         | Required | Validation                                                                 |
|------------|--------------|----------|----------------------------------------------------------------------------|
| `price`    | `BigDecimal` | yes      | `price.signum() >= 0` (zero is legal — free item; negative is rejected)    |
| `quantity` | `BigDecimal` | yes      | `quantity.signum() > 0` (strictly positive — zero triggers validation error) |

**Derived**

| Derived        | Type         | Definition                                                   |
|----------------|--------------|--------------------------------------------------------------|
| `unitPrice`    | `BigDecimal` | `price.divide(quantity, MathContext.DECIMAL64)`              |

Defined only when the `Offer` is valid (both fields pass validation).

**Invariants**

- `Offer` is immutable.
- `unitPrice` is deterministic and pure (same inputs → same output, no I/O).
- `price == 0` with `quantity > 0` yields `unitPrice == 0`.

**Factory**

```kotlin
sealed interface OfferParseResult {
    data class Success(val offer: Offer) : OfferParseResult
    data class InvalidPrice(val raw: String) : OfferParseResult
    data class InvalidQuantity(val raw: String) : OfferParseResult
    data object NegativePrice : OfferParseResult
    data object NonPositiveQuantity : OfferParseResult
}
```

The UI converts raw user strings to `Offer` via a `LocaleNumberFormatter`
plus an `OfferParser`; the parser returns `OfferParseResult` and the UI
renders the specific error next to the offending field.

## `ComparisonOutcome` (sealed)

Produced by `PriceComparator.compare(a: Offer, b: Offer)` when **both**
offers are valid. If either offer is invalid, the caller (ViewModel) does
**not** invoke `compare` and surfaces the validation error instead.

**Variants**

| Variant                          | Meaning                                            | Payload                                                                 |
|----------------------------------|----------------------------------------------------|-------------------------------------------------------------------------|
| `Tie`                            | `a.unitPrice == b.unitPrice` at full precision     | none                                                                    |
| `AWins(perUnitDelta, percentDelta)` | `a.unitPrice < b.unitPrice`                        | `perUnitDelta: BigDecimal (≥ 0)`, `percentDelta: BigDecimal (0…100)`   |
| `BWins(perUnitDelta, percentDelta)` | `b.unitPrice < a.unitPrice`                        | `perUnitDelta: BigDecimal (≥ 0)`, `percentDelta: BigDecimal (0…100)`   |

**Definitions of payload fields**

- `perUnitDelta = |a.unitPrice - b.unitPrice|` — always positive.
- `percentDelta = perUnitDelta / max(a.unitPrice, b.unitPrice) * 100` — the
  savings expressed against the more expensive option (so "B is 20% cheaper"
  means a 20% reduction relative to A's per-unit price when A is the loser).

**Edge refinement**

- If both `unitPrice` values are zero, the result is `Tie` (both are free).
- If one `unitPrice` is zero and the other is positive, the zero-price offer
  wins with `percentDelta == 100`.

## UI State (`CompareUiState`)

Lives in `com.mablanco.pricegrab.ui.compare` but documented here because it
is the data contract between `CompareViewModel` and `CompareScreen`.

```kotlin
data class CompareUiState(
    val priceARaw: String = "",
    val quantityARaw: String = "",
    val priceBRaw: String = "",
    val quantityBRaw: String = "",

    val priceAError: InputError? = null,
    val quantityAError: InputError? = null,
    val priceBError: InputError? = null,
    val quantityBError: InputError? = null,

    val outcome: ComparisonOutcome? = null,  // null until all inputs valid
)

enum class InputError {
    Empty,
    NotANumber,
    Negative,
    ZeroQuantity,   // only applies to the two quantity fields
    OutOfRange,
}
```

**State transitions**

1. User edits any `*Raw` field → ViewModel re-parses **all four** fields
   (cheap operation) → updates `*Error` flags → if every field is valid,
   calls `PriceComparator.compare` and sets `outcome`; otherwise sets
   `outcome = null`.
2. Configuration change → `SavedStateHandle` restores the four `*Raw`
   strings → ViewModel re-runs step 1 deterministically.
3. App process death → same as (2), via `SavedStateHandle`.

**Concurrency**

Computation is O(1), stays on the main thread, never blocks. No coroutines
are required for this feature.

## What this model is NOT

- **Not persisted.** The app never writes to disk or to a database.
- **Not networked.** No DTOs, no JSON, no serialization.
- **Not multi-user.** No accounts, no ownership, no ACLs.
- **Not historical.** No list of past comparisons in v1.

Any of the above becoming in-scope would be a future feature with its own
spec and its own data-model document.
