package com.mablanco.pricegrab.ui.compare

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mablanco.pricegrab.R
import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.ui.theme.PriceGrabTheme
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

/**
 * Stateful entry point: reads a [CompareViewModel] from the current
 * [androidx.lifecycle.ViewModelStore] and delegates to the stateless
 * [CompareScreen] overload.
 *
 * The Compare screen owns its own [Scaffold] (with a top app bar and a
 * snackbar host) so the activity-level `PriceGrabApp` stays a thin
 * theme + screen wrapper.
 */
@Composable
fun CompareScreen(
    modifier: Modifier = Modifier,
    viewModel: CompareViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CompareScreen(
        state = state,
        onPriceAChange = viewModel::onPriceAChange,
        onQuantityAChange = viewModel::onQuantityAChange,
        onPriceBChange = viewModel::onPriceBChange,
        onQuantityBChange = viewModel::onQuantityBChange,
        onResetClick = viewModel::resetComparison,
        onUndoClick = viewModel::undoReset,
        onUndoDismissed = viewModel::dismissUndo,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    state: CompareUiState,
    onPriceAChange: (String) -> Unit,
    onQuantityAChange: (String) -> Unit,
    onPriceBChange: (String) -> Unit,
    onQuantityBChange: (String) -> Unit,
    onResetClick: () -> Unit,
    onUndoClick: () -> Unit,
    onUndoDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val resetDescription = stringResource(R.string.reset_action_description)
    val priceAFocusRequester = remember { FocusRequester() }
    val undoMessage = stringResource(R.string.comparison_cleared)
    val undoActionLabel = stringResource(R.string.undo_action)

    // Move keyboard focus to Price A whenever a *fresh* reset starts a
    // new UndoState. Keyed on the deadline so the effect refires for
    // each reset (a new reset has a unique deadline) but does not steal
    // focus on rotation (the deadline is preserved across configuration
    // change). FR-005.3.
    LaunchedEffect(state.undoState?.expiresAtEpochMillis) {
        if (state.undoState != null) {
            priceAFocusRequester.requestFocus()
        }
    }

    // Drive the Snackbar from the active UndoState (research.md §3 + §4).
    //
    // We always show with `SnackbarDuration.Indefinite` and bound the
    // visibility ourselves with `withTimeoutOrNull(remaining)`. That
    // wrapper uses the coroutine clock, so the lifetime is honoured
    // verbatim across configuration changes (after rotation, the new
    // LaunchedEffect computes `remaining = deadline - now()` and shows
    // the Snackbar for that long instead of restarting from 10 s).
    LaunchedEffect(state.undoState) {
        val undo = state.undoState
        if (undo == null) {
            // No active Undo: hide any Snackbar still on screen so the
            // typing-dismisses-undo path closes the surface immediately.
            snackbarHostState.currentSnackbarData?.dismiss()
            return@LaunchedEffect
        }
        val remaining = undo.expiresAtEpochMillis - System.currentTimeMillis()
        if (remaining <= 0L) {
            // Stale state (e.g. process death survived the lifetime).
            // Mirror dismissUndo() upstream so observers stay in sync.
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

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(
                        onClick = onResetClick,
                        enabled = state.isResetEnabled,
                        modifier = Modifier
                            .testTag(TEST_TAG_RESET)
                            .semantics { contentDescription = resetDescription },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            // contentDescription is set on the IconButton's
                            // semantics block above so TalkBack reads
                            // "Clear all fields and start a new comparison"
                            // even though Refresh is the visual glyph.
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(SCREEN_PADDING),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
        ) {
            Text(
                text = stringResource(R.string.compare_heading),
                style = MaterialTheme.typography.headlineSmall,
            )

            OfferCard(
                title = stringResource(R.string.offer_a_title),
                priceRaw = state.priceARaw,
                priceError = state.priceAError,
                quantityRaw = state.quantityARaw,
                quantityError = state.quantityAError,
                onPriceChange = onPriceAChange,
                onQuantityChange = onQuantityAChange,
                testTagPrefix = TEST_TAG_OFFER_A,
                priceFocusRequester = priceAFocusRequester,
            )

            OfferCard(
                title = stringResource(R.string.offer_b_title),
                priceRaw = state.priceBRaw,
                priceError = state.priceBError,
                quantityRaw = state.quantityBRaw,
                quantityError = state.quantityBError,
                onPriceChange = onPriceBChange,
                onQuantityChange = onQuantityBChange,
                testTagPrefix = TEST_TAG_OFFER_B,
                priceFocusRequester = null,
            )

            ResultCard(outcome = state.outcome)
        }
    }
}

@Composable
private fun OfferCard(
    title: String,
    priceRaw: String,
    priceError: InputError?,
    quantityRaw: String,
    quantityError: InputError?,
    onPriceChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    testTagPrefix: String,
    priceFocusRequester: FocusRequester?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(SCREEN_PADDING),
            verticalArrangement = Arrangement.spacedBy(FIELD_SPACING),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)

            LabeledNumberField(
                value = priceRaw,
                onValueChange = onPriceChange,
                labelRes = R.string.price_label,
                contentDescription = stringResource(R.string.cd_price_field, title),
                error = priceError,
                imeAction = ImeAction.Next,
                testTag = "${testTagPrefix}_price",
                focusRequester = priceFocusRequester,
            )

            LabeledNumberField(
                value = quantityRaw,
                onValueChange = onQuantityChange,
                labelRes = R.string.quantity_label,
                contentDescription = stringResource(R.string.cd_quantity_field, title),
                error = quantityError,
                imeAction = ImeAction.Done,
                testTag = "${testTagPrefix}_quantity",
                focusRequester = null,
            )
        }
    }
}

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
) {
    val baseModifier = Modifier
        .fillMaxWidth()
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

@Composable
private fun ResultCard(outcome: ComparisonOutcome?) {
    val configuration = LocalConfiguration.current
    val locale = ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.getDefault()

    val headline = stringResource(outcome.headlineRes())
    val savings = ResultPresenter.present(outcome, locale)
    val savingsLine: String? = savings?.let {
        stringResource(R.string.savings_template, it.perUnitDelta, it.percentDelta)
    }
    // Live-region announcement reads the headline first, then the savings
    // detail. We keep them in a single content description so TalkBack speaks
    // the full thought on every state change.
    val a11ySummary = if (savingsLine != null) "$headline. $savingsLine" else headline

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TEST_TAG_RESULT)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = a11ySummary
            },
    ) {
        Row(
            modifier = Modifier.padding(SCREEN_PADDING),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FIELD_SPACING),
        ) {
            if (outcome is ComparisonOutcome.AWins || outcome is ComparisonOutcome.BWins) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(RESULT_LINE_SPACING)) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag(TEST_TAG_RESULT_TEXT),
                )
                if (savingsLine != null) {
                    Text(
                        text = savingsLine,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag(TEST_TAG_RESULT_SAVINGS),
                    )
                }
            }
        }
    }
}

