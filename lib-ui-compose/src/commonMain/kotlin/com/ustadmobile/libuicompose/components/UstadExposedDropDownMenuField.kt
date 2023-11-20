package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.IconButton
import androidx.compose.material.Icon
import androidx.compose.material.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ustadmobile.core.util.MessageIdOption2
import dev.icerock.moko.resources.compose.stringResource as mrStringResource

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
) {

    var expanded by remember { mutableStateOf(false) }
    val adornmentIcon = if(expanded) {
        Icons.Filled.ArrowDropUp
    } else {
        Icons.Filled.ArrowDropDown
    }

    Box(modifier = modifier) {

        OutlinedTextField(
            modifier = modifier,
            value = value?.let { itemText(it) } ?: "",
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            isError = isError,
            enabled = enabled,
            trailingIcon = {
                IconButton(
                    enabled = enabled,
                    onClick = { expanded = !expanded }){
                    Icon(
                        adornmentIcon, "contentDescription",
                        Modifier.align(Alignment.CenterEnd)
                    )
                }
            },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.takeIf { expanded }?.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    }
                ) {
                    Text(text = itemText(option))
                }
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
    )
}
