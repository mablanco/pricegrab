# Capturas de pantalla del teléfono (es-ES)

Tres capturas que F-Droid usa en la ficha de la app:

| Archivo            | Estado                                          |
|--------------------|-------------------------------------------------|
| `01_empty.png`     | Pantalla inicial vacía.                         |
| `02_winner.png`    | Comparación con ganador y porcentaje de ahorro. |
| `03_tie.png`       | Empate (mismo precio por unidad).               |

Capturadas en un Motorola moto g71 5G con Android 12 (1080×2400, 420 dpi)
sobre el APK firmado de `v0.1.0`.

## Cómo regenerarlas

```bash
# 1. Instala el APK firmado de la versión que quieras documentar
adb install -r app-release.apk

# 2. Asegúrate de que el sistema está en español
adb shell am start -a android.settings.LOCALE_SETTINGS
# (mueve "Español (España)" arriba del todo en la UI)

# 3. Lanza la app y rellena los campos a mano para cada estado
adb shell am start -n com.mablanco.pricegrab/.MainActivity

# 4. Captura
adb exec-out screencap -p \
    > android/fastlane/metadata/android/es-ES/images/phoneScreenshots/01_empty.png
```

Repite con los valores que aparecen en cada captura: vacío,
`1,99 / 500` vs `3,49 / 1000` (B gana) y `2,00 / 500` vs `4,00 / 1000`
(empate). Cierra el teclado antes de cada `screencap`.
