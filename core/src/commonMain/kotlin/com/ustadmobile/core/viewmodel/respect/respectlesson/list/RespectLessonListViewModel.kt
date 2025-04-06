package com.ustadmobile.core.viewmodel.respect.respectlesson.list

import app.cash.paging.PagingSource
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.respect.RespectLesson
import org.kodein.di.DI

data class RespectLessonListUiState(
    val lessons: () -> PagingSource<Int, RespectLesson> = { EmptyPagingSource() }
)

class RespectLessonListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : UstadListViewModel<RespectLessonListUiState>(
    di, savedStateHandle, RespectLessonListUiState(), DEST_NAME
){

    override fun onUpdateSearchResult(searchText: String) {

    }

    override fun onClickAdd() {

    }

    companion object {

        const val DEST_NAME = "RespectLessons"

    }

}