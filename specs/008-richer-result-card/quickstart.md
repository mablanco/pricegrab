# Quickstart: Richer Result Card (Feature 008)

Manual checks after implementation. Automated coverage lives in
`CompareScreenSavingsTest` (+ a11y) and existing Compare suites.

## Build & install

```bash
cd android
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

## Unique winner — absolute + percent visible (US1 / SC-001)

1. Launch Compare (fresh or reset).
2. Offer A: price `2.50`, quantity `500`, unit **g**.
3. Offer B: price `4.00`, quantity `1`, unit **kg**.
4. Expect: Offer B cheaper; absolute savings vs Offer A **and** a visible
   **20%** (or locale `20`) percent line on the hero.
5. Add Offer C more expensive than both → winner/savings vs second-cheapest
   still show both absolute and percent.

## Free offer — 100% (edge)

1. Offer A: `0` / `100` g; Offer B: `5.00` / `100` g.
2. Expect: Offer A wins; absolute savings visible; percent shows **100**.

## Quiet states (US2)

1. Equal unit prices → tied message; **no** absolute or percent lines.
2. Clear / incomplete fields → placeholder; no savings lines.
3. Mass vs volume units → incompatible message; no savings lines.

## Accessibility & large text (US3)

1. TalkBack: focus result region on a winner → announcement includes
   winner, absolute savings, and percent.
2. System font 200%: both savings lines fully readable (wrap OK; no
   clipped meaning) with two and three offers.
3. Device language `es` and `en`: percent wording translated; numerals
   use locale separators (e.g. `16,7` in `es-ES` when applicable).

## Appearance regression (007)

1. Settings → Light / Dark / System: hero text remains readable.
2. Material You on/off (if API 31+): savings lines still contrast
   adequately (spot-check).

## Compare regression smoke

- Units, add/remove third offer, reset/undo unchanged.
- Locale `es-ES`: decimal comma still accepted on inputs.
- Cold start still feels instant; no new permissions or network.
