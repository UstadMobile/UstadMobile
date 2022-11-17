package com.ustadmobile.port.android.view.composable

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.datepicker.MaterialDatePicker
import com.toughra.ustadmobile.R
import com.ustadmobile.core.util.StringAndSerialNum
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.compose.rememberFormattedDate
import com.ustadmobile.port.android.util.ext.getActivityContext

@Composable
fun UstadEditField(
    modifier: Modifier = Modifier,
    error: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
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

    UstadEditField(modifier, errorText) {
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
            visualTransformation = if (password && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
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
            }else {
                null
            }
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
fun UstadDateEditTextField(
    value: Long,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @Suppress("UNUSED_PARAMETER") //Reserved for future use
    timezoneId: String = "UTC",
    error: String? = null,
    onValueChange: (Long) -> Unit = {},
) {
    var errorText by remember(error) {
        mutableStateOf(error)
    }

    val dateFormatted = rememberFormattedDate(timeInMillis = value)
    val context = LocalContext.current

    UstadTextEditField(
        value = dateFormatted,
        label = label,
        enabled = enabled,
        modifier = modifier,
        onValueChange = {},
        readOnly = true,
        onClick = {
            val supportFragmentManager =
                (context.getActivityContext() as AppCompatActivity)
                    .supportFragmentManager
            MaterialDatePicker.Builder
                .datePicker()
                .build()
                .apply {
                    addOnPositiveButtonClickListener {
                        onValueChange(it)
                        errorText = null
                    }
                }
                .show(supportFragmentManager, "tag")
        }
    )
}

@Preview
@Composable
private fun UstadDateTextFieldPreview() {
    UstadDateEditTextField(
        value = System.currentTimeMillis(),
        label = "Date",
        enabled = true
    )
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun <T> UstadExposedDropDownMenuField(
    value: T?,
    label: String,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    itemText: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
) {

    var errorText: String? by remember {
        mutableStateOf(error)
    }

    UstadEditField(
        modifier = modifier,
        error = error,
    ) {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
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
                isError = errorText != null,
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
                            errorText = null
                            onOptionSelected(option)
                        }
                    ) {
                        Text(text = itemText(option))
                    }
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
    error: String? = null,
) {
    UstadExposedDropDownMenuField(
        value = options.firstOrNull { it.value == value },
        label = label,
        options = options,
        onOptionSelected,
        itemText = { messageIdResource(id = it.messageId) },
        modifier = modifier,
        error = error,
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

