# Quickstart: Settings Appearance (Feature 007)

Manual checks after implementation. Automated coverage lives in JVM
preference/resolution tests and Compose instrumented Settings tests.

## Build & install

```bash
cd android
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

## Fresh install defaults (SC-001)

1. Clear app data or install fresh.
2. Launch PriceGrab → Compare appears (not Settings).
3. Confirm appearance follows the **device** light/dark setting.
4. Confirm colors match the **brand** steel-blue palette (not wallpaper).

## Theme System / Light / Dark (US1)

1. From Compare, open Settings (top-bar affordance).
2. Select **Light** → app is light even if the device is dark; no restart.
3. Select **Dark** → app is dark even if the device is light.
4. Select **System** → app tracks the device again.
5. Enter prices on Compare → open Settings → change theme → back →
   values still present (FR-012 / SC-004).
6. Force-stop → relaunch → last theme choice still applied (SC-003).

## Material You (US2)

### Capable device (API 31+)

1. Settings → Material You **off** by default (brand palette).
2. Enable Material You → accents/surfaces follow wallpaper; theme mode
   still respected.
3. Disable → brand palette returns immediately.
4. Force-stop → relaunch → Material You remains on if left on.
5. Optional: change wallpaper, return to app → palette updates when the
   platform supplies a new dynamic scheme.

### Non-capable device (API &lt; 31) or emulator image without dynamic color

1. Settings → Material You control is disabled / unavailable with clear
   copy.
2. Brand palette remains; cannot enable the switch (SC-006).

## Accessibility & i18n (US3 / SC-007)

1. TalkBack: open Settings; each theme option and Material You announce
   name + state.
2. System font → 200%: Settings readable, tappable, no overlap/truncation.
3. Device language `es` and `en`: all Settings labels translated; no
   hardcoded English on `es`.

## Contrast smoke (SC-008)

1. Brand light + brand dark: primary text / controls remain readable
   (spot-check vs 003 baseline).
2. With Material You on: one light and one dark pass on a reference
   wallpaper (non-extreme).

## Regression smoke

- Two- and three-offer compare, units, reset/undo, savings copy unchanged.
- Locale `es-ES`: decimal comma still accepted on Compare.
- Cold start still feels instant (no network; no long splash for prefs).
