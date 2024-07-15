package com.ustadmobile.core.viewmodel.clazz.gradebook

import com.ustadmobile.core.util.ext.toDisplayString
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.lib.db.entities.CourseBlock

fun BlockStatus.displayMarkFor(block: CourseBlock?): String? {
    val scaledMarkVal = this.sScoreScaled
    val maxPointsVal = block?.cbMaxPoints
    return if(scaledMarkVal != null && maxPointsVal != null) {
        (scaledMarkVal * maxPointsVal).toDisplayString()
    }else {
        null
    }
}
