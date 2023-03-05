package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon
import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.*

data class ClazzAssignmentDetailStudentProgressUiState(

    val submitMarkError: String? = null,

    val submitterName: String = "",

    val gradeFilterChips: List<ListFilterIdOption> = emptyList(),

    val submissionList: List<CourseAssignmentSubmissionWithAttachment> =
        emptyList(),

    val markList: List<CourseAssignmentMarkWithPersonMarker> = emptyList(),

    val clazzAssignmentPrivateComments: List<CommentsWithPerson> = emptyList(),

    val submissionScore: AverageCourseAssignmentMark? = null,

    val submissionStatus: Int? = null,

    val markNextStudentVisible: Boolean =  true,

    val markStudentVisible: Boolean = true,

    val assignment: ClazzAssignmentWithCourseBlock? = null,

    val fieldsEnabled: Boolean = true,

    val submissionHeaderUiState: UstadAssignmentSubmissionHeaderUiState =
        UstadAssignmentSubmissionHeaderUiState(),

    val selectedChipId: Int = ClazzEnrolmentDaoCommon.FILTER_ACTIVE_ONLY,

    val gradeFilterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MessageID.most_recent, CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES),
        MessageIdOption2(MessageID.all, CourseAssignmentMarkDaoCommon.ARG_FILTER_ALL_SCORES)
    ),

    val markListItemUiState:UstadCourseAssignmentMarkListItemUiState = UstadCourseAssignmentMarkListItemUiState()

)