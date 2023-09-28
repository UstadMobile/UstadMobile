package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable

@Composable
expect fun UstadTimePickerAdornment(

    onTimeSelected: (Int) -> Unit

)