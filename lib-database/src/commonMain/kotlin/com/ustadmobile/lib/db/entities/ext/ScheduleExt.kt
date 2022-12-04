package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.Schedule

@ShallowCopy
expect fun Schedule.shallowCopy(
    block: Schedule.() -> Unit,
): Schedule