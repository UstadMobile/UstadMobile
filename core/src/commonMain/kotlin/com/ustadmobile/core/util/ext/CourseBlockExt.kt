package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.CourseBlock

/**
 * Determine the latest possible time that this CourseBlock can be submitted: applies the grace
 * period if set, otherwise uses the deadline.
 */
fun CourseBlock.lastPossibleSubmissionTime(): Long {
    if(cbGracePeriodDate.isDateSet() && cbGracePeriodDate > cbDeadlineDate)
        return cbGracePeriodDate
    else
        return cbDeadlineDate
}

