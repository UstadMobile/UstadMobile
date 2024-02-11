package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UstadSelectableListItem(
    headlineContent: @Composable () -> Unit,
    isSelected: Boolean,
    onSetSelected: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    selectedColors: ListItemColors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
) {
    val localWinState = LocalWindowInfo.current

    ListItem(
        headlineContent = headlineContent,
        modifier = modifier.combinedClickable(
            enabled = true,
            onClick = {
                if(localWinState.keyboardModifiers.isShiftPressed || localWinState.keyboardModifiers.isCtrlPressed) {
                    onSetSelected(!isSelected)
                }else {
                    onClick()
                }
            },
            onLongClick = {
                onSetSelected(!isSelected)
            }
        ).semantics {
            selected = isSelected
            selectableGroup()
        },
        colors = if(isSelected) {
            selectedColors
        }else {
            colors
        },
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    )
}