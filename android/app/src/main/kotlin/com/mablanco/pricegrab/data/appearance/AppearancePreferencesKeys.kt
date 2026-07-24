package com.mablanco.pricegrab.data.appearance

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * DataStore preference keys for appearance (feature 007).
 *
 * File name: `appearance_preferences` (see [AppearancePreferencesRepository]).
 */
object AppearancePreferencesKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val MATERIAL_YOU_ENABLED = booleanPreferencesKey("material_you_enabled")
}
