package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission

@ShallowCopy
expect fun CourseAssignmentSubmission.shallowCopy(
    block: CourseAssignmentSubmission.() -> Unit
): CourseAssignmentSubmission
