package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.CourseBlockWithCompleteEntity
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.Schedule
import kotlin.math.cbrt

data class ClazzDetailOverviewUiState(

    val clazz: ClazzWithDisplayDetails? = null,

    val scheduleList: List<Schedule> = emptyList(),

    val courseBlockList: List<CourseBlockWithCompleteEntity> = emptyList(),

    val clazzCodeVisible: Boolean = false,

) {
    val clazzSchoolUidVisible: Boolean
        get() = clazz?.clazzSchoolUid != null
                && clazz.clazzSchoolUid != 0L

    val clazzDateVisible: Boolean
        get() = clazz?.clazzStartTime.isDateSet()
                || clazz?.clazzEndTime.isDateSet()

    val clazzHolidayCalendarVisible: Boolean
        get() = clazz?.clazzHolidayCalendar != null

    fun cbDescriptionVisible(courseBlock: CourseBlockWithCompleteEntity): Boolean {
        if (!courseBlock.cbDescription.isNullOrBlank())
            return true
        return false
    }

}