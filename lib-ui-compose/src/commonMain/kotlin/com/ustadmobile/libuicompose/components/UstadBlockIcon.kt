package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.avatarColorForName
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.libuicompose.util.ext.rgbaColor
import com.ustadmobile.libuicompose.view.clazz.blockTypeImageVector
import com.ustadmobile.libuicompose.view.contententry.contentTypeImageVector

@Composable
fun UstadBlockIcon(
    title: String,
    courseBlock: CourseBlock? = null,
    contentEntry: ContentEntry? = null,
    pictureUri: String? = null,
    modifier: Modifier = Modifier,
) {
    val baseModifier = modifier.width(40.dp).height(40.dp)
    if(pictureUri == null) {
        val backgroundColor = avatarColorForName(title).rgbaColor()

        Box(
            modifier = if(title.isNotEmpty()){
                baseModifier.background(backgroundColor)
            } else {
                modifier
            },
            contentAlignment = Alignment.Center,
        ) {
            val icon = contentEntry?.contentTypeImageVector
                ?: courseBlock?.blockTypeImageVector

            if(icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
    }else {
        Box(
            modifier = baseModifier,
            contentAlignment = Alignment.Center,
        ) {
            UstadAsyncImage(
                uri = pictureUri,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }

    }

}