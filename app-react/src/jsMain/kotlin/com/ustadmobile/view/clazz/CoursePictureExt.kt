package com.ustadmobile.view.clazz

import com.ustadmobile.core.viewmodel.clazz.defaultCourseBannerImageIndex
import com.ustadmobile.lib.db.entities.CoursePicture

fun CoursePicture?.uriOrDefaultBanner(clazzName: String) : String {
    return this?.coursePictureUri
        ?: "img/default_course_banners/${defaultCourseBannerImageIndex(clazzName)}.webp"
}
