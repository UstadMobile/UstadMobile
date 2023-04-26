package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Workaround to make a textfield clickable. See
 * https://issuetracker.google.com/issues/172154008
 *
 * As per https://caelis.medium.com/jetpack-compose-datepicker-textfield-39808e42646a
 */
@Composable
fun UstadClickableTextField(
    value: String,
    onClick: () -> Unit,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    shape: Shape = MaterialTheme.shapes.small,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors()
) {
    Box {
       OutlinedTextField(
           value = value,
           onValueChange = onValueChange,
           modifier = modifier,
           enabled = enabled,
           readOnly = readOnly,
           textStyle = textStyle,
           label = label,
           placeholder = placeholder,
           leadingIcon = leadingIcon,
           trailingIcon = trailingIcon,
           isError = isError,
           visualTransformation = visualTransformation,
           keyboardOptions = keyboardOptions,
           keyboardActions = keyboardActions,
           singleLine = singleLine,
           maxLines = maxLines,
           shape = shape,
           colors = colors,
       )

        Box(
            modifier = Modifier.matchParentSize()
                .alpha(0f)
                .clickable(onClick = onClick)
        )
    }
}