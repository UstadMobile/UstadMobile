package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails

data class ClazzListUiState(

    val newClazzListOptionVisible: Boolean = true,

    val clazzList: List<ClazzWithListDisplayDetails> = emptyList()

)