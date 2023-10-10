package com.ustadmobile.libuicompose.components


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
expect fun UstadImageSelectButton(
    imageUri: String?,
    onImageUriChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
)