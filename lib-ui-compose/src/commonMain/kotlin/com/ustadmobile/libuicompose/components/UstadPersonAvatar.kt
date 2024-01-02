package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.avatarColorForName
import com.ustadmobile.core.util.ext.initial
import com.ustadmobile.libuicompose.util.ext.rgbaColor

/**
 * This is a placeholder that will be filled in when attachments are re-enabled.
 */
@Composable
fun UstadPersonAvatar(
    @Suppress("UNUSED_PARAMETER") personUid: Long = 0,
    pictureUri: String? = null,
    personName: String? = null,
    modifier: Modifier = Modifier.size(40.dp),
) {
    if(pictureUri == null) {
       if(personName != null) {
           Box(
               modifier = modifier,
               contentAlignment = Alignment.Center
           ) {
               val bgColor = avatarColorForName(personName).rgbaColor()
               Canvas(modifier = Modifier.fillMaxSize()) {
                   drawCircle(SolidColor(bgColor))
               }

               Text(
                   color = androidx.compose.ui.graphics.Color.White,
                   style = MaterialTheme.typography.titleMedium,
                   text=  personName.initial(),
               )
           }
       }
    }else {
        UstadAsyncImage(
            uri = pictureUri,
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape)
        )
    }
}
