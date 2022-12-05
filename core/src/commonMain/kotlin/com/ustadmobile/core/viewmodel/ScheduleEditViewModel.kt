package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.Schedule


data class ScheduleEditUiState(

    val entity: Schedule? = null,

    val fromTimeError: String? = null,

    val toTimeError: String? = null,

    val fieldsEnabled: Boolean = true,

)