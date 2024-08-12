package com.ustadmobile.lib.db.composites

import kotlinx.serialization.Serializable

@Serializable
data class CourseBlockUidAndClazzUid(
    var clazzUid: Long = 0,
    var courseBlockUid: Long = 0,
)
