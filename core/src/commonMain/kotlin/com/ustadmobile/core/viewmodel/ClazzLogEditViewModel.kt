package com.ustadmobile.core.viewmodel

data class ClazzLogEditUiState(

    val fieldsEnabled: Boolean = true,

    val timeZone: String = "UTC",

    val dateError: String? = null,

    val timeError: String? = null,

    val date: Long = 0,

    //The time (ms since midnight)
    val time: Int = 0,

)