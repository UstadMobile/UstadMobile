package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.CourseAssignmentMark

fun CourseAssignmentMark.penaltyPercentage(): Float {
    return camMark / (camMark + camPenalty)
}
