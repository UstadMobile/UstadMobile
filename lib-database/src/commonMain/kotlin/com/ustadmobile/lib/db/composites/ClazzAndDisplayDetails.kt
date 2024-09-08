package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CoursePicture
import com.ustadmobile.lib.db.entities.CourseTerminology

data class ClazzAndDisplayDetails(
    @Embedded
    var clazz: Clazz? = null,

    @Embedded
    var terminology: CourseTerminology? = null,

    @Embedded
    var coursePicture: CoursePicture? = null,

    var numStudents: Int = 0,

    var numTeachers: Int = 0,

    var activeUserIsStudent: Boolean = false,
)