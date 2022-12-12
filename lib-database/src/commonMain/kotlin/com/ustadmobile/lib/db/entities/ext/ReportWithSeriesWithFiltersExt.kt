package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters

@ShallowCopy
expect fun ReportWithSeriesWithFilters.shallowCopyReportWithSeriesWithFilters(
    block: ReportWithSeriesWithFilters.() -> Unit,
): ReportWithSeriesWithFilters