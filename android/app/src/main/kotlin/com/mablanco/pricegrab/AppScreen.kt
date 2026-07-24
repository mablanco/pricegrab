package com.mablanco.pricegrab

/**
 * In-memory navigation destinations for the Activity Compose tree.
 * Not persisted to DataStore — cold launch always starts on [Compare].
 * An [enum] so [androidx.compose.runtime.saveable.rememberSaveable] can
 * restore across configuration changes.
 */
enum class AppScreen {
    Compare,
    Settings,
}
