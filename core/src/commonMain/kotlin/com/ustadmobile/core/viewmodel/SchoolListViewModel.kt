package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation
import kotlin.jvm.JvmInline

data class SchoolListUiState(

    val newSchoolListOptionVisible: Boolean = false,

    val schoolList: List<SchoolWithMemberCountAndLocation> = emptyList()

)

val SchoolWithMemberCountAndLocation.listItemUiState
    get() = SchoolWithMemberCountAndLocationUiState(this)

@JvmInline
value class SchoolWithMemberCountAndLocationUiState(
    val school: SchoolWithMemberCountAndLocation,
) {

    val schoolAddressVisible: Boolean
        get() = !school.schoolAddress.isNullOrBlank()

}