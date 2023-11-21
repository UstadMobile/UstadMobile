package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun UstadAddListItem(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector = Icons.Filled.Add,
    onClickAdd: (() -> Unit) = {  }
){
    ListItem(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = { onClickAdd() }
            ),
        leadingContent = {
            Icon(
                icon,
                contentDescription = null
            )
        },
        headlineContent = {
            Text(text)
        }
    )
}
