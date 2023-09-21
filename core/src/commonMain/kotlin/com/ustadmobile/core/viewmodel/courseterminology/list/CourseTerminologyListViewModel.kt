package com.ustadmobile.core.viewmodel.courseterminology.list

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.CourseTerminologyEditView
import com.ustadmobile.core.view.CourseTerminologyListView
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import app.cash.paging.PagingSource
import com.ustadmobile.lib.db.entities.CourseTerminology
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI


data class CourseTerminologyListUiState(
    val terminologyList: () -> PagingSource<Int, CourseTerminology> = { EmptyPagingSource() },
    val showAddItemInList: Boolean = false,
)

class CourseTerminologyListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadListViewModel<CourseTerminologyListUiState>(
    di, savedStateHandle, CourseTerminologyListUiState(), CourseTerminologyListView.VIEW_NAME,
) {

    private val pagingSourceFactory: () -> PagingSource<Int, CourseTerminology> = {
        activeDb.courseTerminologyDao.findAllCourseTerminologyPagingSource()
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = listTitle(MR.strings.terminology, MR.strings.select_terminology)
            )

        }
        _uiState.update { prev ->
            prev.copy(
                terminologyList = pagingSourceFactory
            )
        }

        viewModelScope.launch {
            collectHasPermissionFlowAndSetAddNewItemUiState(
                hasPermissionFlow = {
                    flowOf(true)
                },
                fabStringResource = MR.strings.terminology,
                onSetAddListItemVisibility = {
                    _uiState.update { prev -> prev.copy(showAddItemInList = it) }
                }
            )
        }
    }

    override fun onClickAdd() {
        navigateToCreateNew(CourseTerminologyEditView.VIEW_NAME)
    }

    fun onClickEntry(entry: CourseTerminology) {
        navigateOnItemClicked(CourseTerminologyEditView.VIEW_NAME, entry.ctUid, entry)
    }

    override fun onUpdateSearchResult(searchText: String) {
        //not used
    }

    companion object {

        const val DEST_NAME = "CourseTerminologyList"

    }
}