package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.ClazzAssignment

@ShallowCopy
expect fun ClazzAssignment.shallowCopy(
    block: ClazzAssignment.() -> Unit,
): ClazzAssignment