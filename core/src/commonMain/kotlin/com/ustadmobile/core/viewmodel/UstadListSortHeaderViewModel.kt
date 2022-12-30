package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption

data class UstadListSortHeaderUiState (

    val sortOption: SortOrderOption? = null,

    val sortDirection: Int? = null, //Ascending or descending

    val sortBy: String = "",

    val filterOptions: List<MessageIdOption2> = emptyList(),

    val selectedFilterOption: MessageIdOption2? = null,

)
