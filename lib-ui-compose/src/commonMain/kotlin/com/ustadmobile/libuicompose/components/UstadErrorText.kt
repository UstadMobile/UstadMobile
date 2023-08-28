package com.ustadmobile.libuicompose.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UstadErrorText(
    modifier: Modifier = Modifier,
    error: String,
){
    Text(text = error,
        style = MaterialTheme.typography.caption,
        color = MaterialTheme.colors.error,
        modifier = modifier,
    )
}

@Composable
private fun UstadErrorTextPreview() {
    UstadErrorText(
        error = "Email is not valid"
    )
}