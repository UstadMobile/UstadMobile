package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * This is a placeholder that will be filled in when attachments are re-enabled.
 */
@Composable
fun UstadPersonAvatar(
    @Suppress("UNUSED_PARAMETER") personUid: Long,
    pictureUri: String? = null,
    modifier: Modifier = Modifier.size(40.dp),
) {
    if(pictureUri == null) {
        Icon(
            modifier = modifier,
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = null
        )
    }else {
        UstadAsyncImage(
            uri = pictureUri,
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape)
        )
    }
}
