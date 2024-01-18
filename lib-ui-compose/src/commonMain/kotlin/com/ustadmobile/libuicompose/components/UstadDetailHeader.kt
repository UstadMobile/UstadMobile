package com.ustadmobile.libuicompose.components

import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Standard header for detail screens - For now this is a simple alias
 * of ListItem
 */
@Composable
fun UstadDetailHeader(
    modifier: Modifier = Modifier,
    headerContent: @Composable () -> Unit,
) {
    ListItem(
        headlineContent = headerContent,
        modifier = modifier,
    )
}