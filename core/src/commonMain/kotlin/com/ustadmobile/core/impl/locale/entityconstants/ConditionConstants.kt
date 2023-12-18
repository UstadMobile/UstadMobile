package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ReportFilter

object ConditionConstants {

    val CONDITION_MESSAGE_IDS = listOf(
        MessageIdOption2(MR.strings.condition_is, ReportFilter.CONDITION_IS),
        MessageIdOption2(MR.strings.condition_is_not, ReportFilter.CONDITION_IS_NOT),
        MessageIdOption2(MR.strings.condition_greater_than, ReportFilter.CONDITION_GREATER_THAN),
        MessageIdOption2(MR.strings.condition_less_than, ReportFilter.CONDITION_LESS_THAN),
        MessageIdOption2(MR.strings.condition_between, ReportFilter.CONDITION_BETWEEN),
        MessageIdOption2(MR.strings.condition_in_list, ReportFilter.CONDITION_IN_LIST),
        MessageIdOption2(MR.strings.condition_not_in_list, ReportFilter.CONDITION_NOT_IN_LIST)
    )
}