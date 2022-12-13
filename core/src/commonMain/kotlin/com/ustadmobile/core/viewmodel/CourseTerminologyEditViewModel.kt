package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseTerminology

data class CourseTerminologyEditUiState(

    val titleError: String? = null,

    val entity: CourseTerminology? = null,

    val fieldsEnabled: Boolean = true,

)