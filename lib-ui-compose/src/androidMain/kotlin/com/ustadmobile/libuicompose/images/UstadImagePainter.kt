package com.ustadmobile.libuicompose.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.ustadmobile.libuicompose.R

private val nameMap = mapOf(
    UstadImage.ILLUSTRATION_CONNECT to R.drawable.illustration_connect,
    UstadImage.ILLUSTRATION_OFFLINE_USAGE to R.drawable.illustration_offline_usage,
    UstadImage.ILLUSTRATION_OFFLINE_SHARING to R.drawable.illustration_offline_sharing,
    UstadImage.ILLUSTRATION_ORGANIZED to R.drawable.illustration_organized,
    UstadImage.COURSE_BANNER_DEFAULT0 to R.drawable.course_banner_default0,
)

@Composable
actual fun ustadAppImagePainter(image: UstadImage): Painter {
    return painterResource(nameMap[image]  ?: throw IllegalArgumentException("no image for $image"))
}
