# PriceGrab

> Compara hasta tres ofertas con distintas cantidades y unidades, y descubre al instante cuál sale más barata por kg, L o unidad.
> Compare up to three offers with different quantities and units, and instantly see which is cheaper per kg, L, or piece.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)
[![Platform: Android](https://img.shields.io/badge/platform-Android-3DDC84.svg)](https://developer.android.com/)
[![Latest release](https://img.shields.io/github/v/release/mablanco/pricegrab?color=3DDC84&label=release)](https://github.com/mablanco/pricegrab/releases/latest)
[![F-Droid](https://img.shields.io/badge/F--Droid-available-8ab000?logo=fdroid&logoColor=white)](https://f-droid.org/packages/com.mablanco.pricegrab/)

---

## Español

### ¿Qué es?

**PriceGrab** es una aplicación Android que resuelve una pregunta muy concreta del supermercado: *"¿cuál de estas ofertas me sale más barata por unidad?"*. Introduces precio, cantidad y unidad de hasta tres ofertas, y la app te dice cuál es la mejor comparando el precio por kg, por L o por pieza, con el ahorro frente a la segunda más barata.

### Antecedentes

Hace años desarrollé una versión preliminar de esta aplicación. Este proyecto es la reescritura moderna de aquella idea, como app Android nativa.

### Objetivos

- Interfaz moderna siguiendo **Material Design 3**.
- **Accesibilidad** de primer nivel (TalkBack, alto contraste, font scaling).
- Soporte nativo de **español e inglés** desde el día uno.
- **Funciona sin conexión** y sin recolectar ningún dato personal.
- **Los cálculos están cubiertos por pruebas automáticas**, para que el resultado sea fiable.

### Estado del proyecto

**[`v0.1.8`](https://github.com/mablanco/pricegrab/releases/tag/v0.1.8) publicada** en [GitHub Releases](https://github.com/mablanco/pricegrab/releases/latest). En F-Droid el catálogo puede ir un poco por detrás (Mode B / builds reproducibles); el APK firmado de GitHub es la referencia.

Incluye: hasta **tres ofertas** (`+` / `−`), unidades **g / kg / ml / L / uds**, Reset con Undo, branding Material 3, TalkBack, y ES/EN.

- Estado y backlog: [`docs/project-status.md`](./docs/project-status.md).
- Cómo se firma y se libera el APK: [`docs/release.md`](./docs/release.md).
- Specs por feature: [`specs/`](./specs/) (001–005).
- Principios del producto: [`.specify/memory/constitution.md`](./.specify/memory/constitution.md) (versión 1.0.1).
- F-Droid (Mode B): [`docs/fdroid.md`](./docs/fdroid.md).

### Cómo construirla

Requisitos: JDK 17 y, para correr la app, Android Studio Ladybug (2024.2.1) o el SDK por línea de comandos para API 35.

```bash
git clone git@github.com:mablanco/pricegrab.git
cd pricegrab/android
./gradlew :app:assembleDebug      # compila la versión de debug
./gradlew :app:test :app:detekt   # tests unitarios + análisis estático
```

Guía completa en [`specs/001-unit-price-comparison/quickstart.md`](./specs/001-unit-price-comparison/quickstart.md).

### Distribución

Puedes descargar la app como APK firmado desde [GitHub Releases](../../releases) y desde [F-Droid](https://f-droid.org/packages/com.mablanco.pricegrab/). **No está ni estará en Google Play.** Las descripciones de la ficha de la app están en [`fastlane/metadata/android/`](./fastlane/metadata/android/) en español e inglés. La historia de cómo llegó a F-Droid —incluida la receta para el catálogo de F-Droid— está en [`docs/fdroid.md`](./docs/fdroid.md).

### Cómo contribuir

El proyecto sigue un flujo **Spec-Driven Development**: cada mejora nueva pasa por `/speckit.specify` → `/speckit.plan` → `/speckit.tasks` → `/speckit.implement`. Las reglas operativas para humanos y agentes IA están en [`AGENTS.md`](./AGENTS.md) y [`.cursor/rules/project-conventions.mdc`](./.cursor/rules/project-conventions.mdc).

### Licencia

[MIT](./LICENSE) © 2026 Marco Antonio Blanco.

---

## English

### What is it?

**PriceGrab** is an Android app that answers one very specific supermarket question: *"which of these offers is cheaper per unit?"*. You enter price, quantity, and unit for up to three offers, and the app picks the best deal by comparing price per kg, per L, or per piece, with savings versus the second-cheapest.

### Background

Years ago I built a preliminary version of this application. This project is the modern rewrite of that idea, as a native Android app.

### Goals

- Modern UI following **Material Design 3**.
- First-class **accessibility** (TalkBack, high contrast, font scaling).
- Native **Spanish and English** support from day one.
- Works **fully offline**, collects **no personal data**.
- **Automatic tests cover the calculations**, so you can trust the result.

### Project status

**[`v0.1.8`](https://github.com/mablanco/pricegrab/releases/tag/v0.1.8) is out** on [GitHub Releases](https://github.com/mablanco/pricegrab/releases/latest). The F-Droid catalog may lag slightly (Mode B / reproducible builds); the signed GitHub APK is the source of truth.

Includes: up to **three offers** (`+` / `−`), units **g / kg / ml / L / pcs**, Reset with Undo, Material 3 branding, TalkBack, and ES/EN.

- Status and backlog: [`docs/project-status.md`](./docs/project-status.md).
- How the APK is signed and released: [`docs/release.md`](./docs/release.md).
- Per-feature specs: [`specs/`](./specs/) (001–005).
- Product principles: [`.specify/memory/constitution.md`](./.specify/memory/constitution.md) (version 1.0.1).
- F-Droid (Mode B): [`docs/fdroid.md`](./docs/fdroid.md).

### How to build

Prerequisites: JDK 17 and, to run the app, Android Studio Ladybug (2024.2.1) or the command-line SDK for API 35.

```bash
git clone git@github.com:mablanco/pricegrab.git
cd pricegrab/android
./gradlew :app:assembleDebug      # build the debug APK
./gradlew :app:test :app:detekt   # unit tests + static analysis
```

Full guide in [`specs/001-unit-price-comparison/quickstart.md`](./specs/001-unit-price-comparison/quickstart.md).

### Distribution

You can download the app as a signed APK from [GitHub Releases](../../releases) and from [F-Droid](https://f-droid.org/packages/com.mablanco.pricegrab/). **It is not and will not be on Google Play.** Store listing text is in [`fastlane/metadata/android/`](./fastlane/metadata/android/) in Spanish and English. How the app got onto F-Droid —including the catalog recipe— is documented in [`docs/fdroid.md`](./docs/fdroid.md).

### Contributing

The project follows a **Spec-Driven Development** workflow: every new feature goes through `/speckit.specify` → `/speckit.plan` → `/speckit.tasks` → `/speckit.implement`. Operating rules for humans and AI agents live in [`AGENTS.md`](./AGENTS.md) and [`.cursor/rules/project-conventions.mdc`](./.cursor/rules/project-conventions.mdc).

### License

[MIT](./LICENSE) © 2026 Marco Antonio Blanco.
