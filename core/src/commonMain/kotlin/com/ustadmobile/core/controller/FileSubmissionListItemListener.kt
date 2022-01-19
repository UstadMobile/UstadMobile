package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.AssignmentFileSubmission

interface FileSubmissionListItemListener {

    fun onClickDeleteFileSubmission(fileSubmission: AssignmentFileSubmission)

    fun onClickDownloadFileSubmission(fileSubmission: AssignmentFileSubmission)

}