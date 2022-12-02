package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchoolAndTerminology
import com.ustadmobile.lib.db.entities.Schedule

data class ClazzEditUiState(

    val fieldsEnabled: Boolean = true,

    val entity: ClazzWithHolidayCalendarAndSchoolAndTerminology? = null,

    val clazzStartDateError: String? = null,

    val clazzEndDateError: String? = null,

    var clazzSchedules: List<Schedule> = emptyList()

) {

    val clazzEditAttendanceChecked: Boolean
        get() = entity?.clazzFeatures == Clazz.CLAZZ_FEATURE_ATTENDANCE
                && Clazz.CLAZZ_FEATURE_ATTENDANCE == Clazz.CLAZZ_FEATURE_ATTENDANCE

}