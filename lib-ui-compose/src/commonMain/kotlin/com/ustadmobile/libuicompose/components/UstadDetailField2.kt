package com.ustadmobile.libuicompose.components

import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Currently this is just a wrapper for ListItem, but that could change if needed.
 */
@Composable
fun UstadDetailField2(
    valueContent: @Composable () -> Unit,
    labelContent: (@Composable () -> Unit)? = null,
    leadingContent: @Composable (()-> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    //if required - this can be converted to use our own colors /style via
    //ProvideTextStyle and CompositionLocalProvider(LocalContentColor provides color)
    ListItem(
        modifier = modifier,
        headlineContent = valueContent,
        supportingContent = labelContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
    )
}

@Composable
fun UstadDetailField2(
    valueText: String,
    labelText: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    UstadDetailField2(
        modifier = modifier,
        valueContent = { Text(valueText) },
        labelContent = { Text(labelText) },
        leadingContent = if(icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
            }
        }else {
            null
        }
    )
}