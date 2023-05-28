package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import kotlin.math.roundToInt

/**
 * The penalty as a percentage of the mark (as an Int out of 100)
 */
fun CourseAssignmentMark.penaltyPercentage(): Int {
    return ((camPenalty * 100) / (camMark + camPenalty)).roundToInt()
}

