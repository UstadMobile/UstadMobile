package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ustadmobile.core.util.StringAndSerialNum

@Composable
fun UstadNumberTextEditField(
    value: String,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    error: String? = null,
    errorString: StringAndSerialNum? = null,
    onClick: () -> Unit,
    suffixText: String? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    var errorText by remember(errorString) {
        mutableStateOf(errorString?.toString())
    }

    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    LaunchedEffect(isPressed){
        if(isPressed) {
            onClick()
        }
    }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        value = value,
        onValueChange = { },
        isError = errorText != null,
        enabled = enabled,
        interactionSource = interactionSource,
        readOnly = true,
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions,
    )
}

@Preview
@Composable
fun UstadNumberTextEditFieldPreview() {
    UstadNumberTextEditField(
        value = "bob@email.com",
        label = "Email",
        error =null,
        enabled = true,
        onClick = {}
    )
}
