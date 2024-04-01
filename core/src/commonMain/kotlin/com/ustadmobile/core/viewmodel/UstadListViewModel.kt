package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.AppBarSearchUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.EventCollator2
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import app.cash.paging.PagingSource
import com.ustadmobile.core.paging.RefreshCommand
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import org.kodein.di.DI

typealias ListPagingSourceFactory<T> = () -> PagingSource<Int, T>

/**
 *
 * Base class for list view models.
 *
 * Note: The UiState PagingSourceFactory needs to stay the same even when parameters change. If the
 * PagingSourceFactory changes, then we need a new Pager (Android) or QueryKey (JS). That will
 * cause flickering when the user searches, reorders, changes filtering chips etc. The list ViewModel
 * will therefor create a single PagingSourceFactory. When the PagingSourceFactory is invoked the
 * ViewModel will get a reference to the PagingSource that was created. That PagingSource will be
 * invalidated by the ViewModel if input parameters change (e.g. search text, sorting, filtering
 * options, etc).
 *
 * @param S the UI State type
 */
abstract class UstadListViewModel<S>(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    initialState: S,
    destinationName: String,
) : UstadViewModel(di, savedStateHandle, destinationName) {

    @Suppress("PropertyName")
    protected val _uiState = MutableStateFlow(initialState)

    val uiState: Flow<S> = _uiState.asStateFlow()

    protected val _refreshCommandFlow = MutableSharedFlow<RefreshCommand>(
        replay = 1, extraBufferCapacity = 0, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val refreshCommandFlow: Flow<RefreshCommand> = _refreshCommandFlow.asSharedFlow()



    open val listMode: ListViewMode
        get() {
            return if(
                savedStateHandle[UstadView.ARG_LISTMODE] == ListViewMode.PICKER.mode ||
                (
                    savedStateHandle[UstadView.ARG_RESULT_DEST_VIEWNAME] != null &&
                        savedStateHandle[UstadView.ARG_RESULT_DEST_KEY] != null
                )
            ) {
                ListViewMode.PICKER
            }else {
                ListViewMode.BROWSER
            }
        }


    /**
     * Avoid re-running search queries every single key stroke, whilst ensuring the search is
     * responsive.
     */
    private val searchEvtCollator = EventCollator2<String>(
        200, viewModelScope, 1, BufferOverflow.DROP_OLDEST
    ) {
        onUpdateSearchResult(it.lastOrNull() ?: "")
    }

    /**
     * Shorthand to create a SearchUiState that will display the search and collate events. The
     * PagingSource can then be updated in onUpdateSearchResult
     */
    protected fun createSearchEnabledState(visible: Boolean = true): AppBarSearchUiState {
        return AppBarSearchUiState(
            visible = visible,
            onSearchTextChanged = { searchText ->
                _appUiState.update { prev ->
                    prev.copy(
                        searchState = prev.searchState.copy(searchText = searchText)
                    )
                }
                searchEvtCollator.receiveEvent(searchText)
            }
        )
    }

    private fun setAddNewItemUiState(
        hasAddPermission: Boolean,
        fabStringResource: StringResource,
        onSetAddItemVisibility: (Boolean) -> Unit,
    ) {
        val fabState = if(hasAddPermission && listMode == ListViewMode.BROWSER) {
            FabUiState(
                visible = true,
                text = systemImpl.getString(fabStringResource),
                icon = FabUiState.FabIcon.ADD,
                onClick = this@UstadListViewModel::onClickAdd
            )
        }else {
            FabUiState(visible = false)
        }

        _appUiState.update { prev -> prev.copy(fabState = fabState) }

        onSetAddItemVisibility(hasAddPermission && listMode == ListViewMode.PICKER)
    }

    /**
     * This is the default event listener when using createSearchEnabledState(). It will
     * update the text on the search bar and use teh searchEvtCollator to trigger
     * onUpdateSearchResult
     */
    protected fun onSearchTextChanged(searchText: String) {
        _appUiState.update { prev ->
            prev.copy(searchState = prev.searchState.copy(searchText = searchText))
        }
        searchEvtCollator.receiveEvent(searchText)
    }

    /**
     * This should be implemented by a ListViewModel to update the list according to the search text
     * (if search is enabled). AppUiState.searchUiState should be updated using the state created by
     * createSearchEnabledState()
     */
    protected abstract fun onUpdateSearchResult(searchText: String)

    suspend fun collectHasPermissionFlowAndSetAddNewItemUiState(
        hasPermissionFlow: () -> Flow<Boolean>,
        fabStringResource: StringResource,
        onSetAddListItemVisibility: (Boolean) -> Unit,
    ) {
        _uiState.whenSubscribed {
            hasPermissionFlow().distinctUntilChanged().collect { hasAddPermission ->
                setAddNewItemUiState(hasAddPermission, fabStringResource, onSetAddListItemVisibility)
            }
        }
    }
    /**
     *
     */
    protected fun createFabState(
        hasAddPermission: Boolean,
        stringResource: StringResource,
        onClick: () -> Unit = this::onClickAdd
    ) : FabUiState{
        return if(hasAddPermission && listMode == ListViewMode.BROWSER) {
            FabUiState(
                visible = true,
                text = systemImpl.getString(stringResource),
                icon = FabUiState.FabIcon.ADD,
                onClick = onClick,
            )
        }else {
            FabUiState(visible = false)
        }
    }

    abstract fun onClickAdd()

    /**
     * Implements the default case for navigating to create a new item. If there is an expected
     * return result, then pass those arguments through. Otherwise, simply navigate to the edit
     * view name.
     */
    protected fun navigateToCreateNew(
        editViewName: String,
        extraArgs: Map<String, String> = emptyMap()
    ) {
        val resultDest = expectedResultDest
        val args =if(resultDest != null) {
            mapOf(
                UstadView.ARG_RESULT_DEST_VIEWNAME to resultDest.viewName,
                UstadView.ARG_RESULT_DEST_KEY to resultDest.key
            ) + extraArgs
        }else {
            extraArgs
        }

        navController.navigate(editViewName, args)
    }

    /**
     * Implements the default case for when a user clicks on an item in the list. If there is a
     * return result expected, then "return" the result via finishWithResult. Otherwise navigate to
     * the detail view, passing the entity uid.
     */
    protected fun navigateOnItemClicked(
        detailViewName: String,
        entityUid: Long,
        result: Any,
        extraArgs: Map<String, String> = emptyMap(),
    ) {
        val resultDest = expectedResultDest
        if(resultDest != null) {
            finishWithResult(result)
        }else {
            navController.navigate(
                detailViewName,
                buildMap {
                    putAll(extraArgs)
                    put(ARG_ENTITY_UID, entityUid.toString())
                }
            )
        }
    }

    protected fun listTitle(
        browseStringResource: StringResource, selectStringResource: StringResource
    ): String {
        return systemImpl.getString(
            if(listMode == ListViewMode.BROWSER){
                browseStringResource
            }else {
                selectStringResource
            }
        )
    }

    companion object {

        const val ARG_LISTMODE = "listMode"

    }



}