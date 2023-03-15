package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun UstadNumberTextEditField(
    value: Int,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    error: String? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    onValueChange: (Int) -> Unit,
) {

    var strValue: String by remember {
        mutableStateOf(if(value != 0) value.toString() else "")
    }

    OutlinedTextField(
        value = strValue,
        modifier = modifier,
        onValueChange = { text ->
            strValue = text
            val intVal = text.filter { it.isDigit() }.toIntOrNull() ?: 0
            onValueChange(intVal)
        },
        isError = error != null,
        label = { Text(text = label) },
        placeholder = { Text(text = label) },
        enabled = enabled,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
    )
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
        trailingIcon = {
            Text("points")
        }
    )
}
