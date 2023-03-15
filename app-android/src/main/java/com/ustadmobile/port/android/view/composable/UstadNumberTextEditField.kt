package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun UstadNumberTextEditField(
    value: Int,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    error: String? = null,
    suffixText: String? = null,
    onValueChange: (Int) -> Unit,
) {

    var errorText by remember {
        mutableStateOf(error)
    }

    var strValue: String by remember {
        mutableStateOf(if(value != 0) value.toString() else "")
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(
            value = strValue,
            modifier = modifier,
            onValueChange = { text ->
                errorText = null
                strValue = text
                val intVal = text.filter { it.isDigit() }.toIntOrNull() ?: 0
                onValueChange(intVal)
            },
            label = { Text(text = label) },
            placeholder = { Text(text = label) },
            isError = errorText != null,
            enabled = enabled,
            trailingIcon = if(suffixText != null) {
                {
                    Text(
                        text = suffixText,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
        )

        if (errorText != null) {
            UstadErrorText(error = error ?: "")
        }
    }
}

@Preview
@Composable
fun UstadNumberTextEditFieldPreview() {

    UstadNumberTextEditField(
        modifier = Modifier.fillMaxWidth(),
        value = 0,
        label = "Phone Number",
        error = "Not Valid",
        enabled = true,
        onValueChange = {

        },
        suffixText = "points"
    )
}
