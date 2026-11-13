# Quickstart: Length Units (Feature 009)

Manual checks after implementation. Automated coverage lives in the JVM
unit suites and `CompareScreenUnitsTest`, `CompareScreenIncompatibleUnitsTest`,
`CompareScreenAccessibilityTest` and the large-font tests.

## Build & install

```bash
cd android
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Issue #56 scenario (US1 / SC-001)

1. Launch Compare (fresh or after Reset). Both units show **g**.
2. Open Offer A's unit selector: order is g, kg, ml, L, **cm**, **m**, pcs.
3. Offer A: price `49`, quantity `5`, unit **m**.
4. Offer B: price `22`, quantity `250`, unit **cm**.
5. Expect: **Offer B** cheaper; "Save 1 per m vs Offer A"; "10.2% less".
6. Switch the device to Spanish and repeat: "Ahorras 1 por m frente a
   Oferta A"; "10,2% menos".

## Same unit, three offers (US1)

1. Offers: `20` / `200` cm, `27` / `300` cm, `12` / `1` m.
2. Expect: Offer B wins (9/m), savings vs Offer A (10/m) shows 1 per m.

## Block mixed measures (US2 / SC-002)

1. Offer A **m**, Offer B **kg** (then **L**, then **pcs**), valid numbers.
2. Expect: no winner; message mentions "length" / "longitud".
3. Change Offer B to **cm**: error clears and a result appears.

## Reset / undo / rotation (edge cases)

1. Set Offer A to **m**, rotate the device: still **m**.
2. Tap Reset: both units back to **g**. Tap Undo: Offer A back to **m**.

## Accessibility (US3 / SC-003)

1. TalkBack on. Open the unit selector, move over the new options: hears
   "centimetres", "metres" (es: "centímetros", "metros").
2. Complete the issue #56 scenario: the result announcement includes the
   savings line with "per m" / "por m" and is intelligible.
3. System font scale 200%: an offer with **cm** and one with **m** — codes
   are not clipped; row layout behaves as with kg.

## Regression

- Existing g/kg, ml/L and pcs scenarios from features 004 and 008 still
  show per kg / per L / per piece.
