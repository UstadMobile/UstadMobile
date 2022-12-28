package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.ReportFilter

@ShallowCopy
expect fun ReportFilter.shallowCopy(
    block: ReportFilter.() -> Unit,
): ReportFilter