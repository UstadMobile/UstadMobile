package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable

@Composable
actual fun onShowBottomSheetFragmentFunction(
    content: @Composable (onDismiss: () -> Unit) -> Unit,
): () -> Unit {
    return { }
}
