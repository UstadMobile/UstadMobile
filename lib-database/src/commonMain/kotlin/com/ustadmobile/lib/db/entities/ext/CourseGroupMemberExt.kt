package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.CourseGroupMember

@ShallowCopy
expect fun CourseGroupMember.shallowCopy(
    block: CourseGroupMember.() -> Unit,
): CourseGroupMember