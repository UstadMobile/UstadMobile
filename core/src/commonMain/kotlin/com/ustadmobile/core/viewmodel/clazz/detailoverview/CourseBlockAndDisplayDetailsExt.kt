package com.ustadmobile.core.viewmodel.clazz.detailoverview

import com.ustadmobile.core.util.ext.roundTo
import com.ustadmobile.core.util.ext.toDisplayString
import com.ustadmobile.lib.db.composites.CourseBlockAndDisplayDetails

fun CourseBlockAndDisplayDetails.getScoreInPointsStr(
    decimalPlaces: Int = 2
) : String? {

    val scoreScaled = this.status?.sScoreScaled
    val courseBlockMaxPoints = this.courseBlock?.cbMaxPoints
    return if(scoreScaled != null && courseBlockMaxPoints != null) {
        (scoreScaled * courseBlockMaxPoints).roundTo(decimalPlaces).toDisplayString()
    }else {
        null
    }
}