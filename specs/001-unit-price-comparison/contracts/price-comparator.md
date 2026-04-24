# Contract: `PriceComparator`

The app's only external-facing "interface" is the calculation module that the
UI calls. F-Droid, in-app tests, and any future non-Android front-end (e.g.,
CLI) rely on this module behaving deterministically. This document pins its
contract.

## Location

```
android/app/src/main/kotlin/com/mablanco/pricegrab/core/calc/PriceComparator.kt
```

Pure Kotlin. No Android, Compose, or `android.*` imports. JVM-testable.

## Signature

```kotlin
object PriceComparator {
    fun compare(a: Offer, b: Offer): ComparisonOutcome
}
```

Where `Offer` and `ComparisonOutcome` are defined in
[`data-model.md`](../data-model.md).

## Preconditions

The caller MUST supply two **valid** `Offer` instances:

- `price.signum() >= 0` for both offers.
- `quantity.signum() > 0` for both offers.

Violating the preconditions is a programming error. The function MUST NOT
try to "fix" invalid input and MUST throw `IllegalArgumentException` with a
clear message naming which field violated the invariant. It MUST NOT return
a fake `Tie` or similar.

## Postconditions

- Returns `ComparisonOutcome.Tie` iff `a.unitPrice == b.unitPrice`
  (compared with `BigDecimal.compareTo`, i.e. value-based, not scale-based).
- Returns `AWins(perUnitDelta, percentDelta)` iff `a.unitPrice < b.unitPrice`.
- Returns `BWins(perUnitDelta, percentDelta)` iff `b.unitPrice < a.unitPrice`.

Payload semantics:

- `perUnitDelta = (a.unitPrice - b.unitPrice).abs()`; always `≥ 0`.
- `percentDelta = perUnitDelta.divide(max(a.unitPrice, b.unitPrice), MathContext.DECIMAL64) * BigDecimal.valueOf(100)`.
- When `max(a.unitPrice, b.unitPrice) == 0` (both offers free):
  returned as `Tie`; never `AWins` / `BWins`.
- When exactly one `unitPrice` is zero:
  the zero side wins; `perUnitDelta == other.unitPrice`;
  `percentDelta == 100`.

## Invariants

- **Purity**: same inputs → same outputs. No I/O, no clock, no RNG, no
  system property reads.
- **Thread-safety**: reentrant; may be called from any thread.
- **No mutation**: inputs MUST NOT be mutated. `Offer` is immutable anyway.
- **No rounding loss in the winner decision**: the winner is decided on
  full `MathContext.DECIMAL64` precision, not on display precision.

## Required unit tests (canonical suite)

These are the tests without which the calculator MUST NOT ship. The real
test class holds at least these cases, plus whatever additional cases
emerge during TDD. Each case specifies inputs as
`(priceA, quantityA, priceB, quantityB)` and the expected outcome shape.

| # | Inputs                              | Expected outcome                                    | Purpose                                                  |
|---|-------------------------------------|-----------------------------------------------------|----------------------------------------------------------|
| 1 | `2.50 / 500, 4.00 / 1000`           | `BWins(Δ=0.001, pct=20)`                            | Canonical "B cheaper" happy path                         |
| 2 | `3.00 / 1, 5.00 / 2`                | `BWins(Δ=0.5, pct≈16.666…)`                         | Non-round percent, verifies `DECIMAL64` precision        |
| 3 | `2.00 / 100, 4.00 / 200`            | `Tie`                                               | Equal unit prices, different absolute scales             |
| 4 | `0 / 5, 1 / 5`                      | `AWins(Δ=0.2, pct=100)`                             | One offer is free                                        |
| 5 | `0 / 5, 0 / 10`                     | `Tie`                                               | Both offers free                                         |
| 6 | `1.00 / 0.5, 1.00 / 0.25`           | `BWins(Δ=2.0, pct=50)`                              | Fractional quantities                                    |
| 7 | `1.00 / 3, 1.00 / 3`                | `Tie`                                               | Same input values                                        |
| 8 | Very small: `0.01 / 1_000_000, 0.01 / 999_999` | `AWins(...)`                                        | Verifies no underflow; winner decided at full precision  |
| 9 | Very large: `999_999_999 / 1, 1_000_000_000 / 1` | `AWins(Δ=1, pct≈0.0000001)`                         | Verifies no overflow                                     |
| 10| Precondition violation: negative price   | throws `IllegalArgumentException`                   | Guard clause                                             |
| 11| Precondition violation: zero quantity    | throws `IllegalArgumentException`                   | Guard clause                                             |

## Coverage gate

Line coverage on `core/calc/**` MUST be ≥ 90% and MUST be enforced in CI
(constitutional Principle V). The canonical suite above is sufficient to
reach that threshold; property-based tests are welcome additions but not a
hard requirement.
