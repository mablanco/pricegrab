package com.mablanco.pricegrab

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mablanco.pricegrab.ui.compare.CompareScreen
import com.mablanco.pricegrab.ui.theme.PriceGrabTheme

@Composable
fun PriceGrabApp() {
    PriceGrabTheme {
        Scaffold { innerPadding ->
            CompareScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceGrabAppPreview() {
    PriceGrabApp()
}
