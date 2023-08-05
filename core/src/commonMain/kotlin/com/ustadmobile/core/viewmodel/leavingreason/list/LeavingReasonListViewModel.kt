package com.ustadmobile.core.viewmodel.leavingreason.list

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails
import org.kodein.di.DI

data class LeavingReasonListUiState(

    val leavingReasonList: ListPagingSourceFactory<LeavingReason> = { EmptyPagingSource() },

)

class LeavingReasonListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadListViewModel<LeavingReasonListUiState>(
    di, savedStateHandle, LeavingReasonListUiState(), DEST_NAME
) {

    init {

    }

    companion object {

        const val DEST_NAME = "LeavingReasonList"

    }

    override fun onUpdateSearchResult(searchText: String) {
        TODO("Not yet implemented")
    }

    override fun onClickAdd() {
        TODO("Not yet implemented")
    }

}