package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails

data class ClazzListUiState(

    val newClazzListOptionVisible: Boolean = true,

    val clazzList: List<ClazzWithListDisplayDetails> = emptyList(),

    val sortOptions: List<SortOrderOption> = listOf(
        SortOrderOption(MessageID.name, ClazzDaoCommon.SORT_CLAZZNAME_ASC, true),
        SortOrderOption(MessageID.name, ClazzDaoCommon.SORT_CLAZZNAME_DESC, false),
        SortOrderOption(MessageID.attendance, ClazzDaoCommon.SORT_ATTENDANCE_ASC, true),
        SortOrderOption(MessageID.attendance, ClazzDaoCommon.SORT_ATTENDANCE_DESC, false)
    ),

    val activeSortOrderOption: SortOrderOption = sortOptions.first(),

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED,

    val filterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MessageID.currently_enrolled, ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
        MessageIdOption2(MessageID.past_enrollments, ClazzDaoCommon.FILTER_PAST_ENROLLMENTS),
        MessageIdOption2(MessageID.all, 0)
    ),

)
