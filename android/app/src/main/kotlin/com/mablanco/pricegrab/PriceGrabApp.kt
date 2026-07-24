package com.mablanco.pricegrab

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mablanco.pricegrab.data.appearance.AppearanceMode
import com.mablanco.pricegrab.data.appearance.AppearancePreferences
import com.mablanco.pricegrab.data.appearance.resolveAppearance
import com.mablanco.pricegrab.ui.compare.CompareScreen
import com.mablanco.pricegrab.ui.settings.SettingsScreen
import com.mablanco.pricegrab.ui.settings.SettingsViewModel
import com.mablanco.pricegrab.ui.theme.PriceGrabTheme

/**
 * Activity-level wrapper: theme from appearance preferences, plus a
 * two-destination screen state (Compare ↔ Settings) without Navigation
 * Compose. [com.mablanco.pricegrab.ui.compare.CompareViewModel] stays
 * Activity-scoped so Compare form state survives Settings.
 */
@Composable
fun PriceGrabApp(
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val preferences by settingsViewModel.preferences.collectAsStateWithLifecycle()
    PriceGrabAppContent(
        preferences = preferences,
        materialYouSupported = settingsViewModel.materialYouSupported,
        onModeSelected = settingsViewModel::setMode,
        onMaterialYouChange = settingsViewModel::setMaterialYouEnabled,
    )
}

@Composable
fun PriceGrabAppContent(
    preferences: AppearancePreferences,
    materialYouSupported: Boolean,
    onModeSelected: (AppearanceMode) -> Unit,
    onMaterialYouChange: (Boolean) -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val resolved = resolveAppearance(
        prefs = preferences,
        systemDark = systemDark,
        sdkInt = Build.VERSION.SDK_INT,
    )

    var screen by rememberSaveable { mutableStateOf(AppScreen.Compare) }

    PriceGrabTheme(
        darkTheme = resolved.darkTheme,
        useDynamicColor = resolved.useDynamicColor,
    ) {
        when (screen) {
            AppScreen.Settings -> {
                BackHandler { screen = AppScreen.Compare }
                SettingsScreen(
                    preferences = preferences,
                    materialYouSupported = materialYouSupported,
                    onModeSelected = onModeSelected,
                    onMaterialYouChange = onMaterialYouChange,
                    onBack = { screen = AppScreen.Compare },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            AppScreen.Compare -> {
                CompareScreen(
                    onOpenSettings = { screen = AppScreen.Settings },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceGrabAppPreview() {
    PriceGrabAppContent(
        preferences = AppearancePreferences(),
        materialYouSupported = true,
        onModeSelected = {},
        onMaterialYouChange = {},
    )
}
