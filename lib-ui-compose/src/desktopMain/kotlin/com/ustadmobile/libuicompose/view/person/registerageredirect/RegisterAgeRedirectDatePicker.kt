package com.ustadmobile.libuicompose.view.person.registerageredirect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.libuicompose.components.UstadDateField

@Composable
actual fun RegisterAgeRedirectDatePicker(
    date: Long,
    onSetDate: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        UstadDateField(
            value = date,
            label = {

            },
            timeZoneId = "UTC",
            onValueChange = onSetDate,
        )
    }

}