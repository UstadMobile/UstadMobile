package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason

@ShallowCopy
expect fun ClazzEnrolmentWithLeavingReason.shallowCopy(
    block: ClazzEnrolmentWithLeavingReason.() -> Unit,
): ClazzEnrolmentWithLeavingReason