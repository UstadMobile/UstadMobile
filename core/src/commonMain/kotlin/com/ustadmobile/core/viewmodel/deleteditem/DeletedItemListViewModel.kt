package com.ustadmobile.core.viewmodel.deleteditem

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.DeletedItem
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.deleteditem.DeletePermanentlyUseCase
import com.ustadmobile.core.domain.deleteditem.RestoreDeletedItemUseCase
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.util.ext.onActiveEndpoint
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import org.kodein.di.instance

data class DeletedItemListUiState(
    val deletedItemsList: ListPagingSourceFactory<DeletedItem> = { EmptyPagingSource() },
    val confirmDialogVisible: Boolean = false,
    val deleteConfirmText: String? = null,
    val itemsToConfirmDeletion: List<DeletedItem> = emptyList(),
)

class DeletedItemListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
): UstadListViewModel<DeletedItemListUiState>(di, savedStateHandle, DeletedItemListUiState(), DEST_NAME) {

    private val pagingSourceFactory: ListPagingSourceFactory<DeletedItem> = {
        activeRepoWithFallback.deletedItemDao().findDeletedItemsForUser(
            personUid = activeUserPersonUid,
            includeActionedItems = false,
        )
    }

    private val restoreDeletedItemUseCase: RestoreDeletedItemUseCase by di.onActiveEndpoint()
        .instance()

    private val deletePermanentlyUseCase: DeletePermanentlyUseCase by di.onActiveEndpoint()
        .instance()

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

    fun onDismissConfirmDialog() {
        _uiState.update { prev ->
            prev.copy(
                confirmDialogVisible = false,
            )
        }
    }

    fun onConfirmDeletePermanently() {
        val itemsToDelete = _uiState.getAndUpdate {
            prev -> prev.copy(
                confirmDialogVisible = false
            )
        }.itemsToConfirmDeletion

        viewModelScope.launch {
            try {
                deletePermanentlyUseCase(itemsToDelete)
                snackDispatcher.showSnackBar(
                    Snack(systemImpl.formatPlural(MR.plurals.items_deleted, itemsToDelete.size))
                )
            }catch(e: Throwable) {
                Napier.e("Exception deleting", e)
                snackDispatcher.showSnackBar(
                    Snack("${systemImpl.getString(MR.strings.error)}: ${e.message}")
                )
            }
        }
    }

    private fun restoreItems(
        items: List<DeletedItem>
    ) {
       viewModelScope.launch {
           restoreDeletedItemUseCase(items)
           snackDispatcher.showSnackBar(
               Snack(systemImpl.formatPlural(MR.plurals.items_restored, items.size))
           )
       }
    }

    fun onClickRestore(item: DeletedItem) {
        restoreItems(listOf(item))
    }

    fun onClickDeletePermanently(item: DeletedItem) {
        _uiState.update { prev ->
            prev.copy(
                itemsToConfirmDeletion = listOf(item),
                deleteConfirmText = systemImpl.formatPlural(
                    MR.plurals.are_you_sure_you_want_to_permanently_delete, 1
                ),
                confirmDialogVisible = true
            )
        }
    }

    override fun onClickAdd() {
        //there is no such things adding a deleted item
    }

    companion object {

        const val DEST_NAME = "DeletedItems"
    }
}