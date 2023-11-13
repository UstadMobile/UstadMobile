package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar

data class SchoolEditUiState(

    val fieldsEnabled: Boolean = true,

    val entity: SchoolWithHolidayCalendar? = null,

)

class SchoolEditViewModel  {

    companion object {

        const val DEST_NAME = "SchoolEdit"

    }
}
