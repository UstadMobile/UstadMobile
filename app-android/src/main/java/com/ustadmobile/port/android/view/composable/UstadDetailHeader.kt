package com.ustadmobile.port.android.view.composable

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Standard header for detail screens - For now this is a simple alias
 * of ListItem
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadDetailHeader(
    modifier: Modifier = Modifier,
    headerContent: @Composable () -> Unit,
) {
    ListItem(
        text = headerContent,
        modifier = modifier,
    )
}