package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
@Preview
private fun UstadDetailHeaderPreview() {
    UstadDetailHeader(
        headerContent = { Text("UstadDetailHeader Preview") }
    )
}