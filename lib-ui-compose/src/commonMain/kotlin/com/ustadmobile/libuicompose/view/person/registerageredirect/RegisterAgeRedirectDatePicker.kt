package com.ustadmobile.libuicompose.view.person.registerageredirect

import androidx.compose.runtime.Composable

@Composable
expect fun RegisterAgeRedirectDatePicker(
    date: Long,
    onSetDate: (Long) -> Unit,
    supportingText: @Composable () -> Unit,
    isError: Boolean,
    maxDate: Long,
    onDone: () -> Unit,
)
