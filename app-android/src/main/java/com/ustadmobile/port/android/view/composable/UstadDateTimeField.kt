package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ustadmobile.port.android.util.ext.timeOfDayInMs
import java.util.*

@Composable
fun UstadDateTimeField(
    value: Long,
    dateLabel: @Composable () -> Unit,
    timeLabel: @Composable () -> Unit,
    timeZoneId: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    unsetDefault: Long = 0,
    dateFieldWeight: Float = 0.6f,
    isError: Boolean = false,
    onValueChange: (Long) -> Unit = {},
) {

    val calendar = remember(value, timeZoneId) {
        Calendar.getInstance().also {
            it.timeZone = TimeZone.getTimeZone(timeZoneId)
            it.timeInMillis = value
        }
    }

    val timeOfDayInMs = remember(value) {
        if(value != unsetDefault) {
            calendar.timeOfDayInMs
        }else {
            0
        }
    }

    val dateInMs = remember(value) {
        value - timeOfDayInMs
    }

    Row(modifier = modifier) {
        UstadDateField(
            modifier = Modifier.weight(dateFieldWeight, true)
                .padding(end = 8.dp),
            value = dateInMs,
            label = dateLabel,
            timeZoneId = timeZoneId,
            unsetDefault = unsetDefault,
            isError = isError,
            onValueChange = {
                onValueChange(it + timeOfDayInMs)
            }
        )

        UstadTimeField(
            modifier = Modifier.weight(1 - dateFieldWeight, true)
                .padding(start= 8.dp),
            value = timeOfDayInMs,
            label = timeLabel,
            enabled = enabled,
            isError = isError,
            onValueChange =  {
                onValueChange(it + dateInMs)
            }
        )
    }

}


@Preview
@Composable
fun UstadDateTimeFieldPreview() {
    UstadDateTimeField(
        value = System.currentTimeMillis(),
        dateLabel =  { Text("Date") },
        timeLabel = { Text("Time") },
        timeZoneId = TimeZone.getDefault().id
    )
}
