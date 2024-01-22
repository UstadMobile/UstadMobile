package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.util.MessageIdOption2
import dev.icerock.moko.resources.compose.stringResource as mrStringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> UstadExposedDropDownMenuField(
    value: T?,
    label: String,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    itemText: @Composable (T) -> String,
    enabled: Boolean = true,
    supportingText: (@Composable () -> Unit)? = null,
) {

    //As per
    // https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#ExposedDropdownMenuBox(kotlin.Boolean,kotlin.Function1,androidx.compose.ui.Modifier,kotlin.Function1)

    var expanded by remember { mutableStateOf(false) }


    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {

        OutlinedTextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            enabled = enabled,
            value = if(value != null) {
                itemText(value)
            }else {
                ""
            },
            label = {
                Text(label)
            },
            onValueChange = { },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            isError = isError,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            supportingText = supportingText,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false}
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(if(item != null) itemText(item) else "") },
                    onClick = {
                        expanded = false
                        onOptionSelected(item)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }

    }
}

@Composable
fun UstadMessageIdOptionExposedDropDownMenuField(
    value: Int,
    label: String,
    options: List<MessageIdOption2>,
    onOptionSelected: (MessageIdOption2) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    supportingText: (@Composable () -> Unit)? = null,
) {
    UstadExposedDropDownMenuField(
        value = options.firstOrNull { it.value == value },
        label = label,
        options = options,
        onOptionSelected = onOptionSelected,
        itemText = { mrStringResource(resource = it.stringResource) },
        modifier = modifier,
        isError = isError,
        enabled = enabled,
        supportingText = supportingText,
    )
}
