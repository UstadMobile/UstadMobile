package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * @param S the UI State type
 */
abstract class UstadListViewModel<S>(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    initialState: S,
) : UstadViewModel(di, savedStateHandle) {

    protected val systemImpl: UstadMobileSystemImpl by instance()

    @Suppress("PropertyName")
    protected val _uiState = MutableStateFlow(initialState)

    val uiState: Flow<S> = _uiState.asStateFlow()

    open val listMode: ListViewMode
        get() {
            return if(savedStateHandle[UstadView.ARG_RESULT_DEST_VIEWNAME] != null &&
                savedStateHandle[UstadView.ARG_RESULT_DEST_KEY] != null
            ) {
                ListViewMode.PICKER
            }else {
                ListViewMode.BROWSER
            }
        }

    private fun setAddNewItemUiState(
        hasAddPermission: Boolean,
        fabMessageId: Int,
        onSetAddItemVisibility: (Boolean) -> Unit,
    ) {
        val fabState = if(hasAddPermission && listMode == ListViewMode.BROWSER) {
            FabUiState(
                visible = true,
                text = systemImpl.getString(fabMessageId),
                icon = FabUiState.FabIcon.ADD,
                onClick = this@UstadListViewModel::onClickAdd
            )
        }else {
            FabUiState(visible = false)
        }

        _appUiState.update { prev -> prev.copy(fabState = fabState) }

        onSetAddItemVisibility(hasAddPermission && listMode == ListViewMode.PICKER)
    }


    suspend fun collectHasPermissionFlowAndSetAddNewItemUiState(
        hasPermissionFlow: () -> Flow<Boolean>,
        fabMessageId: Int,
        onSetAddItemVisibility: (Boolean) -> Unit,
    ) {
        _uiState.whenSubscribed {
            hasPermissionFlow().distinctUntilChanged().collect { hasAddPermission ->
                setAddNewItemUiState(hasAddPermission, fabMessageId, onSetAddItemVisibility)
            }
        }
    }


    /**
     *
     */
    protected fun createFabState(
        hasAddPermission: Boolean,
        messageId: Int
    ) : FabUiState{
        return if(hasAddPermission && listMode == ListViewMode.BROWSER) {
            FabUiState(
                visible = true,
                text = systemImpl.getString(messageId),
                icon = FabUiState.FabIcon.ADD,
                onClick = this@UstadListViewModel::onClickAdd
            )
        }else {
            FabUiState(visible = false)
        }
    }

    abstract fun onClickAdd()

}