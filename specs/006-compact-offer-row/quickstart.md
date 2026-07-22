# Quickstart: Compact Offer Input Row (Feature 006)

Manual checks after implementation. Automated coverage lives in Compose
instrumented tests (default-scale row geometry + `CompareScreenLargeFontTest`
at `fontScale = 2f`).

## Build & install

```bash
cd android
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

## Visual — default font scale

1. Cold launch (system font default).
2. Confirm Offer A and B each show **price | quantity | unit** on one row
   under the title (not a full-width price above a second row).
3. Enter `2.50` / `500` g and `4.00` / `1` kg → winner/savings unchanged.
4. Tap `+` → Offer C uses the same single-row layout.
5. On a typical phone, two-offer flow should reach the result with little
   or no scrolling.

## Visual — 200% font

1. Settings → Accessibility → Font size → largest (200%), or rely on the
   instrumented `fontScale = 2f` test during CI.
2. Open PriceGrab: each offer uses **two rows** (price; then quantity +
   unit). No clipped labels, no overlapping controls.
3. Complete a two-offer compare; result readable.

## Accessibility

1. TalkBack: focus price, quantity, unit on Offer A — each names the offer.
2. Trigger an invalid quantity → error text visible and announced.
3. Confirm `+` / `−` and result live region still make sense with denser
   cards.

## Regression smoke

- Reset → two empty Gram cards; Undo restores values (and slot count).
- g vs ml → incompatible-units error (no winner).
- Locale `es-ES`: decimal comma still accepted; strings intact.
