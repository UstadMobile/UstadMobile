package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.LeavingReason

@ShallowCopy
expect fun LeavingReason.shallowCopy(
    block: LeavingReason.() -> Unit,
): LeavingReason