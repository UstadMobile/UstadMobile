package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.ClazzLog

@ShallowCopy
expect fun ClazzLog.shallowCopy(
    block: ClazzLog.() -> Unit
): ClazzLog
