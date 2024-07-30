package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.ustadmobile.core.util.ext.toDisplayString


@Composable
fun UstadNullableNumberTextField(
    value: Float?,
    onValueChange: (Float?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number
    ),
    supportingText: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
) {
    var rawValue by remember {
        mutableStateOf(value?.toDisplayString() ?: "")
    }

    LaunchedEffect(value) {
        if(rawValue.toFloatOrNull() != value) {
            rawValue = value?.toDisplayString() ?: ""
        }
    }

    OutlinedTextField(
        value = rawValue,
        modifier = modifier,
        onValueChange = { text ->
            val filteredText = text.filter { it.isDigit() || it == '.' }
            rawValue = filteredText
            val floatVal = filteredText.toFloatOrNull()
            onValueChange(floatVal)
        },
        isError = isError,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        supportingText = supportingText,
        singleLine = singleLine,
    )
}

