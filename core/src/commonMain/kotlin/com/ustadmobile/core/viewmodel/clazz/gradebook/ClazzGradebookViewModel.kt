package com.ustadmobile.core.viewmodel.clazz.gradebook

import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CourseBlockAndGradebookDisplayDetails
import com.ustadmobile.lib.db.composites.StudentAndBlockStatuses
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ClazzGradebookUiState(
    val courseBlocks: List<CourseBlockAndGradebookDisplayDetails> = emptyList(),
    val results: ListPagingSourceFactory<StudentAndBlockStatuses> = { EmptyPagingSource() },
    val isFullScreen: Boolean = false,
)

/**
 *
 */
class ClazzGradebookViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadListViewModel<ClazzGradebookUiState>(
    di, savedStateHandle, ClazzGradebookUiState(), DEST_NAME
) {

    private val clazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong()
        ?: throw IllegalArgumentException("No clazzuid")

    private val studentPagingSource: ListPagingSourceFactory<StudentAndBlockStatuses> = {
        ClazzProgressReportPagingSource(
            studentListPagingSource = activeRepo.clazzEnrolmentDao.findByClazzUidAndRole(
                clazzUid = clazzUid,
                roleId = ClazzEnrolment.ROLE_STUDENT,
                sortOrder = ClazzEnrolmentDaoCommon.SORT_FIRST_NAME_ASC,
                searchText = "%",
                filter = 0,
                accountPersonUid = activeUserPersonUid,
                currentTime = systemTimeInMillis(),
                permission = PermissionFlags.PERSON_VIEW
            ),
            db = activeDb,
        )
    }


    init {
        viewModelScope.launch {
            activeRepo.courseBlockDao.findByClazzUidAsFlow(
                clazzUid = clazzUid
            ).distinctUntilChanged().collect { courseBlockList ->
                _uiState.update { prev ->
                    prev.copy(
                        courseBlocks = courseBlockList,
                        results = studentPagingSource,
                    )
                }
            }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {

    }

    override fun onClickAdd() {
        //Do nothing
    }

    fun onClickFullScreen() {
        val isFullScreen = _uiState.updateAndGet {
            it.copy(isFullScreen = !it.isFullScreen)
        }.isFullScreen

        _appUiState.update {
            it.copy(
                hideAppBar = isFullScreen,
                hideBottomNavigation = isFullScreen,
            )
        }
    }

    companion object {

        const val DEST_NAME = "Gradebook"

    }
}