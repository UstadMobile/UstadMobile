package com.ustadmobile.lib.db.composites

import kotlinx.serialization.Serializable

@Serializable
data class CourseNameAndPersonName(
    var clazzName: String? = null,
    var firstNames: String? = null,
    var lastName: String? = null,
)
