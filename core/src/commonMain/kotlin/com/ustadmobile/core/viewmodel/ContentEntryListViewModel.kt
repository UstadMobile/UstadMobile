package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer

data class ContentEntryListUiState(

    val contentEntryList: List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> =
        emptyList(),

)