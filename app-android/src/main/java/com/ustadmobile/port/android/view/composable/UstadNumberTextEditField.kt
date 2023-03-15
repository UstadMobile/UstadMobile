package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
    onValueChange: (String) -> Unit,
) {

    var valueRemember by remember {
        mutableStateOf(value)
    }

    var isError by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(
            value = value,
            onValueChange = { newText ->
                valueRemember = newText
                isError = false
                onValueChange(newText)
            },
            label = { Text(text = label) },
            placeholder = { Text(text = label) },
            isError = error != null,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // validate here
                    isError = validateAge(inputText = value)
                }
            )
        )

        if (error != null) {
            UstadErrorText(error = error)
        }
    }
}

private fun validateAge(inputText: String): Boolean {
    return inputText.toInt() < 18
}

@Preview
@Composable
fun UstadNumberTextEditFieldPreview() {
    UstadNumberTextEditField(
        value = "bob@email.com",
        label = "Email",
        error =null,
        enabled = true,
        onValueChange = {}
    )
}
