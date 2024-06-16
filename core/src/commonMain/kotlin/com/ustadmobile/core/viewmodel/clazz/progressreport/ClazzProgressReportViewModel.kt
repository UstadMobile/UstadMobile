package com.ustadmobile.core.viewmodel.clazz.progressreport

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.composites.StudentAndBlockStatuses
import org.kodein.di.DI

data class ClazzProgressReportUiState(
    val results: ListPagingSourceFactory<StudentAndBlockStatuses> = { EmptyPagingSource() }
)

/**
 *
 */
class ClazzProgressReportViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadListViewModel<ClazzProgressReportUiState>(
    di, savedStateHandle, ClazzProgressReportUiState(), DEST_NAME
) {

    override fun onUpdateSearchResult(searchText: String) {

    }

    override fun onClickAdd() {
        //Do nothing
    }

    companion object {

        const val DEST_NAME = "ClazzProgress"

    }
}