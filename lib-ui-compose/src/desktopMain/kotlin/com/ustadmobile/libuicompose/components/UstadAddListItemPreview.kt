package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable

@Composable
@Preview
private fun UstadAddListItemPreview() {
    UstadAddListItem(
        text = "Add",
        enabled = true,
        icon = Icons.Default.Add,
        onClickAdd = {}
    )
}