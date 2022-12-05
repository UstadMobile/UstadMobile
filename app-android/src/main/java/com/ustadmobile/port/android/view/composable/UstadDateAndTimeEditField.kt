package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ustadmobile.port.android.util.ext.timeOfDayInMs
import java.util.*

/**
 * A single composable that provides both a date picker and a time picker. Emits the combination as
 * a single Long (time in millis since epoch).
 */
@Composable
fun UstadDateTimeEditTextField(
    value: Long,
    dateLabel: String,
    timeLabel: String,
    timeZoneId: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @Suppress("UNUSED_PARAMETER") //Reserved for future use
    error: String? = null,
    onValueChange: (Long) -> Unit = {},
) {

    val calendar = remember(value, timeZoneId) {
        Calendar.getInstance().also {
            it.timeZone = TimeZone.getTimeZone(timeZoneId)
            it.timeInMillis = value
        }
    }

    val timeOfDayInMs = remember(value) {
        calendar.timeOfDayInMs
    }

    val dateInMs = remember(value) {
        value - timeOfDayInMs
    }

    Row(modifier = modifier) {
        UstadDateEditTextField(
            modifier = Modifier.weight(0.7f, true)
                .padding(end = 8.dp),
            value = dateInMs,
            label = dateLabel,
            enabled = enabled,
            timeZoneId = timeZoneId,
            error = error,
            onValueChange = {
                onValueChange(it + timeOfDayInMs)
            }
        )

        UstadTimeEditTextField(
            modifier = Modifier.weight(0.3f, true)
                .padding(start= 8.dp),
            value = timeOfDayInMs,
            label = timeLabel,
            enabled = enabled,
            onValueChange =  {
                onValueChange(it + dateInMs)
            }
        )

    }
}

@Preview
@Composable
fun UstadDateTimeEditTextFieldPreview() {
    UstadDateTimeEditTextField(
        value = System.currentTimeMillis(),
        dateLabel =  "Date",
        timeLabel = "Time",
        timeZoneId = TimeZone.getDefault().id
    )
}
