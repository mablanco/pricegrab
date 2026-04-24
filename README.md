# PriceGrab

> Compara dos precios con cantidades distintas y descubre, al instante, cuál sale más barato por unidad.
> Compare two prices with different quantities and instantly see which one is cheaper per unit.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)
[![Platform: Android](https://img.shields.io/badge/platform-Android-3DDC84.svg)](https://developer.android.com/)
[![Status](https://img.shields.io/badge/status-in%20development-orange.svg)](#estado-del-proyecto--project-status)

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

En fase inicial de especificación con [Spec Kit](https://github.com/github/spec-kit). Los principios del producto están formalizados en [`.specify/memory/constitution.md`](./.specify/memory/constitution.md) (versión 1.0.1).

### Distribución

Los releases se publican como APK firmados en [GitHub Releases](../../releases). **No se publicará en Google Play.** La inclusión en [F-Droid](https://f-droid.org/) está bajo evaluación; el proyecto se mantiene compatible con sus requisitos (toolchain 100% open source, builds reproducibles, sin dependencias propietarias).

### Cómo contribuir

El proyecto sigue un flujo **Spec-Driven Development**: toda feature nueva pasa por `/speckit.specify` → `/speckit.plan` → `/speckit.tasks` → `/speckit.implement`. Más detalles cuando haya una primera especificación publicada en `specs/`.

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

Early specification phase using [Spec Kit](https://github.com/github/spec-kit). Product principles are formalized in [`.specify/memory/constitution.md`](./.specify/memory/constitution.md) (version 1.0.1).

### Distribution

Releases are published as signed APKs on [GitHub Releases](../../releases). **Google Play is not a target.** Inclusion in [F-Droid](https://f-droid.org/) is under evaluation; the project stays compatible with its requirements (fully open-source toolchain, reproducible builds, no proprietary dependencies).

### Contributing

The project follows a **Spec-Driven Development** workflow: every new feature goes through `/speckit.specify` → `/speckit.plan` → `/speckit.tasks` → `/speckit.implement`. More details once a first spec is published under `specs/`.

### License

[MIT](./LICENSE) © 2026 Marco Antonio Blanco.
