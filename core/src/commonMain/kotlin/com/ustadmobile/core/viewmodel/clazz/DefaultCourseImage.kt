package com.ustadmobile.core.viewmodel.clazz

import com.ustadmobile.core.util.avatarColorForName

private const val NUM_COURSE_IMAGES = 5

/**
 * Select a stock banner image for a course based on the name
 */
fun defaultCourseBannerImageIndex(courseName: String?): Int {
    return avatarColorForName(courseName ?: "").mod(NUM_COURSE_IMAGES)
}
