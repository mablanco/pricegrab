package com.mablanco.pricegrab.data.currency

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.mablanco.pricegrab.data.appearance.AppearancePreferencesKeys
import com.mablanco.pricegrab.data.appearance.appearanceDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reads and writes [CurrencyPreferences]. Shares the appearance DataStore
 * file, which the backup rules already include.
 */
class CurrencyPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    constructor(context: Context) : this(context.appearanceDataStore)

    val preferences: Flow<CurrencyPreferences> = dataStore.data.map { prefs ->
        CurrencyPreferences(
            mode = CurrencyMode.fromStorage(prefs[AppearancePreferencesKeys.CURRENCY_MODE]),
            customSymbol = prefs[AppearancePreferencesKeys.CURRENCY_CUSTOM_SYMBOL].orEmpty(),
        )
    }

    suspend fun setMode(mode: CurrencyMode) {
        dataStore.edit { prefs ->
            prefs[AppearancePreferencesKeys.CURRENCY_MODE] = mode.toStorage()
        }
    }

    suspend fun setCustomSymbol(symbol: String) {
        dataStore.edit { prefs ->
            prefs[AppearancePreferencesKeys.CURRENCY_CUSTOM_SYMBOL] = capCustomSymbol(symbol)
        }
    }
}
