package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary

interface SubmissionSummaryListener {

    fun onClickPerson(personWithAttemptsSummary: AssignmentSubmitterSummary)

}