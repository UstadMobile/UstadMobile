package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.StringAndSerialNum
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.compose.rememberFormattedDate
import java.util.*

@Composable
fun UstadEditField(
    modifier: Modifier = Modifier,
    error: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        content()

        if(error != null) {
            Text(text = error,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

/**
 * Base edit text field for fields.
 *
 * Note: onClick uses a workaround to trigger the onClick handler.
 *
 * @param error the error text to be displayed. This error message will be cleared as soon as the
 * user starts typing. This isn't strictly reactive to follow the viewmodel, but avoids the need to
 * add a lot of new viewmodel event functions
 */
@Deprecated("This is trying to do too much and needs split up. Where possible, use the standard OutlinedTextField")
@Composable
fun UstadTextEditField(
    value: String,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    error: String? = null,
    errorString: StringAndSerialNum? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
    password: Boolean = false,
    suffixText: String? = null,
    singleLine: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    var errorText by remember(errorString) {
        mutableStateOf(errorString?.toString())
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    if(onClick != null){
        val isPressed = interactionSource.collectIsPressedAsState().value
        LaunchedEffect(isPressed){
            if(isPressed) {
                onClick()
            }
        }
    }

    UstadEditField(
        modifier = modifier,
        error = errorText,
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            value = value,
            onValueChange = {
                errorText = null
                onValueChange(it)
            },
            isError = errorText != null,
            enabled = enabled,
            interactionSource = interactionSource,
            readOnly = readOnly,
            keyboardActions = keyboardActions,
            visualTransformation = if (password && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            singleLine = singleLine,
            trailingIcon = if(password) {
                {
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        val icon = if(passwordVisible) {
                            Icons.Filled.VisibilityOff
                        }else {
                            Icons.Filled.Visibility
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = stringResource(R.string.toggle_visibility)
                        )
                    }
                }
            }else if(suffixText != null) {
                {
                    Text(
                        text = suffixText,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }else {
                null
            },
            keyboardOptions = keyboardOptions,
        )
    }
}

@Preview
@Composable
fun UstadTextEditFieldPreview() {
    UstadTextEditField(
        value = "bob@email.com",
        label = "Email",
        onValueChange = {},
        error =null,
        enabled = true,
    )
}

@Preview
@Composable
fun UstadTextEditFieldSuffixPreview() {
    var maxScore: Int by remember { mutableStateOf(42) }

    UstadTextEditField(
        value = "42",
        label = "Maximum score",
        onValueChange = { newString ->
            maxScore = newString.filter { it.isDigit() }.toIntOrNull() ?: 0
        },
        error =null,
        enabled = true,
        suffixText = "points",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Preview
@Composable
fun UstadTextEditPasswordPreview() {
    var passwordText by remember {
        mutableStateOf("secret")
    }

    UstadTextEditField(
        value = passwordText,
        label = "password",
        onValueChange = {
            passwordText = it
        },
        error = null,
        enabled = true,
        password = true,
    )
}

@Composable
@Deprecated("Should use UstadDateField")
fun UstadDateEditTextField(
    value: Long,
    label: String,
    timeZoneId: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @Suppress("UNUSED_PARAMETER") //Reserved for future use
    error: String? = null,
    onValueChange: (Long) -> Unit = {},
) {

    val dateFormatted = rememberFormattedDate(
        timeInMillis = value,
        timeZoneId = timeZoneId
    )

    val context = LocalContext.current

    UstadClickableTextField(
        modifier = modifier,
        value = dateFormatted,
        enabled = enabled,
        label = { Text(label) },
        readOnly = true,
        onValueChange = {

        },
        onClick = { },
    )
}

@Preview
@Composable
private fun UstadDateTextFieldPreview() {
    UstadDateEditTextField(
        value = System.currentTimeMillis(),
        label = "Date",
        enabled = true,
        timeZoneId = TimeZone.getDefault().id
    )
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun <T> UstadExposedDropDownMenuField(
    value: T?,
    label: String,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    itemText: @Composable (T) -> String,
    enabled: Boolean = true,
) {

    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        modifier = modifier,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = value?.let { itemText(it) } ?: "",
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            isError = isError,
            enabled = enabled,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded ,
            onDismissRequest = {
                expanded = false
            }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    }
                ) {
                    Text(text = itemText(option))
                }
            }
        }
    }
}

@Composable
fun UstadMessageIdOptionExposedDropDownMenuField(
    value: Int,
    label: String,
    options: List<MessageIdOption2>,
    onOptionSelected: (MessageIdOption2) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    UstadExposedDropDownMenuField(
        value = options.firstOrNull { it.value == value },
        label = label,
        options = options,
        onOptionSelected = onOptionSelected,
        itemText = { messageIdResource(id = it.messageId) },
        modifier = modifier,
        isError = isError,
        enabled = enabled,
    )
}

@Preview
@Composable
private fun UstadExposedDropDownMenuFieldPreview() {
    var selectedOption by remember {
        mutableStateOf("Coffee")
    }

    UstadExposedDropDownMenuField<String>(
        value = selectedOption,
        label = "Drink",
        options = listOf("Coffee", "Tea"),
        onOptionSelected = {
            selectedOption = it
        },
        itemText =  { it },
    )
}

