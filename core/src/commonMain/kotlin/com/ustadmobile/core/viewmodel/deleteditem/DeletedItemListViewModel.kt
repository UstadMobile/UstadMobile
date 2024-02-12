package com.ustadmobile.core.viewmodel.deleteditem

import app.cash.paging.PagingSource
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.DeletedItem
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR

data class DeletedItemListUiState(
    val deletedItemsList: ListPagingSourceFactory<DeletedItem> = { EmptyPagingSource() }

)

class DeletedItemListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
): UstadListViewModel<DeletedItemListUiState>(di, savedStateHandle, DeletedItemListUiState(), DEST_NAME) {

    private val pagingSourceFactory: ListPagingSourceFactory<DeletedItem> = {
        activeRepo.deletedItemDao.findDeletedItemsForUser(
            personUid = activeUserPersonUid,
            includeActionedItems = false,
        ).also {
            lastPagingSource = it
        }
    }

    private var lastPagingSource: PagingSource<Int, DeletedItem>? = null

    init {
        _uiState.update { prev ->
            prev.copy(
                deletedItemsList = pagingSourceFactory
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.deleted_items)
            )
        }
    }


    override fun onUpdateSearchResult(searchText: String) {

    }

    fun onClickRestore(item: DeletedItem) {

    }

    fun onClickDeletePermanently(item: DeletedItem) {

    }

    override fun onClickAdd() {
        //there is no such things adding a deleted item
    }

    companion object {

        const val DEST_NAME = "DeletedItems"
    }
}