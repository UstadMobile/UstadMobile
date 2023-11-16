package com.ustadmobile.libuicompose.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.timepicker.MaterialTimePicker
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager

@Composable
actual fun UstadTimePickerAdornment(onTimeSelected: (Int) -> Unit) {

    val context = LocalContext.current

    IconButton(
        onClick = {
            val supportFragmentManager = context.getContextSupportFragmentManager()
            MaterialTimePicker.Builder()
                .build()
                .apply {
                    addOnPositiveButtonClickListener {
                        onTimeSelected(((hour * MS_PER_HOUR) + (minute * MS_PER_MIN)))
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

}