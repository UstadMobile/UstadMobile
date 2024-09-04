package com.ustadmobile.core.viewmodel.clazz.gradebook

import com.ustadmobile.core.util.ext.toDisplayString
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.lib.db.entities.CourseBlock

//TODO HERE - Combine List of BlockStatus together - if module then aggregate o
// e.g. aggregateIfModule

fun BlockStatus.markFor(block: CourseBlock?): Float? {
    val scaledMarkVal = this.sScoreScaled
    val maxPointsVal = block?.cbMaxPoints
    return if(scaledMarkVal != null && maxPointsVal != null) {
        (scaledMarkVal * maxPointsVal)
    }else {
        null
    }
}

fun BlockStatus.displayMarkFor(
    block: CourseBlock?,
): String? {
    return markFor(block)?.toDisplayString()
}
