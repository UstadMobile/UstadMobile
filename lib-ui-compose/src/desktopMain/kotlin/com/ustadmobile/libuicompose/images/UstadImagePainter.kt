package com.ustadmobile.libuicompose.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

val nameMap = mapOf(
    UstadImage.ILLUSTRATION_CONNECT to "/img/illustration_connect.png",
    UstadImage.ILLUSTRATION_ONBOARDING1 to "/img/illustration_onboarding1.png",
    UstadImage.ILLUSTRATION_ONBOARDING2 to "/img/illustration_onboarding2.png",
    UstadImage.ILLUSTRATION_ONBOARDING3 to "/img/illustration_onboarding3.png",
    UstadImage.COURSE_BANNER_DEFAULT0 to "/img/course_banner_default0.webp",
    UstadImage.COURSE_BANNER_DEFAULT1 to "/img/course_banner_default1.webp",
    UstadImage.COURSE_BANNER_DEFAULT2 to "/img/course_banner_default2.webp",
    UstadImage.COURSE_BANNER_DEFAULT3 to "/img/course_banner_default3.webp",
    UstadImage.COURSE_BANNER_DEFAULT4 to "/img/course_banner_default4.webp",

)
@Composable
actual fun ustadAppImagePainter(image: UstadImage): Painter {
    return painterResource(nameMap[image]  ?: throw IllegalArgumentException("no image for $image"))
}
