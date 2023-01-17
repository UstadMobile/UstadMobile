package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.PersonWithSessionsDisplay

data class SessionListUiState (

    val personWithContentTitle: String = "",

    val sessionsList: List<PersonWithSessionsDisplay> = emptyList()

)