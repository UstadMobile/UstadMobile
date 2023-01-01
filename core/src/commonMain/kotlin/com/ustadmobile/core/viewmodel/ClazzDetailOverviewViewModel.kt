package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.CourseBlockWithCompleteEntity
import com.ustadmobile.lib.db.entities.Schedule

data class ClazzDetailOverviewUiState(

    val showPermissionButton: Boolean = false,

    val fieldsEnabled: Boolean = true,

    val courseBlock: CourseBlockWithCompleteEntity? = null,

    val clazz: ClazzWithDisplayDetails? = null,

    val scheduleList: List<Schedule> = emptyList()

) {
    val clazzSchoolUidVisible: Boolean
        get() = clazz?.clazzSchoolUid  != null
                && clazz.clazzSchoolUid != 0L

    val clazzDateVisible: Boolean
        get() = clazz?.clazzStartTime.isDateSet()
                || clazz?.clazzEndTime.isDateSet()

    val clazzHolidayCalendarVisible: Boolean
        get() = clazz?.clazzHolidayCalendar != null
}