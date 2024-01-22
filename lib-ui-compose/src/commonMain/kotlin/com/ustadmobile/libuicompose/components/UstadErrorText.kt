package com.ustadmobile.libuicompose.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UstadErrorText(
    modifier: Modifier = Modifier,
    error: String,
){
    Text(text = error,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier,
    )
}

