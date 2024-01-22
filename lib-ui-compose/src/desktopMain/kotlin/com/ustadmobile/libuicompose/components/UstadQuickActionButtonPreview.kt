package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.runtime.Composable

@Composable
@Preview
private fun UstadQuickActionButtonPreview() {
    UstadQuickActionButton(
        imageVector = Icons.Default.CalendarToday,
        labelText = "Birthday"
    )
}
