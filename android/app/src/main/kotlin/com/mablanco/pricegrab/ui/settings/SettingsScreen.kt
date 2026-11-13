package com.mablanco.pricegrab.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mablanco.pricegrab.R
import com.mablanco.pricegrab.data.appearance.AppearanceMode
import com.mablanco.pricegrab.data.appearance.AppearancePreferences
import com.mablanco.pricegrab.data.currency.CurrencyMode
import com.mablanco.pricegrab.data.currency.CurrencyPreferences
import com.mablanco.pricegrab.data.currency.capCustomSymbol
import com.mablanco.pricegrab.data.currency.resolveCurrencySymbol
import com.mablanco.pricegrab.ui.theme.spacing

const val TEST_TAG_SETTINGS_SCREEN: String = "settings_screen"
const val TEST_TAG_SETTINGS_BACK: String = "settings_back"
const val TEST_TAG_THEME_SYSTEM: String = "theme_system"
const val TEST_TAG_THEME_LIGHT: String = "theme_light"
const val TEST_TAG_THEME_DARK: String = "theme_dark"
const val TEST_TAG_MATERIAL_YOU_SWITCH: String = "material_you_switch"
const val TEST_TAG_CURRENCY_AUTOMATIC: String = "currency_automatic"
const val TEST_TAG_CURRENCY_CUSTOM: String = "currency_custom"
const val TEST_TAG_CURRENCY_SYMBOL_FIELD: String = "currency_symbol_field"

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val currency by viewModel.currencyPreferences.collectAsStateWithLifecycle()
    SettingsScreen(
        preferences = preferences,
        materialYouSupported = viewModel.materialYouSupported,
        onModeSelected = viewModel::setMode,
        onMaterialYouChange = viewModel::setMaterialYouEnabled,
        onBack = onBack,
        modifier = modifier,
        currency = currency,
        automaticCurrencySymbol = resolveCurrencySymbol(
            CurrencyPreferences(),
            LocalLocale.current.platformLocale,
        ),
        onCurrencyModeSelected = viewModel::setCurrencyMode,
        onCustomCurrencySymbolChange = viewModel::setCustomCurrencySymbol,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: AppearancePreferences,
    materialYouSupported: Boolean,
    onModeSelected: (AppearanceMode) -> Unit,
    onMaterialYouChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    currency: CurrencyPreferences = CurrencyPreferences(),
    automaticCurrencySymbol: String = "",
    onCurrencyModeSelected: (CurrencyMode) -> Unit = {},
    onCustomCurrencySymbolChange: (String) -> Unit = {},
) {
    val backDescription = stringResource(R.string.settings_back_description)
    Scaffold(
        modifier = modifier.testTag(TEST_TAG_SETTINGS_SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .testTag(TEST_TAG_SETTINGS_BACK)
                            .semantics { contentDescription = backDescription },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.l)
                .padding(bottom = MaterialTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.l),
        ) {
            Text(
                text = stringResource(R.string.settings_theme_section),
                style = MaterialTheme.typography.titleMedium,
            )
            ThemeModeGroup(
                selected = preferences.mode,
                onModeSelected = onModeSelected,
            )
            MaterialYouRow(
                enabled = materialYouSupported,
                checked = preferences.materialYouEnabled && materialYouSupported,
                onCheckedChange = onMaterialYouChange,
            )
            Text(
                text = stringResource(R.string.settings_currency_section),
                style = MaterialTheme.typography.titleMedium,
            )
            CurrencyGroup(
                currency = currency,
                automaticSymbol = automaticCurrencySymbol,
                onModeSelected = onCurrencyModeSelected,
                onCustomSymbolChange = onCustomCurrencySymbolChange,
            )
        }
    }
}

@Composable
private fun ThemeModeGroup(
    selected: AppearanceMode,
    onModeSelected: (AppearanceMode) -> Unit,
) {
    Column(modifier = Modifier.selectableGroup()) {
        ThemeOptionRow(
            label = stringResource(R.string.settings_theme_system),
            selected = selected == AppearanceMode.System,
            testTag = TEST_TAG_THEME_SYSTEM,
            onClick = { onModeSelected(AppearanceMode.System) },
        )
        ThemeOptionRow(
            label = stringResource(R.string.settings_theme_light),
            selected = selected == AppearanceMode.Light,
            testTag = TEST_TAG_THEME_LIGHT,
            onClick = { onModeSelected(AppearanceMode.Light) },
        )
        ThemeOptionRow(
            label = stringResource(R.string.settings_theme_dark),
            selected = selected == AppearanceMode.Dark,
            testTag = TEST_TAG_THEME_DARK,
            onClick = { onModeSelected(AppearanceMode.Dark) },
        )
    }
}

@Composable
private fun CurrencyGroup(
    currency: CurrencyPreferences,
    automaticSymbol: String,
    onModeSelected: (CurrencyMode) -> Unit,
    onCustomSymbolChange: (String) -> Unit,
) {
    val automaticLabel = if (automaticSymbol.isEmpty()) {
        stringResource(R.string.settings_currency_automatic_none)
    } else {
        stringResource(R.string.settings_currency_automatic, automaticSymbol)
    }
    val customSelected = currency.mode == CurrencyMode.Custom
    Column(modifier = Modifier.selectableGroup()) {
        ThemeOptionRow(
            label = automaticLabel,
            selected = !customSelected,
            testTag = TEST_TAG_CURRENCY_AUTOMATIC,
            onClick = { onModeSelected(CurrencyMode.Automatic) },
        )
        ThemeOptionRow(
            label = stringResource(R.string.settings_currency_custom),
            selected = customSelected,
            testTag = TEST_TAG_CURRENCY_CUSTOM,
            onClick = { onModeSelected(CurrencyMode.Custom) },
        )
    }
    // Once the shopper types, the field owns its text: echoing the
    // asynchronously persisted value back would reset the cursor mid-typing.
    var editedSymbol by rememberSaveable { mutableStateOf<String?>(null) }
    OutlinedTextField(
        value = editedSymbol ?: currency.customSymbol,
        onValueChange = { input ->
            val capped = capCustomSymbol(input)
            editedSymbol = capped
            onCustomSymbolChange(capped)
        },
        enabled = customSelected,
        singleLine = true,
        label = { Text(stringResource(R.string.settings_currency_symbol_label)) },
        supportingText = { Text(stringResource(R.string.settings_currency_symbol_hint)) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TEST_TAG_CURRENCY_SYMBOL_FIELD),
    )
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    val selectedLabel = stringResource(R.string.settings_theme_selected)
    val unselectedLabel = stringResource(R.string.settings_theme_unselected)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .testTag(testTag)
            .semantics {
                contentDescription = label
                stateDescription = if (selected) selectedLabel else unselectedLabel
            }
            .padding(vertical = MaterialTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.m),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun MaterialYouRow(
    enabled: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val title = stringResource(R.string.settings_material_you_title)
    val onLabel = stringResource(R.string.settings_material_you_on)
    val offLabel = stringResource(R.string.settings_material_you_off)
    val unavailable = stringResource(R.string.settings_material_you_unavailable)
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.s),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                modifier = Modifier
                    .testTag(TEST_TAG_MATERIAL_YOU_SWITCH)
                    .semantics {
                        contentDescription = title
                        stateDescription = when {
                            !enabled -> unavailable
                            checked -> onLabel
                            else -> offLabel
                        }
                    },
            )
        }
        Text(
            text = if (enabled) {
                stringResource(R.string.settings_material_you_description)
            } else {
                unavailable
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
