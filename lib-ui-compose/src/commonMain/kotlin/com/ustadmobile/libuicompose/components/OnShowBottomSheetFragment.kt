package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable


/**
 * The Jetpack Compose BottomSheet has a bug that places it incorrectly when the softkeyboard is
 * displayed (e.g. where there is text input).
 *
 * This is an expect/actual workaround that will use a Fragment on Android. On JVM/desktop it does
 * nothing
 *
 * https://issuetracker.google.com/issues/308308431
 */
@Composable
expect fun onShowBottomSheetFragmentFunction(
    content: @Composable (onDismiss: () -> Unit) -> Unit,
): () -> Unit
