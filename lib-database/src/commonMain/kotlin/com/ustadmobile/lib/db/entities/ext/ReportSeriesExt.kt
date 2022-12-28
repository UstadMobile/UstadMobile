package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.ReportSeries

@ShallowCopy
expect fun ReportSeries.shallowCopy(
    block: ReportSeries.() -> Unit,
): ReportSeries