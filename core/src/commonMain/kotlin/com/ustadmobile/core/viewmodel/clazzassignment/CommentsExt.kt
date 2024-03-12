package com.ustadmobile.core.viewmodel.clazzassignment

import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission

val Comments.isFromSubmitterGroup: Boolean
    get() = commentsFromSubmitterUid > 0 && commentsFromSubmitterUid < CourseAssignmentSubmission.MIN_SUBMITTER_UID_FOR_PERSON
