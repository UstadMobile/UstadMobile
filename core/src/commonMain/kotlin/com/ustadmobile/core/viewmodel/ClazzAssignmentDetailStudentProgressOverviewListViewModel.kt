package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary

data class ClazzAssignmentDetailStudentProgressListOverviewUiState(

    val progressSummary: AssignmentProgressSummary? = null,

    val assignmentSubmitterList: List<AssignmentSubmitterSummary> = emptyList()

)