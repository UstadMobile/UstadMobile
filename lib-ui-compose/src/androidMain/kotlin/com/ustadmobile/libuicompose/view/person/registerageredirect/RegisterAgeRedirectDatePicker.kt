package com.ustadmobile.libuicompose.view.person.registerageredirect

import androidx.compose.runtime.Composable
import com.ustadmobile.libuicompose.components.DatePickerSpinner

@Composable
actual fun RegisterAgeRedirectDatePicker(
    date: Long,
    onSetDate: (Long) -> Unit
) {
    DatePickerSpinner(
        date = date,
        onSetDate = onSetDate
    )
}
