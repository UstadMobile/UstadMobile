package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import kotlin.random.Random

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
    )
) {

    var rawValue by remember(value) {
        mutableStateOf(if(value != 0.toFloat()) value.toInt().toString() else "")
    }

    OutlinedTextField(
        value = rawValue,
        modifier = modifier,
        onValueChange = { text ->
            val filteredText = text.filter { it.isDigit() }
            rawValue = filteredText
            val floatVal = filteredText.toFloatOrNull() ?: 0.toFloat()
            onValueChange(floatVal)
        },
        isError = isError,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
    )
}

@Preview
@Composable
fun UstadNumberTextEditFieldPreview() {

    var aNumber by remember {
        mutableStateOf(0.toFloat())
    }

    Column {
        UstadNumberTextField(
            modifier = Modifier.fillMaxWidth(),
            value = aNumber,
            label = { Text("Phone Number") },
            enabled = true,
            onValueChange = {
                aNumber = it
            },
            trailingIcon = {
                Text("points")
            }
        )

        //Test to ensure that the field will update when required by viewmodel
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                aNumber = Random.nextInt(1, 6).toFloat()
            }
        ) {
            Text("Roll Dice")
        }
    }

    

}
