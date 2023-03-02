package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.AssignmentSubmitterWithAllocations

@ShallowCopy
expect fun AssignmentSubmitterWithAllocations.shallowCopy(
    block: AssignmentSubmitterWithAllocations.() -> Unit,
): AssignmentSubmitterWithAllocations