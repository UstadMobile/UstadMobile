package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.libuicompose.util.ext.timeOfDayInMs
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
    dateSupportingText: (@Composable () -> Unit)? = null,
    timeSupportingText: (@Composable () -> Unit)? = null,
) {

    val calendar = remember(value, timeZoneId) {
        Calendar.getInstance().also {
            it.timeZone = TimeZone.getTimeZone(timeZoneId)
            it.timeInMillis = value
        }
    }

    val timeOfDayInMs = remember(value) {
        if(value != unsetDefault && value.isDateSet()) {
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
            },
            supportingText = dateSupportingText,
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
            },
            supportingText = timeSupportingText,
        )
    }

}
