package com.ustadmobile.core.viewmodel.respect.respectassignment.list

import app.cash.paging.PagingSource
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.respect.RespectAssignment
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel

data class RespectAssignmentListUiState(
    val assignments: () -> PagingSource<Int, RespectAssignment> = { EmptyPagingSource() },
)

class RespectAssignmentListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): UstadListViewModel<RespectAssignmentListUiState>(
    di, savedStateHandle, RespectAssignmentListUiState(), destinationName,
) {

    private val pagingSourceFactory: () -> PagingSource<Int, RespectAssignment> = {
        activeRepo.respectAssignmentDao().findByPersonUid(activeUserPersonUid)
    }

    init {
        _appUiState.value = AppUiState(
            fabState = FabUiState(
                text = systemImpl.getString(MR.strings.assignment),
                icon = FabUiState.FabIcon.ADD,
                onClick = this::onClickAdd,
            ),
            title = systemImpl.getString(MR.strings.assignments),
        )

        _uiState.update { it.copy(assignments = pagingSourceFactory) }


        viewModelScope.launch {
            val hasSystemPermissionFlow = activeRepo.systemPermissionDao().personHasSystemPermissionAsFlow(
                accountPersonUid = activeUserPersonUid,
                permission = PermissionFlags.ADD_COURSE,
            )

            hasSystemPermissionFlow.combine(
                accountManager.currentUserSessionFlow
            ) { hasSystemPermission, currentSession ->
                hasSystemPermission || currentSession.person.personPrimaryRole == Person.PRIMARY_ROLE_TEACHER
            }.collect { showNewAssignment ->
                _appUiState.update { prev ->
                    prev.copy(
                        fabState = prev.fabState.copy(visible = showNewAssignment)
                    )
                }
            }
        }

    }

    override fun onUpdateSearchResult(searchText: String) {

    }

    override fun onClickAdd() {
        navController.navigate(
            viewName = ClazzListViewModel.DEST_NAME,
            args = mapOf(
                UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString()
            )
        )
    }

    companion object {

        const val DEST_NAME = "RespectAssignmentList"

    }
}