package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.Report

object DateRangeConstants {

    val DATE_RANGE_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.time_range_all, Report.EVERYTHING),
        MessageIdOption2(MessageID.last_week_date_range, Report.LAST_WEEK_DATE),
        MessageIdOption2(MessageID.last_two_week_date_range, Report.LAST_TWO_WEEKS_DATE),
        MessageIdOption2(MessageID.last_month_date_range, Report.LAST_MONTH_DATE),
        MessageIdOption2(MessageID.last_three_months_date_range, Report.LAST_THREE_MONTHS_DATE),
        MessageIdOption2(MessageID.selected_custom_range, Report.CUSTOM_RANGE),
        MessageIdOption2(MessageID.new_custom_date_range, Report.NEW_CUSTOM_RANGE_DATE),
    )
}