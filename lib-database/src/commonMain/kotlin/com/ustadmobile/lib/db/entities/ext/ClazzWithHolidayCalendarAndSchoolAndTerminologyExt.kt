package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndAndTerminology


@ShallowCopy
expect fun ClazzWithHolidayCalendarAndAndTerminology.shallowCopy(
    block: ClazzWithHolidayCalendarAndAndTerminology.() -> Unit,
): ClazzWithHolidayCalendarAndAndTerminology