package com.ustadmobile.core.viewmodel.clazz.edit

import com.ustadmobile.core.controller.asCourseBlockWithEntity
import com.ustadmobile.core.db.dao.deactivateByUids
import com.ustadmobile.core.domain.courseblockupdate.AddOrUpdateCourseBlockUseCase
import com.ustadmobile.core.domain.courseblockupdate.UpdateCourseBlocksOnReorderOrCommitUseCase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.courseterminology.list.CourseTerminologyListViewModel
import com.ustadmobile.core.viewmodel.TimeZoneListViewModel
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.Clazz.Companion.CLAZZ_FEATURE_ATTENDANCE
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.lib.db.entities.ext.shallowCopyWithEntity
import com.ustadmobile.lib.util.getDefaultTimeZoneId
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import org.kodein.di.DI
import org.kodein.di.instance

@Serializable
data class ClazzEditUiState(

    val fieldsEnabled: Boolean = true,

    val entity: ClazzWithHolidayCalendarAndSchoolAndTerminology? = null,

    val clazzStartDateError: String? = null,

    val clazzEndDateError: String? = null,

    val clazzSchedules: List<Schedule> = emptyList(),

    val courseBlockList: List<CourseBlockWithEntity> = emptyList(),

    val timeZone: String = "UTC"

) {

    class CourseBlockUiState internal constructor(
        val courseBlock: CourseBlockWithEntity
    ) {
        val showIndent: Boolean
            get() = courseBlock.cbType != CourseBlock.BLOCK_MODULE_TYPE && courseBlock.cbIndentLevel < BLOCK_MAX_INDENT

        val showUnindent: Boolean
            get() = courseBlock.cbIndentLevel > 0

        val showHide: Boolean
            get() = !courseBlock.cbHidden

        val showUnhide: Boolean
            get() = courseBlock.cbHidden
    }


    val clazzEditAttendanceChecked: Boolean
        get() = entity?.clazzFeatures == CLAZZ_FEATURE_ATTENDANCE
                && CLAZZ_FEATURE_ATTENDANCE == CLAZZ_FEATURE_ATTENDANCE

    fun courseBlockStateFor(couresBlockWithEntity: CourseBlockWithEntity): CourseBlockUiState {
        return CourseBlockUiState(couresBlockWithEntity)
    }

    companion object {

        const val BLOCK_MAX_INDENT = 3

    }

}

class ClazzEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    private val addOrUpdateCourseBlockUseCase: AddOrUpdateCourseBlockUseCase =
        AddOrUpdateCourseBlockUseCase(),
    private val updateCourseBlocksOnReorderOrCommitUseCase: UpdateCourseBlocksOnReorderOrCommitUseCase =
        UpdateCourseBlocksOnReorderOrCommitUseCase(),
): UstadEditViewModel(di, savedStateHandle, ClazzEdit2View.VIEW_NAME) {

    private val _uiState = MutableStateFlow(ClazzEditUiState())

    val uiState: Flow<ClazzEditUiState> = _uiState.asStateFlow()

    init {
        val title = createEditTitle(MessageID.add_a_new_course, MessageID.edit_course)
        _appUiState.update {
            AppUiState(
                title = title,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.save),
                    onClick = this::onClickSave
                ),
                loadingState = LoadingUiState.INDETERMINATE,
                hideBottomNavigation = true,
            )
        }

        _uiState.update { prev -> prev.copy(fieldsEnabled = false) }

        viewModelScope.launch {
            awaitAll(
                async {
                    loadEntity(
                        serializer = ClazzWithHolidayCalendarAndSchoolAndTerminology.serializer(),
                        onLoadFromDb = {
                            it.clazzDao.takeIf { entityUidArg != 0L }
                                ?.findByUidWithHolidayCalendarAsync(entityUidArg)
                        },
                        makeDefault = {
                            ClazzWithHolidayCalendarAndSchoolAndTerminology().apply {
                                clazzUid = activeDb.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)
                                clazzName = ""
                                isClazzActive = true
                                clazzStartTime = systemTimeInMillis()
                                clazzTimeZone = getDefaultTimeZoneId()
                                clazzSchoolUid = savedStateHandle[UstadView.ARG_SCHOOL_UID]?.toLong() ?: 0L
                                school = activeRepo.schoolDao.takeIf { clazzSchoolUid != 0L }
                                    ?.findByUidAsync(clazzSchoolUid)
                                terminology = activeRepo.courseTerminologyDao
                                    .takeIf { clazzTerminologyUid != 0L }
                                    ?.findByUidAsync(clazzTerminologyUid)
                            }
                        },
                        uiUpdate = {
                            _uiState.update { prev ->
                                prev.copy(
                                    entity = it
                                )
                            }
                        }
                    )
                },
                async {
                    loadEntity(
                        serializer = ListSerializer(Schedule.serializer()),
                        loadFromStateKeys = listOf(STATE_KEY_SCHEDULES),
                        onLoadFromDb = {
                            it.scheduleDao.takeIf { entityUidArg != 0L }
                                ?.findAllSchedulesByClazzUidAsync(entityUidArg)
                        },
                        makeDefault = {
                            emptyList()
                        },
                        uiUpdate = {
                            _uiState.update { prev ->
                                prev.copy(clazzSchedules =  it ?: emptyList())
                            }
                        }
                    )
                },
                async {
                    loadEntity(
                        serializer = ListSerializer(CourseBlockWithEntity.serializer()),
                        loadFromStateKeys = listOf(STATE_KEY_COURSEBLOCKS),
                        onLoadFromDb = { db ->
                            val courseBlocksDb = db.courseBlockDao.takeIf { entityUidArg != 0L }
                                ?.findAllCourseBlockByClazzUidAsync(entityUidArg) ?: emptyList()

                            val assignmentPeerAllocations = db.peerReviewerAllocationDao
                                .takeIf { entityUidArg != 0L }?.getAllPeerReviewerAllocations(
                                    courseBlocksDb
                                        .filter { block -> block.assignment != null }
                                        .map { assignmentBlock -> assignmentBlock.assignment?.caUid ?: 0 }
                                ) ?: emptyList()

                            courseBlocksDb.map {
                                it.asCourseBlockWithEntity(emptyList(), assignmentPeerAllocations)
                            }
                        },
                        makeDefault = {
                            emptyList()
                        },
                        uiUpdate = {
                            _uiState.update { prev ->
                                prev.copy(courseBlockList = it ?: emptyList())
                            }
                        }
                    )
                }
            )

            if(savedStateHandle[KEY_INIT_STATE] == null) {
                savedStateHandle[KEY_INIT_STATE] = withContext(Dispatchers.Default) {
                    json.encodeToString(_uiState.value)
                }
            }

            launch {
                resultReturner.filteredResultFlowForKey(RESULT_KEY_SCHEDULE).collect { result ->
                    val returnedSchedule = result.result as? Schedule ?: return@collect
                    val newSchedules = _uiState.value.clazzSchedules.replaceOrAppend(returnedSchedule) {
                        it.scheduleUid == returnedSchedule.scheduleUid
                    }

                    _uiState.update { prev ->
                        prev.copy(
                            clazzSchedules = newSchedules
                        )
                    }

                    savedStateHandle[STATE_KEY_SCHEDULES] = withContext(Dispatchers.Default) {
                        json.encodeToString(ListSerializer(Schedule.serializer()), newSchedules)
                    }
                }
            }

            launch {
                resultReturner.filteredResultFlowForKey(RESULT_KEY_COURSEBLOCK).collect { result ->
                    val courseBlock = result.result as? CourseBlock ?: return@collect
                    val courseBlockWithEntity = result.result as? CourseBlockWithEntity
                    val assignment = courseBlockWithEntity?.assignment
                    val peerReviewerAllocations = courseBlockWithEntity?.assignmentPeerAllocations

                    val newCourseBlockList = addOrUpdateCourseBlockUseCase(
                        currentList = _uiState.value.courseBlockList,
                        clazzUid = _uiState.value.entity?.clazzUid
                            ?: throw IllegalStateException("Clazz must not be null when collecting course block"),
                        addOrUpdateBlock = courseBlock,
                        assignment = assignment,
                        assignmentPeerReviewAllocations = peerReviewerAllocations,
                    )

                    updateCourseBlockList(newCourseBlockList)
                }
            }

            launch {
                resultReturner.filteredResultFlowForKey(RESULT_KEY_TIMEZONE).collect { result ->
                    val timeZoneId = result.result as? String ?: return@collect
                    onEntityChanged(_uiState.value.entity?.shallowCopy {
                        clazzTimeZone = timeZoneId
                    })
                }
            }

            launch {
                resultReturner.filteredResultFlowForKey(RESULT_KEY_TERMINOLOGY).collect { result ->
                    val newTerminology = result.result as? CourseTerminology ?: return@collect
                    onEntityChanged(_uiState.value.entity?.shallowCopy {
                        clazzTerminologyUid = newTerminology.ctUid
                        terminology = newTerminology
                    })
                }
            }

            launch {
                resultReturner.filteredResultFlowForKey(RESULT_KEY_HTML_DESC).collect { result ->
                    val newDescription = result.result as? String ?: return@collect
                    onEntityChanged(_uiState.value.entity?.shallowCopy {
                        clazzDesc = newDescription
                    })
                }
            }

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }

            loadingState = LoadingUiState.NOT_LOADING
        }

    }

    private suspend fun updateCourseBlockList(
        newCourseBlockList: List<CourseBlockWithEntity>
    ) {
        _uiState.update { prev ->
            prev.copy(
                courseBlockList = newCourseBlockList
            )
        }

        savedStateHandle[STATE_KEY_COURSEBLOCKS] = withContext(Dispatchers.Default) {
            json.encodeToString(ListSerializer(CourseBlockWithEntity.serializer()),
                newCourseBlockList)
        }
    }

    fun onEntityChanged(entity: ClazzWithHolidayCalendarAndSchoolAndTerminology?) {
        _uiState.update { prev ->
            prev.copy(
                entity = entity,
                clazzEndDateError = updateErrorMessageOnChange(prev.entity?.clazzEndTime,
                    entity?.clazzEndTime, prev.clazzEndDateError),
                clazzStartDateError = updateErrorMessageOnChange(prev.entity?.clazzStartTime,
                    entity?.clazzStartTime, prev.clazzStartDateError)
            )
        }

        scheduleEntityCommitToSavedState(
            entity = entity,
            serializer = ClazzWithHolidayCalendarAndSchoolAndTerminology.serializer(),
            commitDelay = 200,
        )
    }

    fun onCheckedAttendanceChanged(checked: Boolean) {
        onEntityChanged(_uiState.value.entity?.shallowCopy {
            clazzFeatures = if(checked) {
                CLAZZ_FEATURE_ATTENDANCE
            }else {
                0L
            }
        })
    }

    fun onClickAddSchedule(){
        navigateForResult(ScheduleEditView.VIEW_NAME, "Schedule", currentValue = null,
            serializer = Schedule.serializer())
    }

    fun onClickEditSchedule(schedule: Schedule) {
        navigateForResult(
            nextViewName = ScheduleEditView.VIEW_NAME,
            key = RESULT_KEY_SCHEDULE,
            currentValue = schedule,
            serializer = Schedule.serializer()
        )
    }

    fun onClickDeleteSchedule(schedule: Schedule) {
        val newSchedules = _uiState.value.clazzSchedules
            .filter { it.scheduleUid != schedule.scheduleUid }
        savedStateHandle[STATE_KEY_SCHEDULES] = json.encodeToString(
            ListSerializer(Schedule.serializer()), newSchedules)
        _uiState.update { prev ->
            prev.copy(
                clazzSchedules = newSchedules
            )
        }
    }

    fun onClickEditDescription() {
        navigateToEditHtml(
            currentValue = _uiState.value.entity?.clazzDesc,
            resultKey = RESULT_KEY_HTML_DESC
        )
    }


    fun onAddCourseBlock(blockType: Int) {
        val (viewName, keyName) = when(blockType) {
            CourseBlock.BLOCK_DISCUSSION_TYPE,
            CourseBlock.BLOCK_TEXT_TYPE,
            CourseBlock.BLOCK_MODULE_TYPE ->
                CourseBlockEditViewModel.DEST_NAME to RESULT_KEY_COURSEBLOCK
            CourseBlock.BLOCK_ASSIGNMENT_TYPE ->
                ClazzAssignmentEditView.VIEW_NAME to RESULT_KEY_COURSEBLOCK
            else -> return
        }

        navigateForResult(
            nextViewName = viewName,
            key = keyName,
            currentValue = null,
            args = mapOf(CourseBlockEditViewModel.ARG_BLOCK_TYPE to blockType.toString()),
            serializer = CourseBlockWithEntity.serializer(),
        )
    }

    private fun ClazzEditUiState.hasErrors() : Boolean {
        return clazzStartDateError != null || clazzEndDateError != null
    }

    fun onClickSave() {
        val initEntity = _uiState.value.entity ?: return

        if (initEntity.clazzStartTime == 0L) {
            _uiState.update { prev ->
                prev.copy(
                    clazzStartDateError = systemImpl.getString(MessageID.field_required_prompt)
                )
            }
        }

        if(initEntity.clazzEndTime <= initEntity.clazzStartTime) {
            _uiState.update { prev ->
                prev.copy(clazzEndDateError = systemImpl.getString(MessageID.end_is_before_start))
            }
        }

        if(_uiState.value.hasErrors())
            return

        loadingState = LoadingUiState.INDETERMINATE

        //Entity to save
        val entity = initEntity.shallowCopy {
            this.clazzName = clazzName?.trim()
            clazzStartTime = Instant.fromEpochMilliseconds(initEntity.clazzStartTime)
                .toLocalMidnight(initEntity.effectiveTimeZone).toEpochMilliseconds()

            if(clazzEndTime != Long.MAX_VALUE){
                clazzEndTime = Instant.fromEpochMilliseconds(initEntity.clazzEndTime)
                    .toLocalEndOfDay(initEntity.effectiveTimeZone).toEpochMilliseconds()
            }
        }

        _uiState.update { prev -> prev.copy(fieldsEnabled = false, entity = entity) }

        viewModelScope.launch {
            val initState = savedStateHandle.getJson(KEY_INIT_STATE, ClazzEditUiState.serializer())
                ?: return@launch

            activeDb.withDoorTransactionAsync {
                if(entityUidArg == 0L) {
                    val termMap = activeDb.courseTerminologyDao.findByUidAsync(initEntity.clazzTerminologyUid)
                        .toTermMap(json, systemImpl)
                    activeDb.createNewClazzAndGroups(initEntity, systemImpl, termMap)
                }else {
                    activeDb.clazzDao.updateAsync(initEntity)
                }

                val clazzUid = entity.clazzUid

                val schedulesToCommit = _uiState.value.clazzSchedules.map {
                    it.shallowCopy { scheduleClazzUid = clazzUid }
                }

                activeDb.scheduleDao.upsertListAsync(schedulesToCommit)
                activeDb.scheduleDao.deactivateByUids(
                    initState.clazzSchedules.findKeysNotInOtherList(schedulesToCommit) {
                        it.scheduleUid
                    }, systemTimeInMillis()
                )

                val courseBlockModulesToCommit = updateCourseBlocksOnReorderOrCommitUseCase(
                    _uiState.value.courseBlockList)
                activeDb.courseBlockDao.upsertListAsync(courseBlockModulesToCommit)
                activeDb.courseBlockDao.deactivateByUids(
                    initState.courseBlockList.findKeysNotInOtherList(courseBlockModulesToCommit) {
                        it.cbUid
                    }, systemTimeInMillis()
                )

                val assignmentsToUpsert = _uiState.value.courseBlockList.mapNotNull { it.assignment }
                activeDb.clazzAssignmentDao.upsertListAsync(assignmentsToUpsert)
                val assignmentsToDeactivate = initState.courseBlockList.mapNotNull { it.assignment}
                    .findKeysNotInOtherList(assignmentsToUpsert) { it.caUid }
                activeDb.clazzAssignmentDao.takeIf { assignmentsToDeactivate.isNotEmpty() }
                    ?.updateActiveByList(assignmentsToDeactivate, false, systemTimeInMillis())
            }

            val entityTimeZone = TimeZone.of(entity.effectiveTimeZone)
            val fromLocalDate = Clock.System.now().toLocalDateTime(entityTimeZone)
                .toLocalMidnight()
            val clazzLogCreatorManager: ClazzLogCreatorManager by di.instance()
            clazzLogCreatorManager.requestClazzLogCreation(
                entity.clazzUid,
                accountManager.activeAccount.endpointUrl,
                fromLocalDate.toInstant(entityTimeZone).toEpochMilliseconds(),
                fromLocalDate.toLocalEndOfDay().toInstant(entityTimeZone).toEpochMilliseconds()
            )
            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }
            loadingState = LoadingUiState.NOT_LOADING

            finishWithResult(ClazzDetailView.VIEW_NAME, entity.clazzUid, entity)
        }
    }


    fun onCourseBlockMoved(from: Int, to: Int) {
        val reorderedList = _uiState.value.courseBlockList.toMutableList().apply {
            add(to, removeAt(from))
        }.toList()
        val newList = updateCourseBlocksOnReorderOrCommitUseCase(reorderedList, to)

        viewModelScope.launch {
            updateCourseBlockList(newList)
        }
    }

    fun onClickTimezone() {
        navController.navigate(
            viewName = TimeZoneListViewModel.DEST_NAME,
            args = mapOf(
                UstadView.ARG_RESULT_DEST_KEY to RESULT_KEY_TIMEZONE,
                UstadView.ARG_RESULT_DEST_VIEWNAME to destinationName,
            )
        )
    }

    fun onClickHolidayCalendar() {

    }

    fun onClickTerminology() {
        navigateForResult(
            nextViewName = CourseTerminologyListViewModel.DEST_NAME,
            key = RESULT_KEY_TERMINOLOGY,
            currentValue = null,
            serializer = CourseTerminology.serializer(),
        )
    }

    private fun updateCourseBlock(updatedBlock: CourseBlockWithEntity){
        viewModelScope.launch {
            updateCourseBlockList(_uiState.value.courseBlockList.replace(updatedBlock) {
                it.cbUid == updatedBlock.cbUid
            })
        }
    }

    fun onClickHideBlockPopupMenu(block: CourseBlockWithEntity) {
        updateCourseBlock(block.shallowCopyWithEntity {
            cbHidden = true
        })
    }

    fun onClickUnHideBlockPopupMenu(block: CourseBlockWithEntity) {
        updateCourseBlock(block.shallowCopyWithEntity {
            cbHidden = false
        })
    }

    fun onClickIndentBlockPopupMenu(block: CourseBlockWithEntity) {
        updateCourseBlock(block.shallowCopyWithEntity {
            cbIndentLevel = block.cbIndentLevel + 1
        })
    }

    fun onClickUnIndentBlockPopupMenu(block: CourseBlockWithEntity) {
        updateCourseBlock(block.shallowCopyWithEntity {
            cbIndentLevel = block.cbIndentLevel - 1
        })
    }

    fun onClickDeleteCourseBlock(block: CourseBlockWithEntity) {
        viewModelScope.launch {
            updateCourseBlockList(_uiState.value.courseBlockList.filter {
                it.cbUid != block.cbUid
            })
        }
    }

    fun onClickEditCourseBlock(block: CourseBlockWithEntity) {
        when(block.cbType) {
            CourseBlock.BLOCK_DISCUSSION_TYPE,
            CourseBlock.BLOCK_TEXT_TYPE,
            CourseBlock.BLOCK_MODULE_TYPE -> {
                navigateForResult(
                    nextViewName = CourseBlockEditViewModel.DEST_NAME,
                    key = RESULT_KEY_COURSEBLOCK,
                    serializer = CourseBlock.serializer(),
                    currentValue = block
                )
            }
            CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
                navigateForResult(
                    nextViewName = ClazzAssignmentEditView.VIEW_NAME,
                    key = RESULT_KEY_COURSEBLOCK,
                    serializer = CourseBlockWithEntity.serializer(),
                    currentValue = block,
                )
            }
        }
    }

    companion object {

        const val RESULT_KEY_SCHEDULE = "Schedule"

        const val STATE_KEY_SCHEDULES = "schedule"

        const val RESULT_KEY_COURSEBLOCK = "courseblock"

        const val RESULT_KEY_TIMEZONE = "timeZone"

        const val RESULT_KEY_TERMINOLOGY = "terminology"

        const val STATE_KEY_COURSEBLOCKS = "courseblocks"

    }

}