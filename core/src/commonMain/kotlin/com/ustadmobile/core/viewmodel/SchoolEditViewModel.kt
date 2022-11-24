package com.ustadmobile.core.viewmodel

import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.ScopedGrantAndName

data class SchoolEditUiState(

    val fieldsEnabled: Boolean = true,

    val entity: SchoolWithHolidayCalendar? = null,

    val scopedGrants: List<ScopedGrantAndName> = emptyList()

)