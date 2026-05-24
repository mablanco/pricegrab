package com.mablanco.pricegrab.ui.compare

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mablanco.pricegrab.R
import com.mablanco.pricegrab.core.model.QuantityUnit

// Short unit codes (g, kg, ml, L, pcs) fit in a fixed slot so the quantity
// field keeps the majority of the row width on narrow phones.
internal val UNIT_SELECTOR_WIDTH = 72.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuantityUnitSelector(
    offerTitle: String,
    selectedUnit: QuantityUnit,
    onUnitSelected: (QuantityUnit) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val unitDescription = stringResource(R.string.cd_quantity_unit, offerTitle)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.requiredWidth(UNIT_SELECTOR_WIDTH),
    ) {
        OutlinedTextField(
            value = stringResource(selectedUnit.codeRes()),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = null,
            textStyle = MaterialTheme.typography.bodyLarge,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .testTag(testTag)
                .semantics { contentDescription = unitDescription },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            QuantityUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(stringResource(unit.nameRes())) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@StringRes
internal fun QuantityUnit.codeRes(): Int = when (this) {
    QuantityUnit.Gram -> R.string.unit_code_g
    QuantityUnit.Kilogram -> R.string.unit_code_kg
    QuantityUnit.Millilitre -> R.string.unit_code_ml
    QuantityUnit.Litre -> R.string.unit_code_L
    QuantityUnit.Piece -> R.string.unit_code_pcs
}

@StringRes
internal fun QuantityUnit.nameRes(): Int = when (this) {
    QuantityUnit.Gram -> R.string.unit_name_gram
    QuantityUnit.Kilogram -> R.string.unit_name_kilogram
    QuantityUnit.Millilitre -> R.string.unit_name_millilitre
    QuantityUnit.Litre -> R.string.unit_name_litre
    QuantityUnit.Piece -> R.string.unit_name_piece
}
