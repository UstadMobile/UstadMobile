package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.CourseGroupSet

@ShallowCopy
expect fun CourseGroupSet.shallowCopy(
    block: CourseGroupSet.() -> Unit,
): CourseGroupSet