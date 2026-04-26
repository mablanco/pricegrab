package com.mablanco.pricegrab

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mablanco.pricegrab.ui.compare.CompareScreen
import com.mablanco.pricegrab.ui.theme.PriceGrabTheme

/**
 * Activity-level wrapper. Just sets up the theme; the Compare screen
 * owns its own [androidx.compose.material3.Scaffold] (top app bar +
 * snackbar host) so this stays thin.
 */
@Composable
fun PriceGrabApp() {
    PriceGrabTheme {
        CompareScreen(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceGrabAppPreview() {
    PriceGrabApp()
}
