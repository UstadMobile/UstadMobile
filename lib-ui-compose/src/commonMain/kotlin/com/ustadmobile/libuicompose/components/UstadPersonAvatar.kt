package com.ustadmobile.libuicompose.components

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * This is a placeholder that will be filled in when attachments are re-enabled.
 */
@Composable
fun UstadPersonAvatar(
    @Suppress("UNUSED_PARAMETER") personUid: Long,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier,
        imageVector = Icons.Filled.AccountCircle,
        contentDescription = null
    )
}
