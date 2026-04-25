# Capturas de pantalla del teléfono (es-ES)

F-Droid espera entre **2 y 8** capturas en PNG o JPEG en este directorio,
con nombres como `01_compare.png`, `02_savings.png`, …

Aún no están commiteadas (T054 en
[`specs/001-unit-price-comparison/tasks.md`](../../../../../../../specs/001-unit-price-comparison/tasks.md)).
Para capturarlas hace falta un dispositivo o emulador:

```bash
cd android
./gradlew :app:installDebug
adb shell setprop persist.sys.locale es-ES
adb shell screencap -p /sdcard/01_compare.png
adb pull /sdcard/01_compare.png \
    fastlane/metadata/android/es-ES/images/phoneScreenshots/01_compare.png
```
