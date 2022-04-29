package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.PersonGroupAssignmentSummary

interface SubmissionSummaryListener {

    fun onClickPerson(personWithAttemptsSummary: PersonGroupAssignmentSummary)

}