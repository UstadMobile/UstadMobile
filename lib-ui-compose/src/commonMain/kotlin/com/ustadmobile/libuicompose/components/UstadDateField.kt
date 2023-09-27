package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import com.ustadmobile.core.MR
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import com.ustadmobile.core.util.ext.isDateSet
import dev.icerock.moko.resources.compose.stringResource
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

/**
 * As per https://developer.android.com/reference/kotlin/androidx/compose/ui/text/input/VisualTransformation
 */
class DateVisualTransformation: VisualTransformation {
    private val mask = "ddmmyyyy"

    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if(text.length >= mask.length) text.substring(0..7) else text.text
        val output = buildAnnotatedString {
            for (i in 0 until 8) {
                if(i < trimmed.length) {
                    append(trimmed[i])
                }else {
                    withStyle(style = SpanStyle(color = Color.LightGray)) {
                        append(mask[i])
                    }
                }

                //Add separators
                if(i == 1 || i == 3) {
                    withStyle(style = SpanStyle(color = Color.LightGray)) {
                        append("/")
                    }
                }
            }

        }

        val offsetMapping = object: OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if(offset <= 2)
                    offset
                else if(offset <= 4)
                    offset + 1
                else if(offset <= 8)
                    offset + 2
                else
                    10

            }

            override fun transformedToOriginal(offset: Int): Int {
                val delta = if(offset <= 3)
                    offset
                else if(offset <= 5)
                    offset -1
                else if(offset <= 9)
                    offset - 2
                else
                    8


                return min(delta, trimmed.length)
            }
        }

        return TransformedText(output, offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UstadDateField(
    value: Long,
    label: @Composable () -> Unit,
    timeZoneId: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValueChange: (Long) -> Unit = {},
    unsetDefault: Long = 0,
) {

    val timeZone = remember(timeZoneId) {
        TimeZone.getTimeZone(timeZoneId)
    }

    val dateFormatter = remember(timeZoneId) {
        SimpleDateFormat("ddMMyyyy").also {
            it.timeZone = timeZone
        }
    }

    fun Long.toDateString(): String {
        return if(isDateSet()) {
            dateFormatter.format(Date(this))
        }else {
            ""
        }
    }

    var rawValue: String by remember(value) {
        mutableStateOf(value.toDateString())
    }

    val datePickerState = rememberDatePickerState()
    var isDialogOpen by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = modifier,
        value = rawValue,
        enabled = enabled,
        isError = isError,
        onValueChange = {
            val filtered = it.filter { it.isDigit() }
            rawValue = filtered.substring(0, min(filtered.length, 8))
            if(filtered.length == 8) {
                try {
                    dateFormatter.parse(rawValue)?.time?.also { time ->
                        onValueChange(time)
                    }
                }catch(e: Exception) {
                    //probably not valid
                }
            }else if(filtered.isEmpty()) {
                onValueChange(unsetDefault)
            }

        },
        label = label,
        visualTransformation = DateVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            IconButton(
                onClick = {
                    isDialogOpen = !isDialogOpen
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = "",
                )
            }
        }
    )

    if (isDialogOpen) {
        DatePickerDialog(
            onDismissRequest = {
                isDialogOpen = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { time ->
                            val offset = TimeZone.getTimeZone(timeZoneId).getOffset(time)
                            onValueChange(time + offset)
                            rawValue = time.toDateString()
                        }

                        isDialogOpen = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text(stringResource(MR.strings.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isDialogOpen = false
                    }
                ) {
                    Text(stringResource(MR.strings.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

}