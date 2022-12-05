package com.ustadmobile.port.android.view.composable

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.timepicker.MaterialTimePicker
import com.ustadmobile.port.android.util.compose.rememberFormattedTime
import com.ustadmobile.port.android.util.ext.MS_PER_HOUR
import com.ustadmobile.port.android.util.ext.MS_PER_MIN
import com.ustadmobile.port.android.util.ext.getActivityContext

/**
 * Ustad standard time edit field for Jetpack compose. This will launch a picker dialog when clicked.
 *
 * @param value the time of day in milliseconds since midnight.
 * @param label text field label
 *
 */
@Composable
fun UstadTimeEditTextField(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @Suppress("UNUSED_PARAMETER") //Reserved for future use
    error: String? = null,
    onValueChange: (Int) -> Unit = {},
) {
    val timeFormatted = rememberFormattedTime(timeInMs = value)
    val context = LocalContext.current

    UstadTextEditField(
        value = timeFormatted,
        label = label,
        enabled = enabled,
        modifier = modifier,
        onValueChange = {},
        readOnly = true,
        error = error,
        onClick = {
            val supportFragmentManager =
                (context.getActivityContext() as AppCompatActivity)
                    .supportFragmentManager
            MaterialTimePicker.Builder()
                .build()
                .apply {
                    addOnPositiveButtonClickListener {
                        onValueChange(((hour * MS_PER_HOUR) + (minute * MS_PER_MIN)))
                    }
                }
                .show(supportFragmentManager, "timePickerTag")
        }
    )
}


@Composable
@Preview
fun UstadTimeEditTextFieldPreview() {
    UstadTimeEditTextField(
        value = (14 * MS_PER_HOUR) + (30 * MS_PER_MIN),
        label = "Time"
    )
}