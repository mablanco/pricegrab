# Phone screenshots (en-US)

Three screenshots F-Droid shows on the app listing:

| File              | State                                         |
|-------------------|-----------------------------------------------|
| `01_empty.png`    | Initial empty form.                           |
| `02_winner.png`   | Comparison with winner and percent savings.   |
| `03_tie.png`      | Tie (both per-unit prices identical).         |

Captured on a Motorola moto g71 5G running Android 12 (1080×2400,
420 dpi) against the signed `v0.1.0` APK.

## How to regenerate

```bash
# 1. Install the signed APK for the version you want to document
adb install -r app-release.apk

# 2. Move English to the top of the system locale list
adb shell am start -a android.settings.LOCALE_SETTINGS
# (drag "English (United States)" or "English (United Kingdom)" to the top)

# 3. Launch the app and fill the form manually for each state
adb shell am start -n com.mablanco.pricegrab/.MainActivity

# 4. Capture
adb exec-out screencap -p \
    > android/fastlane/metadata/android/en-US/images/phoneScreenshots/01_empty.png
```

The English captures use US-style decimals: empty, `1.99 / 500` vs
`3.49 / 1000` (B wins) and `2.00 / 500` vs `4.00 / 1000` (tie). Close
the keyboard before each `screencap`.

Mirror the same set in Spanish under `../../../es-ES/images/phoneScreenshots/`.
