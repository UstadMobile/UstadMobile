package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
expect fun AztecEditor(
    html: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
)