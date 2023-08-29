package com.ustadmobile.core.viewmodel.clazz.detailoverview

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.core.util.ext.toggle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.door.util.systemTimeInMillis
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

    val courseBlockList: () -> PagingSource<Int, CourseBlockWithCompleteEntity> = { EmptyPagingSource() },

    val clazzCodeVisible: Boolean = false,

    val collapsedBlockUids: Set<Long> = emptySet(),

) {
    val clazzSchoolUidVisible: Boolean
        get() = clazz?.clazzSchoolUid != null
                && clazz.clazzSchoolUid != 0L

    val clazzDateVisible: Boolean
        get() = clazz?.clazzStartTime.isDateSet()
                || clazz?.clazzEndTime.isDateSet()

    val clazzHolidayCalendarVisible: Boolean
        get() = clazz?.clazzHolidayCalendar != null

    fun cbDescriptionVisible(courseBlock: CourseBlockWithCompleteEntity): Boolean {
        if (!courseBlock.cbDescription.isNullOrBlank())
            return true
        return false
    }

}

class ClazzDetailOverviewViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String
): DetailViewModel<ClazzWithDisplayDetails>(di, savedStateHandle, destinationName) {


    private val _uiState = MutableStateFlow(ClazzDetailOverviewUiState())

    val uiState: Flow<ClazzDetailOverviewUiState> = _uiState.asStateFlow()

    private var lastCourseBlockPagingSource: PagingSource<Int, CourseBlockWithCompleteEntity>? = null

    private val pagingSourceFactory: () -> PagingSource<Int, CourseBlockWithCompleteEntity> = {
        activeRepo.courseBlockDao.findAllCourseBlockByClazzUidLive(
                entityUidArg, accountManager.activeAccount.personUid,
                _uiState.value.collapsedBlockUids.toList(), systemTimeInMillis()
        ).also {
            lastCourseBlockPagingSource?.invalidate()
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
                        _appUiState.update { prev ->
                            prev.copy(title = it?.clazzName ?: "")
                        }
                    }
                }

                launch {
                    activeRepo.clazzDao.personHasPermissionWithClazzAsFlow(
                        accountManager.activeAccount.personUid, entityUidArg, Role.PERMISSION_CLAZZ_UPDATE
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
            CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
                navController.navigate(ClazzAssignmentDetailView.VIEW_NAME,
                    mapOf(ARG_ENTITY_UID to courseBlock.cbEntityUid.toString()))
            }
            CourseBlock.BLOCK_DISCUSSION_TYPE -> {
                navController.navigate(
                    viewName = CourseDiscussionDetailViewModel.DEST_NAME,
                    args = mapOf(ARG_ENTITY_UID to courseBlock.cbUid.toString())
                )
            }
        }
    }

    fun onClickEdit() {
        navController.navigate(ClazzEdit2View.VIEW_NAME,
            mapOf(UstadView.ARG_ENTITY_UID to entityUidArg.toString()))
    }
}