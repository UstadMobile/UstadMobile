package com.ustadmobile.libuicompose.components

import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * This provides a standard option for a bottom sheet option. In reality, this just delegates to
 * using ListItem. The BottomSheet Material3 is experimental, and liable to change. This functions'
 * implementation can then be adjusted as needed.
 */
@Composable
fun UstadBottomSheetOption(
    headlineContent: @Composable () -> Unit,
    secondaryContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = headlineContent,
        supportingContent = secondaryContent,
        leadingContent =  leadingContent,
        modifier = modifier,
    )
}