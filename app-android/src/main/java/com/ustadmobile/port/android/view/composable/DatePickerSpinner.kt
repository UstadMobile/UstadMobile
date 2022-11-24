package com.ustadmobile.port.android.view.composable

import java.util.Calendar
import android.view.LayoutInflater
import android.widget.DatePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import com.toughra.ustadmobile.R

/**
 * Date picker spinner (wraps AndroidView and inflating R.layout.datepicker_spinner)
 */
@Composable
fun DatePickerSpinner(
    date: Long,
    onSetDate: (Long) -> Unit,
) {
    val calendar: Calendar = remember(date) {
        Calendar.getInstance().apply {
            timeInMillis = date
        }
    }

    AndroidView(factory = {
        LayoutInflater.from(it).inflate(R.layout.datepicker_spinner, null, false).apply {
            this as DatePicker
            init(calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH],
                    calendar[Calendar.DAY_OF_MONTH],
                    DatePicker.OnDateChangedListener { _, _, _, _ ->

                    })
        }
    })
}