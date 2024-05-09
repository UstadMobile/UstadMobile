package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.StudentResult

data class StudentResultAndCourseBlockSourcedId(
    @Embedded
    var studentResult: StudentResult = StudentResult(),

    var cbSourcedId: String? = null,
)