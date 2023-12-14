package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shortcut for common text fields
 */
@Composable
fun UstadOutlinedTextField(
    value: String,
    label: String,
    errorText: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onValueChange: (String) -> Unit,
) {
    UstadInputFieldLayout(
        errorText = errorText,
        modifier = modifier,
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            label = {
                Text(label)
            },
            isError = errorText != null,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            onValueChange = onValueChange,
        )
    }

}
