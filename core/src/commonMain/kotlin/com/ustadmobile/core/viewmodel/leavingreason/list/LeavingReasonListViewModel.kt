package com.ustadmobile.core.viewmodel.leavingreason.list

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.lib.db.entities.LeavingReason
import kotlinx.coroutines.flow.update
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

    private var lastPagingSource: PagingSource<Int, LeavingReason>? = null

    private val pagingSourceFactory: () -> PagingSource<Int, LeavingReason> = {
        activeRepo.leavingReasonDao.findAllReasonsAsPagingSource().also {
            lastPagingSource?.invalidate()
            lastPagingSource = it
        }
    }


    init {
        _uiState.update { prev ->
            prev.copy(
                leavingReasonList = pagingSourceFactory,
            )
        }
    }


    override fun onUpdateSearchResult(searchText: String) {
        // do nothing
    }

    override fun onClickAdd() {
        lastPagingSource?.invalidate()
    }

    fun onClickLeavingReason(leavingReason: LeavingReason) {

    }

    companion object {

        const val DEST_NAME = "LeavingReasonList"

    }
}