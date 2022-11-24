package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar

data class SchoolDetailOverviewUiState(

    var entity: SchoolWithHolidayCalendar? = null,

    val schoolCodeVisible: Boolean = false,

    val clazzes: List<ClazzWithListDisplayDetails> = emptyList(),

) {

    val schoolDescVisible: Boolean
        get() = !entity?.schoolDesc.isNullOrBlank()

    val schoolGenderVisible: Boolean
        get() = entity?.schoolGender != null
                && entity?.schoolGender != 0

    val schoolCodeLayoutVisible: Boolean
        get() = entity?.schoolCode != null
                && schoolCodeVisible

    val schoolAddressVisible: Boolean
        get() = !entity?.schoolAddress.isNullOrBlank()

    val schoolPhoneNumberVisible: Boolean
        get() = !entity?.schoolPhoneNumber.isNullOrBlank()

    val calendarUidVisible: Boolean
        get() = entity?.schoolHolidayCalendarUid != null
                && entity?.schoolHolidayCalendarUid != 0L

    val schoolEmailAddressVisible: Boolean
        get() = !entity?.schoolEmailAddress.isNullOrBlank()

    val schoolTimeZoneVisible: Boolean
        get() = !entity?.schoolTimeZone.isNullOrBlank()

}