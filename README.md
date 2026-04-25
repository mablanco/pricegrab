# PriceGrab

> Compara dos precios con cantidades distintas y descubre, al instante, cuál sale más barato por unidad.
> Compare two prices with different quantities and instantly see which one is cheaper per unit.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)
[![Platform: Android](https://img.shields.io/badge/platform-Android-3DDC84.svg)](https://developer.android.com/)
[![Latest release](https://img.shields.io/github/v/release/mablanco/pricegrab?color=3DDC84&label=release)](https://github.com/mablanco/pricegrab/releases/latest)

---

## Español

### ¿Qué es?

**PriceGrab** es una aplicación Android que resuelve una pregunta muy concreta del supermercado: *"¿cuál de estos dos productos me sale más barato por unidad?"*. Introduces precio y cantidad del producto A y del producto B, y la app te dice cuál es la mejor oferta comparando su precio por unidad.

### Antecedentes

Hace años desarrollé una versión preliminar de esta aplicación. Este proyecto es la reescritura moderna de aquella idea, como app Android nativa.

### Objetivos

- Interfaz moderna siguiendo **Material Design 3**.
- **Accesibilidad** de primer nivel (TalkBack, alto contraste, font scaling).
- Soporte nativo de **español e inglés** desde el día uno.
- **Funciona sin conexión** y sin recolectar ningún dato personal.
- Cálculo provablemente correcto: **desarrollo dirigido por tests**.

### Estado del proyecto

**`v0.1.0` publicada** en [GitHub Releases](https://github.com/mablanco/pricegrab/releases/tag/v0.1.0). El APK firmado pesa ~0.91 MB. La primera feature está completa: comparación de dos ofertas, cálculo del ahorro absoluto y porcentual, soporte completo de TalkBack, rotación e idioma.

- Cómo se firma y se libera el APK: [`docs/release.md`](./docs/release.md).
- Especificación, plan y tareas: [`specs/001-unit-price-comparison/`](./specs/001-unit-price-comparison/).
- Principios del producto: [`.specify/memory/constitution.md`](./.specify/memory/constitution.md) (versión 1.0.1).
- Pendiente para la submission a F-Droid: capturar screenshots reales (`android/fastlane/metadata/android/{en-US,es-ES}/images/phoneScreenshots/`) y mantener la cadena de firma estable en futuros tags.

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

Los releases se publican como APK firmados en [GitHub Releases](../../releases). **No se publicará en Google Play.** El proyecto se mantiene compatible con [F-Droid](https://f-droid.org/) (toolchain 100% open source, sin dependencias propietarias) y los textos de tienda viven en [`android/fastlane/metadata/android/`](./android/fastlane/metadata/android/) en los dos idiomas.

### Cómo contribuir

El proyecto sigue un flujo **Spec-Driven Development**: toda feature nueva pasa por `/speckit.specify` → `/speckit.plan` → `/speckit.tasks` → `/speckit.implement`. Las reglas operativas para humanos y agentes IA están en [`AGENTS.md`](./AGENTS.md) y [`.cursor/rules/project-conventions.mdc`](./.cursor/rules/project-conventions.mdc).

### Licencia

[MIT](./LICENSE) © 2026 Marco Antonio Blanco.

---

## English

### What is it?

**PriceGrab** is an Android app that answers one very specific supermarket question: *"which of these two products is cheaper per unit?"*. You enter the price and quantity for product A and product B, and the app tells you which is the better deal by comparing their unit prices.

### Background

Years ago I built a preliminary version of this application. This project is the modern rewrite of that idea, as a native Android app.

### Goals

- Modern UI following **Material Design 3**.
- First-class **accessibility** (TalkBack, high contrast, font scaling).
- Native **Spanish and English** support from day one.
- Works **fully offline**, collects **no personal data**.
- Provably correct math: **test-first development**.

### Project status

**`v0.1.0` is out** on [GitHub Releases](https://github.com/mablanco/pricegrab/releases/tag/v0.1.0). The signed APK weighs ~0.91 MB. The first feature is complete: two-offer comparison, absolute and percent savings, full TalkBack support, rotation and language preservation.

- How the APK is signed and released: [`docs/release.md`](./docs/release.md).
- Spec, plan and tasks: [`specs/001-unit-price-comparison/`](./specs/001-unit-price-comparison/).
- Product principles: [`.specify/memory/constitution.md`](./.specify/memory/constitution.md) (version 1.0.1).
- Pending for F-Droid submission: capture real screenshots (`android/fastlane/metadata/android/{en-US,es-ES}/images/phoneScreenshots/`) and keep the signing chain stable across future tags.

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

Releases are published as signed APKs on [GitHub Releases](../../releases). **Google Play is not a target.** The project stays compatible with [F-Droid](https://f-droid.org/) (fully open-source toolchain, no proprietary dependencies) and the store metadata lives in [`android/fastlane/metadata/android/`](./android/fastlane/metadata/android/) in both languages.

### Contributing

The project follows a **Spec-Driven Development** workflow: every new feature goes through `/speckit.specify` → `/speckit.plan` → `/speckit.tasks` → `/speckit.implement`. Operating rules for humans and AI agents live in [`AGENTS.md`](./AGENTS.md) and [`.cursor/rules/project-conventions.mdc`](./.cursor/rules/project-conventions.mdc).

### License

[MIT](./LICENSE) © 2026 Marco Antonio Blanco.
