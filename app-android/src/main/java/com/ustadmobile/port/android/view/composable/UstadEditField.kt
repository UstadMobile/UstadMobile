package com.ustadmobile.port.android.view.composable

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.datepicker.MaterialDatePicker
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
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
) {
    var errorText by remember(error) {
        mutableStateOf(error)
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

@Composable
fun UstadDateEditTextField(
    value: Long,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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


