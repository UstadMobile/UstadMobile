package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun UstadNumberTextEditField(
    value: String,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    error: String? = null,
    maxValue: Int? = null,
    minValue: Int? = null,
    onValueChange: (String) -> Unit,
) {

    var valueRemember by remember {
        mutableStateOf(value)
    }

    var isError by remember {
        mutableStateOf(error != null)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(
            value = value,
            modifier = modifier,
            onValueChange = { newText ->
                valueRemember = newText
                isError = false
                onValueChange(newText)
            },
            label = { Text(text = label) },
            placeholder = { Text(text = label) },
            isError = isError,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // validate here
                    isError = validateNumber(maxValue, minValue, inputText = value)
                }
            )
        )

        if (isError) {
            UstadErrorText(error = error ?: "")
        }
    }
}

private fun validateNumber(maxValue: Int?, minValue: Int?, inputText: String): Boolean {
    return (inputText.toInt() < (maxValue ?: Int.MAX_VALUE)) &&
            (inputText.toInt() > (minValue ?: Int.MIN_VALUE))
}

@Preview
@Composable
fun UstadNumberTextEditFieldPreview() {
    UstadNumberTextEditField(
        modifier = Modifier.fillMaxWidth(),
        value = "45",
        label = "Phone Number",
        error = "Not Valid",
        enabled = true,
        onValueChange = {}
    )
}
