package com.ustadmobile.core.viewmodel.clazz.detailoverview

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.core.util.ext.toggle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import app.cash.paging.PagingSource
import com.ustadmobile.core.impl.locale.CourseTerminologyStrings
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.core.viewmodel.clazz.parseAndUpdateTerminologyStringsIfNeeded
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.core.viewmodel.contententry.detail.ContentEntryDetailViewModel
import com.ustadmobile.core.viewmodel.courseblock.textblockdetail.TextBlockDetailViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CourseBlockAndDisplayDetails
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ClazzDetailOverviewUiState(

    val clazz: ClazzWithDisplayDetails? = null,

    val scheduleList: List<Schedule> = emptyList(),

    val courseBlockList: () -> PagingSource<Int, CourseBlockAndDisplayDetails> = { EmptyPagingSource() },

    val clazzCodeVisible: Boolean = true,

    val collapsedBlockUids: Set<Long> = emptySet(),

    val terminologyStrings: CourseTerminologyStrings? = null,

) {
    val clazzSchoolUidVisible: Boolean
        get() = clazz?.clazzSchoolUid != null && clazz.clazzSchoolUid != 0L

    val clazzDateVisible: Boolean
        get() = clazz?.clazzStartTime.isDateSet()
                || clazz?.clazzEndTime.isDateSet()

    val clazzHolidayCalendarVisible: Boolean
        get() = clazz?.clazzHolidayCalendar != null

    val numStudents: Int
        get() = clazz?.numStudents ?: 0

    val numTeachers: Int
        get() = clazz?.numTeachers ?: 0

    val membersString: String
        get() = "${terminologyStrings?.get(MR.strings.teachers_literal) ?: ""}: $numTeachers, " +
                "${terminologyStrings?.get(MR.strings.students) ?: ""}: $numStudents"

}

class ClazzDetailOverviewViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): DetailViewModel<ClazzWithDisplayDetails>(di, savedStateHandle, destinationName) {

    private val _uiState = MutableStateFlow(ClazzDetailOverviewUiState())

    val uiState: Flow<ClazzDetailOverviewUiState> = _uiState.asStateFlow()

    private var lastCourseBlockPagingSource: PagingSource<Int, CourseBlockAndDisplayDetails>? = null

    private val pagingSourceFactory: () -> PagingSource<Int, CourseBlockAndDisplayDetails> = {
        activeRepo.courseBlockDao.findAllCourseBlockByClazzUidAsPagingSource(
            clazzUid = entityUidArg,
            collapseList = _uiState.value.collapsedBlockUids.toList(),
            includeInactive = false,
            includeHidden = false,
            hideUntilFilterTime = systemTimeInMillis(),
        ).also {
            lastCourseBlockPagingSource = it
        }
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                fabState = FabUiState(
                    visible = false,
                    text = systemImpl.getString(MR.strings.edit),
                    icon = FabUiState.FabIcon.EDIT,
                    onClick = this::onClickEdit
                )
            )
        }
        _uiState.update { prev ->
            prev.copy(
                courseBlockList = pagingSourceFactory,
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.clazzDao.getClazzWithDisplayDetails(
                        entityUidArg, systemTimeInMillis()
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(clazz = it)
                        }

                        parseAndUpdateTerminologyStringsIfNeeded(
                            currentTerminologyStrings = _uiState.value.terminologyStrings,
                            terminology = it?.terminology,
                            json = json,
                            systemImpl = systemImpl,
                        ) {
                            _uiState.update { prev -> prev.copy(terminologyStrings = it) }
                        }

                        _appUiState.update { prev ->
                            prev.copy(title = it?.clazzName ?: "")
                        }
                    }
                }

                launch {
                    activeRepo.clazzDao.personHasPermissionWithClazzAsFlow(
                        accountManager.currentAccount.personUid, entityUidArg, Role.PERMISSION_CLAZZ_UPDATE
                    ).collect { hasEditPermission ->
                        _appUiState.update { prev ->
                            prev.copy(
                                fabState = prev.fabState.copy(visible = hasEditPermission)
                            )
                        }
                    }
                }
            }
        }
    }

    fun onClickCourseBlock(courseBlock: CourseBlock) {
        when(courseBlock.cbType) {
            CourseBlock.BLOCK_MODULE_TYPE -> {
                _uiState.update { prev ->
                    prev.copy(
                        collapsedBlockUids = prev.collapsedBlockUids.toggle(courseBlock.cbUid)
                    )
                }
                lastCourseBlockPagingSource?.invalidate()
            }
            CourseBlock.BLOCK_TEXT_TYPE -> {
                navController.navigate(TextBlockDetailViewModel.DEST_NAME,
                    mapOf(ARG_ENTITY_UID to courseBlock.cbUid.toString()))
            }
            CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
                navController.navigate(ClazzAssignmentDetailViewModel.DEST_NAME,
                    mapOf(ARG_ENTITY_UID to courseBlock.cbEntityUid.toString()))
            }
            CourseBlock.BLOCK_DISCUSSION_TYPE -> {
                navController.navigate(
                    viewName = CourseDiscussionDetailViewModel.DEST_NAME,
                    args = mapOf(ARG_ENTITY_UID to courseBlock.cbUid.toString())
                )
            }
            CourseBlock.BLOCK_CONTENT_TYPE -> {
                navController.navigate(
                    viewName = ContentEntryDetailViewModel.DEST_NAME,
                    args = mapOf(ARG_ENTITY_UID to courseBlock.cbEntityUid.toString())
                )
            }
        }
    }

    private fun onClickEdit() {
        navController.navigate(ClazzEditViewModel.DEST_NAME,
            mapOf(UstadView.ARG_ENTITY_UID to entityUidArg.toString()))
    }

    companion object {

        const val DEST_NAME = "CourseDetailOverviewView"

    }
}