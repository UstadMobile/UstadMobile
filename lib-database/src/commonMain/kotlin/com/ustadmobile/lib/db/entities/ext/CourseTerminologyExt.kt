package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.CourseTerminology

@ShallowCopy
expect fun CourseTerminology.shallowCopy(
    block: CourseTerminology.() -> Unit,
): CourseTerminology
