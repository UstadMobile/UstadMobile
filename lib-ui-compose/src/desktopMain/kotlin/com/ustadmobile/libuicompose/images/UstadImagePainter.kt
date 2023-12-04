package com.ustadmobile.libuicompose.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

val nameMap = mapOf(
    UstadImage.ILLUSTRATION_CONNECT to "/img/illustration_connect.png",
    UstadImage.ILLUSTRATION_OFFLINE_SHARING to "/img/illustration_offline_sharing.png",
    UstadImage.ILLUSTRATION_OFFLINE_USAGE to "/img/illustration_offline_usage.png",
    UstadImage.ILLUSTRATION_ORGANIZED to "/img/illustration_organized.png",

)
@Composable
actual fun ustadAppImagePainter(image: UstadImage): Painter {
    return painterResource(nameMap[image]  ?: throw IllegalArgumentException("no image for $image"))
}
