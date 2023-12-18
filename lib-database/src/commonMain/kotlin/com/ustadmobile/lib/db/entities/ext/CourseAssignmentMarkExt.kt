package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.CourseAssignmentMark

@ShallowCopy
expect fun CourseAssignmentMark.shallowCopy(
    block: CourseAssignmentMark.() -> Unit,
): CourseAssignmentMark
