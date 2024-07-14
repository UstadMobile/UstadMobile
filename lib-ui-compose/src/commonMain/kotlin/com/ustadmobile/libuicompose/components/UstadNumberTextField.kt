package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.ustadmobile.core.util.ext.toDisplayString

/**
 * See
 *  https://dev.to/pchmielowski/jetpack-compose-textfield-which-accepts-and-emits-value-other-than-string-1g3o
 *
 *  @param value Float - decimal point values are not currently supported, however this will be
 *  an option in future. Hence type is set as float.
 */
@Composable
fun UstadNumberTextField(
    value: Float,
    onValueChange: (Float) -> Unit,
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
    unsetDefault: Float = 0f,
    singleLine: Boolean = true,
) {
    fun Float.numberString() = if(this == unsetDefault) "" else toDisplayString()

    var rawValue by remember {
        mutableStateOf(value.numberString())
    }

    LaunchedEffect(value) {
        if((rawValue.toFloatOrNull() ?: unsetDefault) != value) {
            rawValue = value.numberString()
        }
    }

    OutlinedTextField(
        value = rawValue,
        modifier = modifier,
        onValueChange = { text ->
            val filteredText = text.filter { it.isDigit() || it == '.' }
            rawValue = filteredText
            val floatVal = filteredText.toFloatOrNull() ?: unsetDefault
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
