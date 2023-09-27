package com.ustadmobile.libuicompose.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.timepicker.MaterialTimePicker
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN

@Composable
actual fun TimePickerAdornment(onTimeSelected: (Int) -> Unit) {

    val context = LocalContext.current

//    val supportFragmentManager = context.getContextSupportFragmentManager()
//    MaterialTimePicker.Builder()
//        .build()
//        .apply {
//            addOnPositiveButtonClickListener {
//                onTimeSelected(((hour * MS_PER_HOUR) + (minute * MS_PER_MIN)))
//            }
//        }
//        .show(supportFragmentManager, "timePickerTag")

}