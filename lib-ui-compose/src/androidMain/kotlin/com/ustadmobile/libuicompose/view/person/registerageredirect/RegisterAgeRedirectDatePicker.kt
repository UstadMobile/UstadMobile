package com.ustadmobile.libuicompose.view.person.registerageredirect

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libuicompose.components.DatePickerSpinner

@Composable
actual fun RegisterAgeRedirectDatePicker(
    date: Long,
    onSetDate: (Long) -> Unit,
    supportingText: @Composable () -> Unit,
    isError: Boolean,
    maxDate: Long,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DatePickerSpinner(
            date = if(date == 0L) {
                systemTimeInMillis()
            }else {
                date
            },
            onSetDate = onSetDate,
            maxDate = maxDate,
        )

        ProvideTextStyle(MaterialTheme.typography.labelSmall) {
            val color = if(isError) {
                MaterialTheme.colorScheme.error
            }else {
                //OnSurfaceVariant is the color used as per OutlinedTextFieldTokens.kt for supportingtext
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            CompositionLocalProvider(LocalContentColor provides color) {
                supportingText()
            }
        }
    }
}
