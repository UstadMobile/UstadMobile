package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ClazzLog

data class ClazzLogEditUiState(

    val fieldsEnabled: Boolean = true,

    val clazzLog: ClazzLog? = null,

    val timeZone: String = "UTC",

    val dateError: String? = null,

)