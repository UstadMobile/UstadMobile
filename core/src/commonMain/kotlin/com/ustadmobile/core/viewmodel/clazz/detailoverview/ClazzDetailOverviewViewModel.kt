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
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.clipboard.SetClipboardStringUseCase
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.locale.CourseTerminologyStrings
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.viewmodel.UstadEditViewModel.Companion.INIT_PIC_URI
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel.Companion.STATE_KEY_SCHEDULES
import com.ustadmobile.core.viewmodel.clazz.parseAndUpdateTerminologyStringsIfNeeded
import com.ustadmobile.core.viewmodel.clazz.permissionlist.CoursePermissionListViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.core.viewmodel.contententry.detail.ContentEntryDetailViewModel
import com.ustadmobile.core.viewmodel.courseblock.textblockdetail.TextBlockDetailViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.lib.db.composites.ClazzAndDisplayDetails
import com.ustadmobile.lib.db.composites.CourseBlockAndDisplayDetails
import com.ustadmobile.lib.db.entities.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import kotlin.getValue
import kotlinx.serialization.encodeToString


data class ClazzDetailOverviewUiState(

    val clazzAndDetail: ClazzAndDisplayDetails? = null,

    val scheduleList: List<Schedule> = emptyList(),

    val coursePicture: CoursePicture?=null,

    val courseBlockList: List<CourseBlockAndDisplayDetails> = emptyList(),

    val blockStatusesForActiveUser: List<BlockStatus> = emptyList(),

    val clazzCodeVisible: Boolean = false,

    val collapsedBlockUids: Set<Long> = emptySet(),

    val terminologyStrings: CourseTerminologyStrings? = null,

    val managePermissionVisible: Boolean = false,

    val canAddNewCourse: Boolean = false,

    ) {
    val clazz: Clazz?
        get() = clazzAndDetail?.clazz


    val clazzDateVisible: Boolean
        get() = clazzAndDetail?.clazz?.clazzStartTime.isDateSet()
                || clazzAndDetail?.clazz?.clazzEndTime.isDateSet()

    val numStudents: Int
        get() = clazzAndDetail?.numStudents ?: 0

    val numTeachers: Int
        get() = clazzAndDetail?.numTeachers ?: 0

    val hasModules: Boolean
        get() = courseBlockList.any { it.courseBlock?.cbType == CourseBlock.BLOCK_MODULE_TYPE }

    val membersString: String
        get() = "${terminologyStrings?.get(MR.strings.teachers_literal) ?: ""}: $numTeachers, " +
                "${terminologyStrings?.get(MR.strings.students) ?: ""}: $numStudents"

    val quickActionBarVisible: Boolean
        get() = managePermissionVisible

    val displayBlockList: List<CourseBlockAndDisplayDetails> by lazy(LazyThreadSafetyMode.NONE) {
        courseBlockList.filter {
            !collapsedBlockUids.contains(it.courseBlock?.cbModuleParentBlockUid ?: 0)
        }
    }

}

class ClazzDetailOverviewViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): DetailViewModel<ClazzWithDisplayDetails>(di, savedStateHandle, destinationName) {

    private val _uiState = MutableStateFlow(ClazzDetailOverviewUiState())

    val uiState: Flow<ClazzDetailOverviewUiState> = _uiState.asStateFlow()

    private val setClipboardStringUseCase: SetClipboardStringUseCase by instance()

    private val _listRefreshCommandFlow = MutableSharedFlow<RefreshCommand>(
        replay = 1, extraBufferCapacity = 0, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val listRefreshCommandFlow: Flow<RefreshCommand> = _listRefreshCommandFlow.asSharedFlow()

    private val copyCourseUseCase: CopyCourseUseCase by di.onActiveEndpoint().instance()


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

        viewModelScope.launch {
            _uiState.whenSubscribed {
                activeRepo.systemPermissionDao().personHasSystemPermissionAsFlow(
                    accountManager.currentAccount.personUid, PermissionFlags.ADD_COURSE
                ).distinctUntilChanged().collect { hasPermission ->
                    _uiState.update { prev ->
                        prev.copy(
                            canAddNewCourse = hasPermission,
                        )
                    }
                }
            }
        }

        val permissionFlow = activeRepo.coursePermissionDao()
            .personHasPermissionWithClazzTripleAsFlow(
                accountPersonUid = activeUserPersonUid,
                clazzUid = entityUidArg,
                firstPermission = PermissionFlags.COURSE_VIEW,
                secondPermission = PermissionFlags.COURSE_EDIT,
                thirdPermission = PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT,
            ).shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    permissionFlow.map {
                        it.firstPermission
                    }.distinctUntilChanged().collectLatest { hasViewPermission ->
                        if(hasViewPermission) {
                            launch {
                                activeRepo.courseBlockDao().findAllCourseBlockByClazzUidAsFlow(
                                    clazzUid = entityUidArg,
                                    includeInactive = false,
                                    includeHidden = false,
                                    hideUntilFilterTime = systemTimeInMillis(),
                                    accountPersonUid = activeUserPersonUid,
                                ).collect { courseBlockList ->
                                    _uiState.update { prev ->
                                        prev.copy(courseBlockList = courseBlockList)
                                    }
                                }
                            }

                            launch {
                                activeRepo.statementDao().findStatusForStudentsInClazzAsFlow(
                                    clazzUid = entityUidArg,
                                    studentPersonUids = listOf(activeUserPersonUid),
                                    accountPersonUid = activeUserPersonUid,
                                ).collect { blockStatuses ->
                                    _uiState.update { prev ->
                                        prev.copy(blockStatusesForActiveUser = blockStatuses)
                                    }
                                }
                            }
                            launch {
                                activeRepo.scheduleDao().findAllSchedulesByClazzUidAsLiveList(
                                    clazzUid = entityUidArg,
                                ).collect { scheduleList ->
                                    _uiState.update { prev ->
                                        prev.copy(scheduleList = scheduleList)
                                    }
                                }
                            }
                        }else{
                            _uiState.update { prev ->
                                prev.copy(courseBlockList = emptyList())
                            }
                        }
                    }
                }

