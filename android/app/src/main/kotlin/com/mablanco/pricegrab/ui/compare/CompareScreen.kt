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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
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
import com.mablanco.pricegrab.ui.theme.spacing
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
        topBar = { CompareTopBar(enabled = state.isResetEnabled, onResetClick = onResetClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        CompareContent(
            state = state,
            onPriceAChange = onPriceAChange,
            onQuantityAChange = onQuantityAChange,
            onPriceBChange = onPriceBChange,
            onQuantityBChange = onQuantityBChange,
            priceAFocusRequester = priceAFocusRequester,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

/**
 * FR-005.3: move keyboard focus to Price A whenever a *fresh* reset
 * starts a new UndoState. Keyed on the deadline so the effect refires
 * for each new reset (each has a unique deadline) but does not steal
 * focus on rotation (rotation preserves the deadline).
 */
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

/**
 * Drive the undo Snackbar from the active [UndoState]
 * (research.md §3 + §4). Always shown with
 * `SnackbarDuration.Indefinite` and bounded by `withTimeoutOrNull
 * (remaining)`, where `remaining = deadline - now()`. The wrapper
 * uses the coroutine clock, so the lifetime is honoured verbatim
 * across configuration changes (after rotation, a smaller `remaining`
 * is computed and the Snackbar shows for exactly that long instead
 * of restarting from a full 10 s).
 */
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
            // No active Undo: hide any Snackbar still on screen so the
            // typing-dismisses-undo path closes the surface immediately.
            snackbarHostState.currentSnackbarData?.dismiss()
            return@LaunchedEffect
        }
        val remaining = undoState.expiresAtEpochMillis - System.currentTimeMillis()
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompareTopBar(enabled: Boolean, onResetClick: () -> Unit) {
    val resetDescription = stringResource(R.string.reset_action_description)
    CenterAlignedTopAppBar(
        title = {
            // Brandmark glyph + plain-text title rendered as a single Row so
            // they read as one composite "PriceGrab" mark to TalkBack (the
            // Icon is decorative — `contentDescription = null` keeps it out
            // of the merged semantics tree, the Text carries the
            // announcement).
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
                onClick = onResetClick,
                enabled = enabled,
                modifier = Modifier
                    .testTag(TEST_TAG_RESET)
                    .semantics { contentDescription = resetDescription },
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    // contentDescription set on the IconButton's
                    // semantics so TalkBack reads
                    // "Clear all fields and start a new comparison".
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
private fun CompareContent(
    state: CompareUiState,
    onPriceAChange: (String) -> Unit,
    onQuantityAChange: (String) -> Unit,
    onPriceBChange: (String) -> Unit,
    onQuantityBChange: (String) -> Unit,
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

        ResultRegion(outcome = state.outcome)
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
    val spacing = MaterialTheme.spacing
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(spacing.l),
            verticalArrangement = Arrangement.spacedBy(spacing.m),
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

/**
 * Outer wrapper for the result region. Always emitted so the polite live
 * region (and its `result` test tag) stays in the merged tree across
 * cold launch → typing → reset transitions, even when the inner content
 * switches between the placeholder hint and the elevated hero card.
 *
 * - When `outcome == null`: render a soft placeholder Text (no Card frame).
 * - When `outcome != null`: render the [HeroResultCard].
 *
 * Either branch announces its content via the same content description on
 * the wrapper, so TalkBack speaks one full thought per state change.
 */
@Composable
private fun ResultRegion(outcome: ComparisonOutcome?) {
    val configuration = LocalConfiguration.current
    val locale = ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.getDefault()

    val placeholder = stringResource(R.string.result_placeholder)
    val headline = outcome?.headlineRes()?.let { stringResource(it) }
    val savings = ResultPresenter.present(outcome, locale)
    val savingsLine: String? = savings?.let {
        stringResource(R.string.result_savings, it.perUnitDelta)
    }
    val a11ySummary: String = when {
        headline == null -> placeholder
        savingsLine != null -> "$headline. $savingsLine"
        else -> headline
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TEST_TAG_RESULT)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = a11ySummary
            },
    ) {
        if (outcome == null) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            HeroResultCard(
                outcome = outcome,
                headline = headline ?: "",
                savingsLine = savingsLine,
            )
        }
    }
}

/**
 * The polished, elevated result card introduced by feature 003 / US2.
 *
 * Visual treatment:
 * - [ElevatedCard] surface (a tonal lift over the offer cards' standard
 *   [Card], so the result reads as the hero of the screen).
 * - Leading icon: `Icons.Filled.Check` for a winner, the custom
 *   `ic_tie_glyph` (= sign) for a tie. Both are decorative — the
 *   announcement comes from the headline + savings text on the
 *   wrapper's content description.
 * - Headline in `headlineSmall` (Bold via [PriceGrabTypography]) with
 *   `Modifier.semantics { heading() }` so TalkBack reads it as a
 *   heading.
 * - Body in `bodyLarge` for the per-unit savings line; collapsed when
 *   savings are not applicable (i.e., a tie).
 */
@Composable
private fun HeroResultCard(
    outcome: ComparisonOutcome,
    headline: String,
    savingsLine: String?,
) {
    val spacing = MaterialTheme.spacing
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.s)
            .testTag(TEST_TAG_HERO_RESULT),
        elevation = CardDefaults.elevatedCardElevation(),
    ) {
        Row(
            modifier = Modifier.padding(spacing.l),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.m),
        ) {
            HeroResultIcon(outcome)
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .testTag(TEST_TAG_RESULT_TEXT)
                        .semantics { heading() },
                )
                if (savingsLine != null) {
                    Text(
                        text = savingsLine,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag(TEST_TAG_RESULT_SAVINGS),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroResultIcon(outcome: ComparisonOutcome) {
    val tint = MaterialTheme.colorScheme.primary
    when (outcome) {
        is ComparisonOutcome.AWins, is ComparisonOutcome.BWins -> Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = tint,
        )
        ComparisonOutcome.Tie -> Icon(
            painter = painterResource(R.drawable.ic_tie_glyph),
            contentDescription = null,
            tint = tint,
        )
    }
}

@StringRes
private fun InputError.messageRes(): Int = when (this) {
    InputError.NotANumber -> R.string.error_not_a_number
    InputError.NegativePrice -> R.string.error_negative_price
    InputError.NonPositiveQuantity -> R.string.error_non_positive_quantity
}

@StringRes
private fun ComparisonOutcome.headlineRes(): Int = when (this) {
    ComparisonOutcome.Tie -> R.string.result_tied
    is ComparisonOutcome.AWins -> R.string.result_winner_a
    is ComparisonOutcome.BWins -> R.string.result_winner_b
}

// ---- Test tags (constants so tests can reference them) ----------------------

const val TEST_TAG_OFFER_A: String = "offerA"
const val TEST_TAG_OFFER_B: String = "offerB"

// The outer result region (always emitted, hosts the polite live region).
const val TEST_TAG_RESULT: String = "result"

// The elevated hero card (only emitted when a comparison result exists).
const val TEST_TAG_HERO_RESULT: String = "heroResult"

const val TEST_TAG_RESULT_TEXT: String = "result_text"
const val TEST_TAG_RESULT_SAVINGS: String = "result_savings"
const val TEST_TAG_RESET: String = "reset_action"
const val TEST_TAG_BRANDMARK: String = "brandmark"

// ---- Layout constants -------------------------------------------------------

// Material 3's default top-app-bar leading icon slot is 24dp; we keep the
// brandmark at the same size so it visually aligns with the trailing reset
// IconButton's 24dp glyph and stays inside the 64dp app-bar height.
private val BRANDMARK_SIZE = 24.dp

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
