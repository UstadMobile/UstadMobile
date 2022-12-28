package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity

@ShallowCopy
expect fun CourseBlockWithEntity.shallowCopyCourseBlockWithEntity(
    block: CourseBlockWithEntity.() -> Unit,
): CourseBlockWithEntity