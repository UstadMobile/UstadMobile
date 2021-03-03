package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzEnrolmentWithClazzWorkProgress : Person() {

    @Embedded
    var mLatestPrivateComment: Comments? = null

    @Embedded
    var mClazzWorkSubmission: ClazzWorkSubmission? = null

    //Progress
    var mProgress: Float = 0.0F

    var clazzWorkHasContent: Boolean = false

    var isActiveEnrolment: Boolean = false

}
