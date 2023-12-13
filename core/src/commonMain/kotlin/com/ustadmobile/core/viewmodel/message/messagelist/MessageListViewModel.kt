package com.ustadmobile.core.viewmodel.message.messagelist

import app.cash.paging.PagingSource
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.composites.MessageAndSenderPerson

data class MessageListUiState(
    val messages: () -> PagingSource<Int, MessageAndSenderPerson> = { EmptyPagingSource() },
    val activePersonUid: Long = 0,
    val sortOptions: List<SortOrderOption> = emptyList() //Should be by name (ascending/descending), time (ascending/descending)
)

