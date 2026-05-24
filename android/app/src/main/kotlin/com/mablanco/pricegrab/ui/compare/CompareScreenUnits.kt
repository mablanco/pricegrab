package com.mablanco.pricegrab.ui.compare

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mablanco.pricegrab.R
import com.mablanco.pricegrab.core.model.QuantityUnit

// Room for three-letter codes (uds) plus trailing chevron with inset padding.
internal val UNIT_SELECTOR_WIDTH = 100.dp

private val UNIT_FIELD_TRAILING_ICON_PADDING = 8.dp

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
            // Invisible twin of the quantity label so both OutlinedTextFields share
            // the same vertical structure and their boxes line up in the Row.
            label = {
                Text(
                    text = stringResource(R.string.quantity_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Transparent,
                    modifier = Modifier.clearAndSetSemantics { },
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            trailingIcon = {
                Box(Modifier.padding(end = UNIT_FIELD_TRAILING_ICON_PADDING)) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
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
                val unitLabel = stringResource(unit.nameRes())
                DropdownMenuItem(
                    text = { Text(stringResource(unit.codeRes())) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    },
                    modifier = Modifier.semantics {
                        contentDescription = unitLabel
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
