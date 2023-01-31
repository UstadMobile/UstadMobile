package com.ustadmobile.core.viewmodel

data class JoinWithCodeUiState(

    val codeError: String? = null,

    val code: String = "",

    val entityType: String = "",

    val buttonLabel: String = "",

    val fieldsEnabled: Boolean = true,

)