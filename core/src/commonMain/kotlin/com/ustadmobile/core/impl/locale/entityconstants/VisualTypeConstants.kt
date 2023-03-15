package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ReportSeries

object VisualTypeConstants {

    val VISUAL_TYPE_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.bar_chart, ReportSeries.BAR_CHART),
        MessageIdOption2(MessageID.line_chart, ReportSeries.LINE_GRAPH)
    )
}