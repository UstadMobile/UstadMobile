package com.ustadmobile.libuicompose.view.person.registerageredirect

import androidx.compose.runtime.Composable

@Composable
expect fun RegisterAgeRedirectDatePicker(
    date: Long,
    onSetDate: (Long) -> Unit
)
