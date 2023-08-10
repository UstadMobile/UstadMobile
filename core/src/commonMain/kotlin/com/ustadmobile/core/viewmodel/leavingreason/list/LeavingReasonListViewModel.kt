package com.ustadmobile.core.viewmodel.leavingreason.list

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.LeavingReasonListView
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditViewModel
import com.ustadmobile.core.viewmodel.leavingreason.edit.LeavingReasonEditViewModel
import com.ustadmobile.core.viewmodel.person.PersonViewModelConstants
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
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
    di, savedStateHandle, LeavingReasonListUiState(), LeavingReasonListView.VIEW_NAME
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
        navigateToCreateNew(LeavingReasonEditViewModel.DEST_NAME, emptyMap())
    }

    fun onClickLeavingReason(leavingReason: LeavingReason) {
        navigateForResult(
            nextViewName = LeavingReasonEditViewModel.DEST_NAME,
            key = "",
            currentValue = null,
            serializer = ClazzEnrolmentWithLeavingReason.serializer(),
            args = mapOf(UstadView.ARG_ENTITY_UID to leavingReason.leavingReasonUid.toString()),
            overwriteDestination = true,
        )
    }
}