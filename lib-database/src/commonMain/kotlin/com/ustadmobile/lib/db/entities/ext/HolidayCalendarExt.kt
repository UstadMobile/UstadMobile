package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.HolidayCalendar

@ShallowCopy
expect fun HolidayCalendar.shallowCopy(
    block: HolidayCalendar.() -> Unit,
): HolidayCalendar
