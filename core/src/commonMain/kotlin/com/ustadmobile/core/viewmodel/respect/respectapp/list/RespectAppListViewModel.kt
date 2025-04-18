package com.ustadmobile.core.viewmodel.respect.respectapp.list

import app.cash.paging.PagingSource
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.respect.RespectApp
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR

data class RespectAppListUiState(
    val appList: () -> PagingSource<Int, RespectApp> = { EmptyPagingSource() },
)

class RespectAppListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): UstadListViewModel<RespectAppListUiState>(
    di, savedStateHandle, RespectAppListUiState(), destinationName
) {

    init {
        _appUiState.update { it.copy(title = systemImpl.getString(MR.strings.apps)) }
        _uiState.update { prev ->
            prev.copy(
                appList = { activeRepo.respectAppDao().findAllAsPagingSource() }
            )
        }
    }


    override fun onUpdateSearchResult(searchText: String) {

    }

    override fun onClickAdd() {

    }

    companion object {

        const val DEST_NAME = "RespectAppList"

    }
}