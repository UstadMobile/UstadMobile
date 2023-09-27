package com.ustadmobile.libuicompose.util.compose

import androidx.compose.runtime.Composable

@Composable
expect fun TimePickerAdornment(

    onTimeSelected: (Int) -> Unit

)