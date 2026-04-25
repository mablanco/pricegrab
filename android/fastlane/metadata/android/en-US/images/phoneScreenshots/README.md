# Phone screenshots (en-US)

F-Droid expects between **2 and 8** PNG / JPEG phone screenshots in this
directory, named like `01_compare.png`, `02_savings.png`, …

The files are not committed yet (T054 in
[`specs/001-unit-price-comparison/tasks.md`](../../../../../../../specs/001-unit-price-comparison/tasks.md)).
Capturing them needs a real or emulated device:

```bash
cd android
./gradlew :app:installDebug
adb shell screencap -p /sdcard/01_compare.png
adb pull /sdcard/01_compare.png \
    fastlane/metadata/android/en-US/images/phoneScreenshots/01_compare.png
```

Do the same set in Spanish and drop the files in
`../../../es-ES/images/phoneScreenshots/`.
