package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.*

data class ClazzEditUiState(

    val fieldsEnabled: Boolean = true,

    val entity: ClazzWithHolidayCalendarAndSchoolAndTerminology? = null,

    val clazzStartDateError: String? = null,

    val clazzEndDateError: String? = null,

    val clazzSchedules: List<Schedule> = emptyList(),

    val courseBlockList: List<CourseBlockWithEntity> = emptyList(),

    val timeZone: String = "UTC"

) {

    class CourseBlockUiState internal constructor(
        val courseBlock: CourseBlockWithEntity
    ) {
        val showIndent: Boolean
            get() = courseBlock.cbType != CourseBlock.BLOCK_MODULE_TYPE && courseBlock.cbIndentLevel < BLOCK_MAX_INDENT

        val showUnindent: Boolean
            get() = courseBlock.cbIndentLevel > 0

        val showHide: Boolean
            get() = !courseBlock.cbHidden

        val showUnhide: Boolean
            get() = courseBlock.cbHidden
    }


    val clazzEditAttendanceChecked: Boolean
        get() = entity?.clazzFeatures == Clazz.CLAZZ_FEATURE_ATTENDANCE
                && Clazz.CLAZZ_FEATURE_ATTENDANCE == Clazz.CLAZZ_FEATURE_ATTENDANCE

    fun courseBlockStateFor(couresBlockWithEntity: CourseBlockWithEntity): CourseBlockUiState {
        return CourseBlockUiState(couresBlockWithEntity)
    }

    companion object {

        const val BLOCK_MAX_INDENT = 3

    }

}