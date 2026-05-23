package com.mablanco.pricegrab.ui.compare

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mablanco.pricegrab.core.model.ComparisonOutcome
import com.mablanco.pricegrab.ui.theme.PriceGrabTheme

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
            onQuantityUnitAChange = {},
            onQuantityUnitBChange = {},
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
            onQuantityUnitAChange = {},
            onQuantityUnitBChange = {},
            onResetClick = {},
            onUndoClick = {},
            onUndoDismissed = {},
        )
    }
}
