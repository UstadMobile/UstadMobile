package com.ustadmobile.core.viewmodel

package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.lib.db.entities.*

data class ClazzAssignmentDetailStudentProgressUiState(

    val fieldsEnabled: Boolean = true,

    val submitMarkError: String? = null,

    val submitterName: String = "",

    val gradeFilterChips: List<ListFilterIdOption> = emptyList(),

    val clazzCourseAssignmentSubmissionAttachment: List<CourseAssignmentSubmissionWithAttachment> =
        emptyList(),

    val markList: List<CourseAssignmentMarkWithPersonMarker> = emptyList(),

    val clazzAssignmentPrivateComments: List<CommentsWithPerson> = emptyList(),

    val submissionScore: AverageCourseAssignmentMark? = null,

    val submissionStatus: Int? = null,

    val markNextStudentVisible: Boolean =  true,

    val markStudentVisible: Boolean = true

)