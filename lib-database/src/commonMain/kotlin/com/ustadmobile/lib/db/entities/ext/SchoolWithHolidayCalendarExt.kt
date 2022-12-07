package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar

@ShallowCopy
expect fun SchoolWithHolidayCalendar.shallowCopy(
    block: SchoolWithHolidayCalendar.() -> Unit,
): SchoolWithHolidayCalendar