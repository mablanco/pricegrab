package com.mablanco.pricegrab.data.appearance

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val APPEARANCE_PREFERENCES_FILE: String = "appearance_preferences"

val Context.appearanceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = APPEARANCE_PREFERENCES_FILE,
)

/**
 * Reads and writes appearance preferences via DataStore Preferences.
 */
class AppearancePreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    constructor(context: Context) : this(context.appearanceDataStore)

    val preferences: Flow<AppearancePreferences> = dataStore.data.map { prefs ->
        AppearancePreferences(
            mode = AppearanceMode.fromStorage(
                prefs[AppearancePreferencesKeys.THEME_MODE],
            ),
            materialYouEnabled = prefs[AppearancePreferencesKeys.MATERIAL_YOU_ENABLED]
                ?: false,
        )
    }

    suspend fun setMode(mode: AppearanceMode) {
        dataStore.edit { prefs ->
            prefs[AppearancePreferencesKeys.THEME_MODE] = mode.toStorage()
        }
    }

    suspend fun setMaterialYouEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[AppearancePreferencesKeys.MATERIAL_YOU_ENABLED] = enabled
        }
    }

    /** Clears all appearance keys (useful for instrumented test isolation). */
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
