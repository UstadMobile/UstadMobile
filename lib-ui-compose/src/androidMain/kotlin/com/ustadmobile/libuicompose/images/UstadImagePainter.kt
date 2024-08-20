package com.ustadmobile.libuicompose.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.ustadmobile.libuicompose.R

private val nameMap = mapOf(
    UstadImage.INDIVIDUAL_NEW_ACCOUNT to R.drawable.individual_create,
    UstadImage.INDIVIDUAL_RESTORE_ACCOUNT to R.drawable.individual_restore,
    UstadImage.ONBOARDING_INDIVIDUAL to R.drawable.onboarding_individual,
    UstadImage.ONBOARDING_EXISTING to R.drawable.onboarding_existing,
    UstadImage.ONBOARDING_ADD_ORG to R.drawable.onboarding_add_org,
    UstadImage.ILLUSTRATION_CONNECT to R.drawable.illustration_connect,
    UstadImage.ILLUSTRATION_ONBOARDING1 to R.drawable.illustration_onboarding1,
    UstadImage.ILLUSTRATION_ONBOARDING2 to R.drawable.illustration_onboarding2,
    UstadImage.ILLUSTRATION_ONBOARDING3 to R.drawable.illustration_onboarding3,
    UstadImage.COURSE_BANNER_DEFAULT0 to R.drawable.course_banner_default0,
    UstadImage.COURSE_BANNER_DEFAULT1 to R.drawable.course_banner_default1,
    UstadImage.COURSE_BANNER_DEFAULT2 to R.drawable.course_banner_default2,
    UstadImage.COURSE_BANNER_DEFAULT3 to R.drawable.course_banner_default3,
    UstadImage.COURSE_BANNER_DEFAULT4 to R.drawable.course_banner_default4,
    UstadImage.APP_LOGO to com.ustadmobile.core.R.drawable.ustad_logo,
)

@Composable
actual fun ustadAppImagePainter(image: UstadImage): Painter {
    return painterResource(nameMap[image]  ?: throw IllegalArgumentException("no image for $image"))
}
