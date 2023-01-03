package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails

data class ClazzListUiState(

    val newClazzListOptionVisible: Boolean = true,

    val clazzList: List<ClazzWithListDisplayDetails> = emptyList(),

    val activeSortOrderOption: SortOrderOption = SortOrderOption(
        MessageID.name,
        ClazzDaoCommon.SORT_CLAZZNAME_ASC,
        true
    ),

    val fieldsEnabled: Boolean = true,

    val filterOptions: List<MessageIdOption2> = emptyList(),

    val selectedChipId: Int = ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED
)