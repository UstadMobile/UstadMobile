package com.ustadmobile.libuicompose.components

import androidx.compose.ui.platform.LocalContext
import com.google.android.material.timepicker.MaterialTimePicker
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager

actual fun UstadTimePickerAdornment(onTimeSelected: (Int) -> Unit) {

    val context = LocalContext.current

    val supportFragmentManager = context.getContextSupportFragmentManager()
    MaterialTimePicker.Builder()
        .build()
        .apply {
            addOnPositiveButtonClickListener {
                onTimeSelected(((hour * MS_PER_HOUR) + (minute * MS_PER_MIN)))
            }
        }
        .show(supportFragmentManager, "timePickerTag")

}