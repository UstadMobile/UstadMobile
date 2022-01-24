package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.ContentEntryWithAttemptsSummary


interface ClazzAssignmentDetailStudentProgressItemListener {

    fun onClickClazzAssignment(clazzAssignment: ContentEntryWithAttemptsSummary)

}