@StringRes
private fun InputError.messageRes(): Int = when (this) {
    InputError.NotANumber -> R.string.error_not_a_number
    InputError.NegativePrice -> R.string.error_negative_price
    InputError.NonPositiveQuantity -> R.string.error_non_positive_quantity
}

@StringRes
private fun ComparisonOutcome?.headlineRes(): Int = when (this) {
    null -> R.string.result_placeholder
    ComparisonOutcome.Tie -> R.string.result_tie
    is ComparisonOutcome.AWins -> R.string.result_a_wins
    is ComparisonOutcome.BWins -> R.string.result_b_wins
}

// ---- Test tags (constants so tests can reference them) ----------------------

const val TEST_TAG_OFFER_A: String = "offerA"
const val TEST_TAG_OFFER_B: String = "offerB"
const val TEST_TAG_RESULT: String = "result"
const val TEST_TAG_RESULT_TEXT: String = "result_text"
const val TEST_TAG_RESULT_SAVINGS: String = "result_savings"
const val TEST_TAG_RESET: String = "reset_action"

// ---- Layout constants -------------------------------------------------------

private val SCREEN_PADDING = 16.dp
private val SECTION_SPACING = 16.dp
private val FIELD_SPACING = 12.dp
private val RESULT_LINE_SPACING = 4.dp

// ---- Previews ---------------------------------------------------------------

@Preview(showBackground = true)
@Composable
private fun CompareScreenEmptyPreview() {
    PriceGrabTheme {
        CompareScreen(
            state = CompareUiState(),
            onPriceAChange = {},
            onQuantityAChange = {},
            onPriceBChange = {},
            onQuantityBChange = {},
            onResetClick = {},
            onUndoClick = {},
            onUndoDismissed = {},
        )
    }
}

@Preview(showBackground = true, name = "A wins with savings")
@Composable
private fun CompareScreenAWinsPreview() {
    PriceGrabTheme {
        CompareScreen(
            state = CompareUiState(
                priceARaw = "2.50",
                quantityARaw = "500",
                priceBRaw = "4.00",
                quantityBRaw = "800",
                outcome = ComparisonOutcome.AWins(
                    perUnitDelta = java.math.BigDecimal("0.001"),
                    percentDelta = java.math.BigDecimal("20"),
                ),
            ),
            onPriceAChange = {},
            onQuantityAChange = {},
            onPriceBChange = {},
            onQuantityBChange = {},
            onResetClick = {},
            onUndoClick = {},
            onUndoDismissed = {},
        )
    }
}
