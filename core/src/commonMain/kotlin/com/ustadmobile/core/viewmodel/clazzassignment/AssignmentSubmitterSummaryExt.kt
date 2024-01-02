package com.ustadmobile.core.viewmodel.clazzassignment

import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission

/**
 * When this is used as an avatar, for individual assignments, the name to generate the color is
 * simply the submitter name.
 *
 * If it is a group, then use the group number (submitter uid)
 */
fun AssignmentSubmitterSummary.avatarName(): String? {
    return if(submitterUid >= CourseAssignmentSubmission.MIN_SUBMITTER_UID_FOR_PERSON) {
        name
    }else {
        submitterUid.toString()
    }
}

fun AssignmentSubmitterSummary.avatarColorName(): String? {
    return if(submitterUid >= CourseAssignmentSubmission.MIN_SUBMITTER_UID_FOR_PERSON) {
        name
    }else {
        //Submitter uid for group numbers will be low, so shift bits to left to get more distinct colors
        (submitterUid shl 16).toString()
    }
}