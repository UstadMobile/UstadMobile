package com.ustadmobile.libuicompose.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UstadCaptionText(
    caption: String,
    modifier: Modifier,
) {
    Text(
        text = caption,
        style = MaterialTheme.typography.caption,
        modifier = modifier,
    )
}