package com.mablanco.pricegrab.ui.settings

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mablanco.pricegrab.data.appearance.AppearanceMode
import com.mablanco.pricegrab.data.appearance.AppearancePreferences
import com.mablanco.pricegrab.data.appearance.AppearancePreferencesRepository
import com.mablanco.pricegrab.data.appearance.DYNAMIC_COLOR_MIN_SDK
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Thin ViewModel over [AppearancePreferencesRepository] for the Settings
 * screen. Shares the Activity ViewModelStore so Compare navigation does
 * not recreate preference collection when returning from Settings.
 */
class SettingsViewModel(
    application: Application,
    private val repository: AppearancePreferencesRepository =
        AppearancePreferencesRepository(application),
) : AndroidViewModel(application) {

    val preferences: StateFlow<AppearancePreferences> = repository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppearancePreferences(),
        )

    val materialYouSupported: Boolean =
        Build.VERSION.SDK_INT >= DYNAMIC_COLOR_MIN_SDK

    fun setMode(mode: AppearanceMode) {
        viewModelScope.launch { repository.setMode(mode) }
    }

    fun setMaterialYouEnabled(enabled: Boolean) {
        if (!materialYouSupported && enabled) return
        viewModelScope.launch { repository.setMaterialYouEnabled(enabled) }
    }
}
