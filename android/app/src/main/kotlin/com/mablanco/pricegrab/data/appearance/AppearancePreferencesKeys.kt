package com.mablanco.pricegrab.data.appearance

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * DataStore preference keys for appearance (feature 007) and currency
 * (feature 010). Both share the `appearance_preferences` file (see
 * [AppearancePreferencesRepository]) so one backup rule covers all user
 * preferences.
 */
object AppearancePreferencesKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val MATERIAL_YOU_ENABLED = booleanPreferencesKey("material_you_enabled")
    val CURRENCY_MODE = stringPreferencesKey("currency_mode")
    val CURRENCY_CUSTOM_SYMBOL = stringPreferencesKey("currency_custom_symbol")
}
