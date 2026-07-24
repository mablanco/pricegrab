package com.mablanco.pricegrab.ui.compare

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mablanco.pricegrab.R
import com.mablanco.pricegrab.core.model.QuantityUnit
import com.mablanco.pricegrab.ui.theme.spacing
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Stateful entry point: reads a [CompareViewModel] from the current
 * [androidx.lifecycle.ViewModelStore] and delegates to the stateless
 * [CompareScreen] overload.
 */
@Composable
fun CompareScreen(
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CompareViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CompareScreen(
        state = state,
        onPriceChange = viewModel::onPriceChange,
        onQuantityChange = viewModel::onQuantityChange,
        onUnitChange = viewModel::onUnitChange,
        onAddOffer = viewModel::addOffer,
        onRemoveOffer = viewModel::removeOffer,
        onResetClick = viewModel::resetComparison,
        onUndoClick = viewModel::undoReset,
        onUndoDismissed = viewModel::dismissUndo,
        onOpenSettings = onOpenSettings,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    state: CompareUiState,
    onPriceChange: (Int, String) -> Unit,
    onQuantityChange: (Int, String) -> Unit,
    onUnitChange: (Int, QuantityUnit) -> Unit,
    onAddOffer: () -> Unit,
    onRemoveOffer: () -> Unit,
    onResetClick: () -> Unit,
    onUndoClick: () -> Unit,
    onUndoDismissed: () -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val priceAFocusRequester = remember { FocusRequester() }

    FocusOnFreshResetEffect(state.undoState, priceAFocusRequester)
    UndoSnackbarEffect(
        undoState = state.undoState,
        snackbarHostState = snackbarHostState,
        onUndoClick = onUndoClick,
        onUndoDismissed = onUndoDismissed,
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            CompareTopBar(
                resetEnabled = state.isResetEnabled,
                onResetClick = onResetClick,
                onOpenSettings = onOpenSettings,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        CompareContent(
            state = state,
            onPriceChange = onPriceChange,
            onQuantityChange = onQuantityChange,
            onUnitChange = onUnitChange,
            onAddOffer = onAddOffer,
            onRemoveOffer = onRemoveOffer,
            priceAFocusRequester = priceAFocusRequester,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun FocusOnFreshResetEffect(
    undoState: UndoState?,
    focusRequester: FocusRequester,
) {
    LaunchedEffect(undoState?.expiresAtEpochMillis) {
        if (undoState != null) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
private fun UndoSnackbarEffect(
    undoState: UndoState?,
    snackbarHostState: SnackbarHostState,
    onUndoClick: () -> Unit,
    onUndoDismissed: () -> Unit,
) {
    val undoMessage = stringResource(R.string.comparison_cleared)
    val undoActionLabel = stringResource(R.string.undo_action)
    LaunchedEffect(undoState) {
        if (undoState == null) {
            snackbarHostState.currentSnackbarData?.dismiss()
            return@LaunchedEffect
        }
        val remaining = undoState.expiresAtEpochMillis - System.currentTimeMillis()
        if (remaining <= 0L) {
            onUndoDismissed()
            return@LaunchedEffect
        }
        val result = withTimeoutOrNull(remaining) {
            snackbarHostState.showSnackbar(
                message = undoMessage,
                actionLabel = undoActionLabel,
                duration = SnackbarDuration.Indefinite,
                withDismissAction = false,
            )
        }
        when (result) {
            SnackbarResult.ActionPerformed -> onUndoClick()
            SnackbarResult.Dismissed, null -> onUndoDismissed()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompareTopBar(
    resetEnabled: Boolean,
    onResetClick: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val resetDescription = stringResource(R.string.reset_action_description)
    val settingsDescription = stringResource(R.string.settings_open_description)
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.s),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_brandmark),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(BRANDMARK_SIZE)
                        .testTag(TEST_TAG_BRANDMARK),
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        actions = {
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .testTag(TEST_TAG_SETTINGS_OPEN)
                    .semantics { contentDescription = settingsDescription },
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                )
            }
            IconButton(
                onClick = onResetClick,
                enabled = resetEnabled,
                modifier = Modifier
                    .testTag(TEST_TAG_RESET)
                    .semantics { contentDescription = resetDescription },
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
private fun CompareContent(
    state: CompareUiState,
    onPriceChange: (Int, String) -> Unit,
    onQuantityChange: (Int, String) -> Unit,
    onUnitChange: (Int, QuantityUnit) -> Unit,
    onAddOffer: () -> Unit,
    onRemoveOffer: () -> Unit,
    priceAFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.l),
    ) {
        Text(
            text = stringResource(R.string.compare_heading),
            style = MaterialTheme.typography.titleLarge,
        )

        state.offers.forEachIndexed { index, slot ->
            OfferCard(
                title = stringResource(offerTitleRes(index)),
                priceRaw = slot.priceRaw,
                priceError = slot.priceError,
                quantityRaw = slot.quantityRaw,
                quantityError = slot.quantityError,
                quantityUnit = slot.quantityUnit,
                onPriceChange = { onPriceChange(index, it) },
                onQuantityChange = { onQuantityChange(index, it) },
                onQuantityUnitChange = { onUnitChange(index, it) },
                testTagPrefix = offerTestTag(index),
                priceFocusRequester = if (index == 0) priceAFocusRequester else null,
            )
        }

        OfferCountControls(
            offerCount = state.offers.size,
            onAddOffer = onAddOffer,
            onRemoveOffer = onRemoveOffer,
        )

        ResultRegion(
            outcome = state.outcome,
            incompatibleUnits = state.incompatibleUnits,
            dimension = resultDimension(state),
        )
    }
}

@Composable
private fun OfferCountControls(
    offerCount: Int,
    onAddOffer: () -> Unit,
    onRemoveOffer: () -> Unit,
) {
    val addDescription = stringResource(R.string.cd_add_offer)
    val removeDescription = stringResource(R.string.cd_remove_offer)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.s),
    ) {
        if (offerCount < MAX_OFFERS) {
            TextButton(
                onClick = onAddOffer,
                modifier = Modifier
                    .testTag(TEST_TAG_ADD_OFFER)
                    .semantics { contentDescription = addDescription },
            ) {
                Text(stringResource(R.string.add_offer))
            }
        }
        if (offerCount > MIN_OFFERS) {
            TextButton(
                onClick = onRemoveOffer,
                modifier = Modifier
                    .testTag(TEST_TAG_REMOVE_OFFER)
                    .semantics { contentDescription = removeDescription },
            ) {
                Text(stringResource(R.string.remove_offer))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfferCard(
    title: String,
    priceRaw: String,
    priceError: InputError?,
    quantityRaw: String,
    quantityError: InputError?,
    quantityUnit: QuantityUnit,
    onPriceChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onQuantityUnitChange: (QuantityUnit) -> Unit,
    testTagPrefix: String,
    priceFocusRequester: FocusRequester?,
) {
    val spacing = MaterialTheme.spacing
    val arrangement = arrangementFor(LocalDensity.current.fontScale)
    val unitName = stringResource(quantityUnit.nameRes())
    val priceCd = stringResource(R.string.cd_price_field, title)
    val quantityCd = "${stringResource(R.string.cd_quantity_field, title)}, $unitName"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(spacing.l),
            verticalArrangement = Arrangement.spacedBy(spacing.m),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)

            when (arrangement) {
                OfferInputArrangement.CompactSingleRow -> CompactOfferFieldsRow(
                    title = title,
                    priceRaw = priceRaw,
                    priceError = priceError,
                    quantityRaw = quantityRaw,
                    quantityError = quantityError,
                    quantityUnit = quantityUnit,
                    priceCd = priceCd,
                    quantityCd = quantityCd,
                    onPriceChange = onPriceChange,
                    onQuantityChange = onQuantityChange,
                    onQuantityUnitChange = onQuantityUnitChange,
                    testTagPrefix = testTagPrefix,
                    priceFocusRequester = priceFocusRequester,
                )

                OfferInputArrangement.AdaptiveTwoRow -> AdaptiveOfferFields(
                    title = title,
                    priceRaw = priceRaw,
                    priceError = priceError,
                    quantityRaw = quantityRaw,
                    quantityError = quantityError,
                    quantityUnit = quantityUnit,
                    priceCd = priceCd,
                    quantityCd = quantityCd,
                    onPriceChange = onPriceChange,
                    onQuantityChange = onQuantityChange,
                    onQuantityUnitChange = onQuantityUnitChange,
                    testTagPrefix = testTagPrefix,
                    priceFocusRequester = priceFocusRequester,
                )
            }
        }
    }
}

@Composable
private fun CompactOfferFieldsRow(
    title: String,
    priceRaw: String,
    priceError: InputError?,
    quantityRaw: String,
    quantityError: InputError?,
    quantityUnit: QuantityUnit,
    priceCd: String,
    quantityCd: String,
    onPriceChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onQuantityUnitChange: (QuantityUnit) -> Unit,
    testTagPrefix: String,
    priceFocusRequester: FocusRequester?,
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.s),
        verticalAlignment = Alignment.Bottom,
    ) {
        LabeledNumberField(
            value = priceRaw,
            onValueChange = onPriceChange,
            labelRes = R.string.price_label,
            contentDescription = priceCd,
            error = priceError,
            imeAction = ImeAction.Next,
            testTag = "${testTagPrefix}_price",
            focusRequester = priceFocusRequester,
            modifier = Modifier.weight(PRICE_FIELD_WEIGHT),
        )
        LabeledNumberField(
            value = quantityRaw,
            onValueChange = onQuantityChange,
            labelRes = R.string.quantity_label,
            contentDescription = quantityCd,
            error = quantityError,
            imeAction = ImeAction.Done,
            testTag = "${testTagPrefix}_quantity",
            focusRequester = null,
            modifier = Modifier.weight(QUANTITY_FIELD_WEIGHT),
        )
        QuantityUnitSelector(
            offerTitle = title,
            selectedUnit = quantityUnit,
            onUnitSelected = onQuantityUnitChange,
            testTag = "${testTagPrefix}_unit",
        )
    }
}

@Composable
private fun AdaptiveOfferFields(
    title: String,
    priceRaw: String,
    priceError: InputError?,
    quantityRaw: String,
    quantityError: InputError?,
    quantityUnit: QuantityUnit,
    priceCd: String,
    quantityCd: String,
    onPriceChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onQuantityUnitChange: (QuantityUnit) -> Unit,
    testTagPrefix: String,
    priceFocusRequester: FocusRequester?,
) {
    val spacing = MaterialTheme.spacing
    LabeledNumberField(
        value = priceRaw,
        onValueChange = onPriceChange,
        labelRes = R.string.price_label,
        contentDescription = priceCd,
        error = priceError,
        imeAction = ImeAction.Next,
        testTag = "${testTagPrefix}_price",
        focusRequester = priceFocusRequester,
        modifier = Modifier.fillMaxWidth(),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.s),
        verticalAlignment = Alignment.Bottom,
    ) {
        LabeledNumberField(
            value = quantityRaw,
            onValueChange = onQuantityChange,
            labelRes = R.string.quantity_label,
            contentDescription = quantityCd,
            error = quantityError,
            imeAction = ImeAction.Done,
            testTag = "${testTagPrefix}_quantity",
            focusRequester = null,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        QuantityUnitSelector(
            offerTitle = title,
            selectedUnit = quantityUnit,
            onUnitSelected = onQuantityUnitChange,
            testTag = "${testTagPrefix}_unit",
        )
    }
}

private const val PRICE_FIELD_WEIGHT = 1.15f
private const val QUANTITY_FIELD_WEIGHT = 1f

@Composable
private fun LabeledNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    contentDescription: String,
    error: InputError?,
    imeAction: ImeAction,
    testTag: String,
    focusRequester: FocusRequester?,
    modifier: Modifier = Modifier,
) {
    val baseModifier = modifier
        .testTag(testTag)
        .semantics { this.contentDescription = contentDescription }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let {
            { Text(stringResource(it.messageRes())) }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction,
        ),
        modifier = if (focusRequester != null) {
            baseModifier.focusRequester(focusRequester)
        } else {
            baseModifier
        },
    )
}

@StringRes
private fun InputError.messageRes(): Int = when (this) {
    InputError.NotANumber -> R.string.error_not_a_number
    InputError.NegativePrice -> R.string.error_negative_price
    InputError.NonPositiveQuantity -> R.string.error_non_positive_quantity
}

// ---- Test tags --------------------------------------------------------------

const val TEST_TAG_OFFER_A: String = "offerA"
const val TEST_TAG_OFFER_B: String = "offerB"
const val TEST_TAG_OFFER_C: String = "offerC"
const val TEST_TAG_ADD_OFFER: String = "add_offer"
const val TEST_TAG_REMOVE_OFFER: String = "remove_offer"

const val TEST_TAG_RESULT: String = "result"
const val TEST_TAG_HERO_RESULT: String = "heroResult"
const val TEST_TAG_RESULT_TEXT: String = "result_text"
const val TEST_TAG_RESULT_SAVINGS: String = "result_savings"
const val TEST_TAG_INCOMPATIBLE_UNITS: String = "incompatible_units"
const val TEST_TAG_RESET: String = "reset_action"
const val TEST_TAG_SETTINGS_OPEN: String = "settings_open"
const val TEST_TAG_BRANDMARK: String = "brandmark"

private val BRANDMARK_SIZE = 24.dp
