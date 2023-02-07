package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation

data class SchoolListUiState(

    val newSchoolListOptionVisible: Boolean = false,

    val schoolList: List<SchoolWithMemberCountAndLocation> = emptyList()

)