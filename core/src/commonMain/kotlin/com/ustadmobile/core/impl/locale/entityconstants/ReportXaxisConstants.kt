package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.util.MessageIdOption2

object ReportXAxisConstants {
    val X_AXIS_OPTIONS = listOf(
        MessageIdOption2(MR.strings.day, ReportXAxis.DAY.value),
        MessageIdOption2(MR.strings.weekly, ReportXAxis.WEEK.value),
        MessageIdOption2(MR.strings.classes, ReportXAxis.CLASS.value),
        MessageIdOption2(MR.strings.gender_literal, ReportXAxis.GENDER.value),
        MessageIdOption2(MR.strings.monthly, ReportXAxis.MONTH.value),
    )
}
