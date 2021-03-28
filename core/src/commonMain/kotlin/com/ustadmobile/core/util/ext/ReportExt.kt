package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.DateRangeMoment
import com.ustadmobile.lib.db.entities.Moment
import com.ustadmobile.lib.db.entities.Report

fun Report.toDateRangeMoment(): DateRangeMoment{
    return DateRangeMoment(
            Moment().apply {
                typeFlag = if(fromDate == 0L) Moment.TYPE_FLAG_RELATIVE else Moment.TYPE_FLAG_FIXED
                fixedTime = fromDate
                relTo = fromRelTo
                relUnit = fromRelUnit
                relOffSet = fromRelOffSet

    }, Moment().apply {
            typeFlag = if(toDate == 0L) Moment.TYPE_FLAG_RELATIVE else Moment.TYPE_FLAG_FIXED
            fixedTime = toDate
            relTo = toRelTo
            relUnit = toRelUnit
            relOffSet = toRelOffSet
    })
}