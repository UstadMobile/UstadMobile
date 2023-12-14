package com.ustadmobile.libuicompose.components

import android.view.LayoutInflater
import android.widget.DatePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.ustadmobile.libuicompose.R
import java.util.*

/**
 * Date picker spinner (wraps AndroidView and inflating R.layout.datepicker_spinner)
 */
@Suppress("unused") //Is used in downstream dev-mvvm branches
@Composable
fun DatePickerSpinner(
    date: Long,
    onSetDate: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val calendar: Calendar = remember(date) {
        Calendar.getInstance().apply {
            timeInMillis = date
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LayoutInflater.from(context).inflate(R.layout.datepicker_spinner, null, false).apply {
                this as DatePicker
                init(calendar[Calendar.YEAR],
                        calendar[Calendar.MONTH],
                        calendar[Calendar.DAY_OF_MONTH]
                ) { _, year, monthOfYear, dayOfMonth ->
                    calendar[Calendar.YEAR] = year
                    calendar[Calendar.MONTH] = monthOfYear
                    calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                    onSetDate(calendar.timeInMillis)
                }
            }
        },
        update = { view ->
            view as DatePicker
            calendar.timeInMillis = date
            if(view.year != calendar[Calendar.YEAR] || view.month != calendar[Calendar.MONTH]
                    || view.dayOfMonth != calendar[Calendar.DAY_OF_MONTH]) {
                view.updateDate(calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH])
            }
        }
    )
}
