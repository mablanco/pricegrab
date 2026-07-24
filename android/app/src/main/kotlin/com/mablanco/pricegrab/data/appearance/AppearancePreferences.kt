package com.mablanco.pricegrab.data.appearance

/**
 * User-selected appearance mode. Persisted as a lowercase string key.
 *
 * Unknown or missing stored values map to [System] (see [fromStorage]).
 */
enum class AppearanceMode {
    System,
    Light,
    Dark,
    ;

    fun toStorage(): String = when (this) {
        System -> STORAGE_SYSTEM
        Light -> STORAGE_LIGHT
        Dark -> STORAGE_DARK
    }

    companion object {
        const val STORAGE_SYSTEM: String = "system"
        const val STORAGE_LIGHT: String = "light"
        const val STORAGE_DARK: String = "dark"

        /**
         * Maps a persisted string to a mode. Missing/blank/invalid → [System].
         */
        fun fromStorage(value: String?): AppearanceMode = when (value) {
            STORAGE_LIGHT -> Light
            STORAGE_DARK -> Dark
            STORAGE_SYSTEM -> System
            else -> System
        }
    }
}

/**
 * Aggregate of appearance choices. Fresh-install defaults match pre-007
 * behaviour: follow the system theme and keep the brand palette
 * (Material You off).
 */
data class AppearancePreferences(
    val mode: AppearanceMode = AppearanceMode.System,
    val materialYouEnabled: Boolean = false,
)

/**
 * Effective theme inputs for [com.mablanco.pricegrab.ui.theme.PriceGrabTheme].
 * Not persisted — derived by [resolveAppearance].
 */
data class ResolvedAppearance(
    val darkTheme: Boolean,
    val useDynamicColor: Boolean,
)
