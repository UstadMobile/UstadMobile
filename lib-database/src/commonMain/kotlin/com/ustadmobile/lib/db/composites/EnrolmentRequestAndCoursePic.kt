package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.CoursePicture
import com.ustadmobile.lib.db.entities.EnrolmentRequest
import kotlinx.serialization.Serializable

@Serializable
data class EnrolmentRequestAndCoursePic(
    @Embedded
    var enrolmentRequest: EnrolmentRequest? = null,
    @Embedded
    var coursePicture: CoursePicture? = null
)