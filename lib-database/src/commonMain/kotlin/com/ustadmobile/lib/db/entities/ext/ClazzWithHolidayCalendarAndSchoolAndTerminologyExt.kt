package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchoolAndTerminology


@ShallowCopy
expect fun ClazzWithHolidayCalendarAndSchoolAndTerminology.shallowCopy(
    block: ClazzWithHolidayCalendarAndSchoolAndTerminology.() -> Unit,
): ClazzWithHolidayCalendarAndSchoolAndTerminology