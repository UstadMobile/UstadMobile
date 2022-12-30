package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.MessageIdOption2


data class FilterChipsHeaderUiState(

    val filterOptions: List<MessageIdOption2> = emptyList<MessageIdOption2>(),

    val selectedFilterOption: MessageIdOption2? = null,

    val fieldsEnabled: Boolean = true,

)