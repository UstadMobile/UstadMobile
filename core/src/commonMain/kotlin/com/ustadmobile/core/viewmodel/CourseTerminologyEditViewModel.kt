package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.TerminologyEntry

data class CourseTerminologyEditUiState(

    val titleError: String? = null,

    val entity: CourseTerminology? = null,

    val fieldsEnabled: Boolean = true,

    val terminologyTermList: List<TerminologyEntry> = emptyList()

)