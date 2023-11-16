package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Preview
@Composable
fun UstadDetailField2Preview() {
//    ProvideTextStyleFromToken()
//    androidx.compose.material3.ListItem()
    UstadDetailField2(
        valueContent = {
            Text("12345")
        },
        labelContent = {
            Text("Number")
        }
    )
}