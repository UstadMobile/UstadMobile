package com.ustadmobile.libuicompose.view.clazz

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.ustadmobile.core.viewmodel.clazz.defaultCourseBannerImageIndex
import com.ustadmobile.libuicompose.images.UstadImage
import com.ustadmobile.libuicompose.images.ustadAppImagePainter

private val courseImages = listOf(
    UstadImage.ONBOARDING_INDIVIDUAL,
    UstadImage.ONBOARDING_EXISTING,
    UstadImage.ONBOARDING_ADD_ORG,
    UstadImage.INDIVIDUAL_NEW_ACCOUNT,
    UstadImage.INDIVIDUAL_RESTORE_ACCOUNT,
    UstadImage.COURSE_BANNER_DEFAULT0,
    UstadImage.COURSE_BANNER_DEFAULT1,
    UstadImage.COURSE_BANNER_DEFAULT2,
    UstadImage.COURSE_BANNER_DEFAULT3,
    UstadImage.COURSE_BANNER_DEFAULT4,
)

@Composable
fun painterForDefaultCourseImage(
    courseName: String?
): Painter {
    val index = defaultCourseBannerImageIndex(courseName)
    return ustadAppImagePainter(courseImages[index])
}
