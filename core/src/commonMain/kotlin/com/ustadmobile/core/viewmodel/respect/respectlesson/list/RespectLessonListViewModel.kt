package com.ustadmobile.core.viewmodel.respect.respectlesson.list

import app.cash.paging.PagingSource
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.respect.RespectLessonAndApp
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.respect.respectassignment.edit.RespectAssignmentEditViewModel

data class RespectLessonListUiState(
    val lessons: () -> PagingSource<Int, RespectLessonAndApp> = { EmptyPagingSource() }
)

class RespectLessonListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : UstadListViewModel<RespectLessonListUiState>(
    di, savedStateHandle, RespectLessonListUiState(), DEST_NAME
){

    private val pagingSource: () -> PagingSource<Int, RespectLessonAndApp> = {
        activeRepo.respectLessonDao().allLessons()
    }

    private val taskName = savedStateHandle[ARG_TASK]

    init {
        _appUiState.update {
            it.copy(title = systemImpl.getString(MR.strings.select_lesson))
        }
        _uiState.update { it.copy(lessons = pagingSource) }
    }

    override fun onUpdateSearchResult(searchText: String) {

    }

    override fun onClickAdd() {
        //Not used
    }

    fun onClickItem(lesson: RespectLessonAndApp) {
        if(taskName == RespectAssignmentEditViewModel.ARG_TASK_NAME_NEW_ASSIGNMENT) {
            navController.navigate(
                viewName = RespectAssignmentEditViewModel.DEST_NAME,
                args = buildMap {
                    putFromSavedStateIfPresent(RespectAssignmentEditViewModel.NEW_ASSIGNMENT_ARGS_TO_PASS)
                    put(RespectAssignmentEditViewModel.ARG_RESPECT_LESSON_UID,
                        lesson.lesson.rlUid.toString())
                }
            )
        }
    }

    companion object {

        const val DEST_NAME = "RespectLessons"


    }

}