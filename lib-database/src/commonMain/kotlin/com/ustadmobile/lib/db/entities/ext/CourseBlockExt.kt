package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.CourseBlock

@ShallowCopy
expect fun CourseBlock.shallowCopy(
    block: CourseBlock.() -> Unit,
): CourseBlock