                launch {
                    activeRepo.clazzDao().getClazzWithDisplayDetails(
                        clazzUid = entityUidArg,
                        currentTime = systemTimeInMillis(),
                        accountPersonUid = activeUserPersonUid,
                    ).combine(permissionFlow) { clazzWithDisplayDetails, permissions ->
                        clazzWithDisplayDetails.takeIf { permissions.firstPermission }
                    }.collect {
                        _uiState.update { prev ->
                            prev.copy(clazzAndDetail = it)
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
                            prev.copy(title = it?.clazz?.clazzName ?: "")
                        }
                    }
                }

                launch {
                    permissionFlow.map {
                        it.secondPermission
                    }.distinctUntilChanged().collect { hasEditPermission ->
                        _appUiState.update { prev ->
                            prev.copy(
                                fabState = prev.fabState.copy(visible = hasEditPermission)
                            )
                        }

                        _uiState.update { prev ->
                            prev.copy(
                                managePermissionVisible = hasEditPermission
                            )
                        }
                    }
                }

                launch {
                    permissionFlow.map {
                        it.thirdPermission
                    }.distinctUntilChanged().collect { canAddStudent ->
                        _uiState.update { prev ->
                            prev.copy(
                                clazzCodeVisible = canAddStudent,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onClickClazzCode(code: String) {
        setClipboardStringUseCase(code)
        snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.copied_to_clipboard)))
    }

    fun onClickCourseBlock(courseBlock: CourseBlock) {
        val navArgs = mapOf(
            ARG_ENTITY_UID to courseBlock.cbUid.toString(),
            ARG_CLAZZUID to entityUidArg.toString(),
        )

        when(courseBlock.cbType) {
            CourseBlock.BLOCK_MODULE_TYPE -> {
                _uiState.update { prev ->
                    prev.copy(
                        collapsedBlockUids = prev.collapsedBlockUids.toggle(courseBlock.cbUid)
                    )
                }
                _listRefreshCommandFlow.tryEmit(RefreshCommand())
            }
            CourseBlock.BLOCK_TEXT_TYPE -> {
                navController.navigate(TextBlockDetailViewModel.DEST_NAME, navArgs)
            }
            CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
                navController.navigate(ClazzAssignmentDetailViewModel.DEST_NAME, navArgs)
            }
            CourseBlock.BLOCK_DISCUSSION_TYPE -> {
                navController.navigate(CourseDiscussionDetailViewModel.DEST_NAME, navArgs)
            }
            CourseBlock.BLOCK_CONTENT_TYPE -> {
                navController.navigate(
                    viewName = ContentEntryDetailViewModel.DEST_NAME,
                    args = mapOf(
                        ARG_ENTITY_UID to courseBlock.cbEntityUid.toString(),
                        ARG_CLAZZUID to entityUidArg.toString(),
                        ARG_COURSE_BLOCK_UID to courseBlock.cbUid.toString(),
                    )
                )
            }
        }
    }

    private fun onClickEdit() {
        navController.navigate(ClazzEditViewModel.DEST_NAME,
            mapOf(UstadView.ARG_ENTITY_UID to entityUidArg.toString(),
                UstadView.CLAZZ_ACTION to ClazzAction.EDIT.name)
        )
    }

    fun onClickPermissions() {
        navController.navigate(
            CoursePermissionListViewModel.DEST_NAME,
            mapOf(ARG_CLAZZUID to entityUidArg.toString())
        )
    }

    fun onClickCopyCourse() {
        viewModelScope.launch {
            val originalClazz = _uiState.value.clazz ?: return@launch
            val originalCourseBlocks = _uiState.value.courseBlockList ?: emptyList()
            val originalSchedule = _uiState.value.scheduleList ?: emptyList()
            val originalCoursePicture = _uiState.value.clazzAndDetail?.coursePicture
            val copyResult = copyCourseUseCase(
                originalClazz,
                originalCourseBlocks,
                originalSchedule,
                originalCoursePicture
            ).also {
                it.clazz.clazzName = "${systemImpl.getString(MR.strings.copy_of)} ${it.clazz.clazzName}"
            }
            originalClazz.clazzName = "${systemImpl.getString(MR.strings.copy_of)} ${originalClazz.clazzName}"
            navController.navigate(
                ClazzEditViewModel.DEST_NAME,
                mapOf(
                    UstadView.CLAZZ_ACTION to ClazzAction.COPY.name,
                    UstadEditView.ARG_ENTITY_JSON to json.encodeToString(copyResult.clazz),
                    ClazzEditViewModel.STATE_KEY_COURSEBLOCKS to json.encodeToString(copyResult.courseBlocks),
                    STATE_KEY_SCHEDULES to json.encodeToString(copyResult.schedules),
                    ))

        }
    }

    companion object {
        const val DEST_NAME = "CourseDetailOverviewView"
    }

    enum class ClazzAction {
        EDIT,
        COPY
    }

}

