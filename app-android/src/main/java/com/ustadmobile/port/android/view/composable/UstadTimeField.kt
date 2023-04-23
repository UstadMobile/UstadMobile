package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
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
import androidx.compose.ui.tooling.preview.Preview
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import kotlin.math.min
import androidx.compose.material.Text
import com.google.android.material.timepicker.MaterialTimePicker
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager
import androidx.compose.material.IconButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule


class TimeVisualTransformation: VisualTransformation {

    private val mask = "hhmm"

    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if(text.length >= mask.length) text.substring(mask.indices) else text.text
        val output = buildAnnotatedString {
            for(i in mask.indices) {
                if (i < trimmed.length) {
                    append(trimmed[i])
                }else {
                    withStyle(style = SpanStyle(color = Color.LightGray)) {
                        append(mask[i])
                    }
                }

                if(i == 1){
                    withStyle(style = SpanStyle(color = Color.LightGray)) {
                        append(":")
                    }
                }

            }
        }

        val offsetmapping= object: OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if(offset <= 2) {
                    offset
                }else if(offset <= mask.length) {
                    offset + 1
                }else {
                    mask.length + 1 //text length plus ":"
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                val delta = if(offset <= 3) {
                    offset
                }else if(offset <= mask.length + 1) {
                    offset - 1
                }else {
                    mask.length
                }

                return min(delta, trimmed.length)
            }
        }

        return TransformedText(output, offsetmapping)

    }
}

@Composable
fun UstadTimeField(
    value: Int,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChange: (Int) -> Unit = {},
) {

    fun Int.toTimeString() : String{
        return if(this > 0) {
            val hours = (this / MS_PER_HOUR)
            val mins = (this.mod(MS_PER_HOUR)) / MS_PER_MIN
            "${hours.toString().padStart(2, '0')}${mins.toString().padStart(2, '0')}"
        }else {
            ""
        }

    }

    var rawValue: String by remember(value) {
        mutableStateOf(value.toTimeString())
    }

    val context = LocalContext.current

    OutlinedTextField(
        value = rawValue,
        label = label,
        modifier = modifier,
        enabled = enabled,
        onValueChange = {
            val filtered = it.filter { it.isDigit() }
            rawValue = filtered.substring(0, min(filtered.length, 4))
            if(filtered.length == 4) {
                try {
                    val hours = filtered.substring(0, 2).toInt()
                    val mins = filtered.substring(2, 4).toInt()
                    if(hours >= 0 && hours <= 23 && mins >= 0 && mins <= 59)
                        onValueChange((hours * MS_PER_HOUR) + (mins * MS_PER_MIN))
                }catch(e: Exception) {
                    //something not a valid time
                }
            }else if(filtered.isEmpty()) {
                onValueChange(0)
            }
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    val supportFragmentManager = context.getContextSupportFragmentManager()
                    MaterialTimePicker.Builder()
                        .build()
                        .apply {
                            addOnPositiveButtonClickListener {
                                onValueChange(((hour * MS_PER_HOUR) + (minute * MS_PER_MIN)))
                            }
                        }
                        .show(supportFragmentManager, "timePickerTag")
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = "",
                )
            }
        },
        visualTransformation = TimeVisualTransformation(),
        maxLines = 1,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )

}


@Preview
@Composable
fun UstadTimeFieldPreview(){

    var time: Int by remember {
        mutableStateOf(10 * MS_PER_HOUR)
    }

    UstadTimeField(
        value = time,
        label = { Text("Time") },
        onValueChange = {
            time = it
        }
    )


}