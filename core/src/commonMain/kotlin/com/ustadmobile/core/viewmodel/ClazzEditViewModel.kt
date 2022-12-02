package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchoolAndTerminology

data class ClazzEditUiState(

    val fieldsEnabled: Boolean = true,

    val entity: ClazzWithHolidayCalendarAndSchoolAndTerminology? = null,

    val clazzDescError: String? = null,

    val institutionError: String? = null,

    val clazzStartDateError: String? = null,

    val clazzEndDateError: String? = null,
) {

    val clazzEditAttendanceChecked: Boolean
        get() = entity?.clazzFeatures == Clazz.CLAZZ_FEATURE_ATTENDANCE
                && Clazz.CLAZZ_FEATURE_ATTENDANCE == Clazz.CLAZZ_FEATURE_ATTENDANCE

}