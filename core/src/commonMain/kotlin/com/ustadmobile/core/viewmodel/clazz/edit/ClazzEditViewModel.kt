package com.ustadmobile.core.viewmodel.clazz.edit

import com.ustadmobile.core.db.dao.deactivateByUids
import com.ustadmobile.core.domain.courseblockupdate.AddOrUpdateCourseBlockUseCase
import com.ustadmobile.core.domain.courseblockupdate.UpdateCourseBlocksOnReorderOrCommitUseCase
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.core.domain.contententry.save.SaveContentEntryUseCase
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.courseterminology.list.CourseTerminologyListViewModel
import com.ustadmobile.core.viewmodel.timezone.TimeZoneListViewModel
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.edit.ClazzAssignmentEditViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.edit.ClazzAssignmentEditViewModel.Companion.ARG_TERMINOLOGY
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditViewModel
import com.ustadmobile.core.viewmodel.schedule.edit.ScheduleEditViewModel
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.Clazz.Companion.CLAZZ_FEATURE_ATTENDANCE
import com.ustadmobile.lib.db.entities.ext.shallowCopy
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
import io.github.aakira.napier.Napier
import org.kodein.di.direct
import org.kodein.di.instanceOrNull

@Serializable
data class ClazzEditUiState(

    val fieldsEnabled: Boolean = false,

    val entity: ClazzWithHolidayCalendarAndAndTerminology? = null,

    val clazzStartDateError: String? = null,

    val clazzEndDateError: String? = null,

    val clazzNameError: String? = null,

    val clazzSchedules: List<Schedule> = emptyList(),

    val courseBlockList: List<CourseBlockAndEditEntities> = emptyList(),

    val timeZone: String = "UTC"

) {

    class CourseBlockUiState internal constructor(
        val block: CourseBlockAndEditEntities
    ) {
        val showIndent: Boolean
            get() = block.courseBlock.cbType != CourseBlock.BLOCK_MODULE_TYPE
                    && block.courseBlock.cbIndentLevel < BLOCK_MAX_INDENT

        val showUnindent: Boolean
            get() = block.courseBlock.cbIndentLevel > 0

        val showHide: Boolean
            get() = !block.courseBlock.cbHidden

        val showUnhide: Boolean
            get() = block.courseBlock.cbHidden
    }


    val clazzEditAttendanceChecked: Boolean
        get() = entity?.clazzFeatures == CLAZZ_FEATURE_ATTENDANCE
                && CLAZZ_FEATURE_ATTENDANCE == CLAZZ_FEATURE_ATTENDANCE

    fun courseBlockStateFor(courseBlockAndEditEntities: CourseBlockAndEditEntities): CourseBlockUiState {
        return CourseBlockUiState(courseBlockAndEditEntities)
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
    private val saveContentEntryUseCase: SaveContentEntryUseCase = SaveContentEntryUseCase(
        db = di.onActiveEndpoint().direct.instance(tag = DoorTag.TAG_DB),
        repo = di.onActiveEndpoint().direct.instanceOrNull(tag = DoorTag.TAG_REPO),
        enqueueSavePictureUseCase = di.onActiveEndpoint().direct.instance(),
    ),
    private val importContentUseCase: EnqueueContentEntryImportUseCase = di.onActiveEndpoint().direct.instance(),
    private val enqueueSavePictureUseCase: EnqueueSavePictureUseCase = di.onActiveEndpoint().direct
        .instance(),
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ClazzEditUiState())

    val uiState: Flow<ClazzEditUiState> = _uiState.asStateFlow()

    /**
     * The clazz uid (whether it is an existing UID or new UID needs to be passed to other screens
     * e.g. ClazzAssignmentEdit etc. This needs to be available immediately to avoid issues with
     * automated testing.
     */
    private val effectiveClazzUid = savedStateHandle[ARG_ENTITY_UID]?.toLong()
        ?: activeDb.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)

    private val createNewClazzUseCase: CreateNewClazzUseCase by di.onActiveEndpoint().instance()

    init {
        val title = createEditTitle(MR.strings.add_a_new_course, MR.strings.edit_course)
        _appUiState.update {
            AppUiState(
                title = title,
                loadingState = LoadingUiState.INDETERMINATE,
                hideBottomNavigation = true,
            )
        }

        launchIfHasPermission(
            permissionCheck = {
                if(entityUidArg != 0L) {
                    it.coursePermissionDao().personHasPermissionWithClazzAsync2(
                        activeUserPersonUid, entityUidArg, PermissionFlags.COURSE_EDIT
                    )
                }else {
                    it.systemPermissionDao().personHasSystemPermission(
                        activeUserPersonUid, PermissionFlags.ADD_COURSE
                    )
                }
            }
        ) {
            awaitAll(
                async {
                    loadEntity(
                        serializer = ClazzWithHolidayCalendarAndAndTerminology.serializer(),
                        onLoadFromDb = {
                            it.clazzDao().takeIf { entityUidArg != 0L }
                                ?.findByUidWithHolidayCalendarAsync(entityUidArg).let { dbResult ->
                                    val hasPicture = dbResult?.coursePicture != null
                                    //Add CoursePicture entity if not already present
                                    if(dbResult == null || hasPicture){
                                        dbResult
                                    }else {
                                        dbResult.shallowCopy {
                                            coursePicture = CoursePicture(
                                                coursePictureUid = entityUidArg
                                            )
                                        }
                                    }
                                }
                        },
                        makeDefault = {
                            ClazzWithHolidayCalendarAndAndTerminology().apply {
                                clazzUid = effectiveClazzUid
                                clazzName = ""
                                isClazzActive = true
                                clazzStartTime = systemTimeInMillis()
                                clazzTimeZone = getDefaultTimeZoneId()
                                clazzSchoolUid = savedStateHandle[UstadView.ARG_SCHOOL_UID]?.toLong() ?: 0L
                                terminology = activeRepoWithFallback.courseTerminologyDao()
                                    .takeIf { clazzTerminologyUid != 0L }
                                    ?.findByUidAsync(clazzTerminologyUid)
                                coursePicture = CoursePicture(
                                    coursePictureUid = effectiveClazzUid
                                )
                                clazzOwnerPersonUid = activeUserPersonUid
                            }
                        },
                        uiUpdate = {
                            _uiState.update { prev ->
                                prev.copy(
                                    entity = it
                                )
                            }
                        }
                    ).also {
                        savedStateHandle.setIfNoValueSetYet(INIT_PIC_URI,
                            it?.coursePicture?.coursePictureUri ?: "")
                    }
                },
                async {
                    loadEntity(
                        serializer = ListSerializer(Schedule.serializer()),
                        loadFromStateKeys = listOf(STATE_KEY_SCHEDULES),
                        onLoadFromDb = {
                            it.scheduleDao().takeIf { entityUidArg != 0L }
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
                        serializer = ListSerializer(CourseBlockAndEditEntities.serializer()),
                        loadFromStateKeys = listOf(STATE_KEY_COURSEBLOCKS),
                        onLoadFromDb = { db ->
                            val courseBlocksDb = db.courseBlockDao()
                                .takeIf { entityUidArg != 0L }
                                ?.findAllCourseBlockByClazzUidAsync(entityUidArg, false)
                                ?: emptyList()
                            val assignmentPeerAllocations = db.peerReviewerAllocationDao()
                                .takeIf { entityUidArg != 0L }?.getAllPeerReviewerAllocationsByClazzUid(
                                    clazzUid = entityUidArg,
                                    includeInactive = false
                                ) ?: emptyList()

                            courseBlocksDb.map {
                                CourseBlockAndEditEntities(
                                    courseBlock = it.courseBlock!!, //CourseBlock can't be null as per query
                                    courseBlockPicture = it.courseBlockPicture ?: CourseBlockPicture(
                                        cbpUid = it.courseBlock!!.cbUid
                                    ),
                                    contentEntry = it.contentEntry,
                                    contentEntryLang = it.contentEntryLang,
                                    assignment = it.assignment,
                                    assignmentCourseGroupSetName = it.assignmentCourseGroupSetName,
                                    assignmentPeerAllocations = assignmentPeerAllocations.filter { allocation ->
                                        allocation.praAssignmentUid == it.assignment?.caUid
                                    }
                                )
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
                //Handle text, module, and discussion topic (e.g. plain CourseBlock that does not
                // include any other entities)
                resultReturner.filteredResultFlowForKey(RESULT_KEY_COURSEBLOCK).collect { result ->
                    val courseBlockResult = result.result as? CourseBlockAndEditEntities
                        ?: return@collect

                    val newCourseBlockList = addOrUpdateCourseBlockUseCase(
                        currentList = _uiState.value.courseBlockList,
                        clazzUid = _uiState.value.entity?.clazzUid ?: 0L,
                        addOrUpdateBlock = courseBlockResult,
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
                resultReturner.filteredResultFlowForKey(RESULT_KEY_DESCRIPTION).collect { result ->
                    val newDescription = result.result as? String ?: return@collect
                    onEntityChanged(_uiState.value.entity?.shallowCopy {
                        clazzDesc = newDescription
                    })
                }
            }

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }

            _appUiState.update { prev ->
                prev.copy(
                    loadingState = LoadingUiState.NOT_LOADING,
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = systemImpl.getString(MR.strings.save),
                        onClick = this@ClazzEditViewModel::onClickSave
                    ),
                )
            }
        }
    }

    private suspend fun updateCourseBlockList(
        newCourseBlockList: List<CourseBlockAndEditEntities>
    ) {
        _uiState.update { prev ->
            prev.copy(
                courseBlockList = newCourseBlockList
            )
        }

        savedStateHandle[STATE_KEY_COURSEBLOCKS] = withContext(Dispatchers.Default) {
            json.encodeToString(ListSerializer(CourseBlockAndEditEntities.serializer()),
                newCourseBlockList)
        }
    }

    fun onEntityChanged(entity: ClazzWithHolidayCalendarAndAndTerminology?) {
        _uiState.update { prev ->
            prev.copy(
                entity = entity,
                clazzEndDateError = updateErrorMessageOnChange(prev.entity?.clazzEndTime,
                    entity?.clazzEndTime, prev.clazzEndDateError),
                clazzStartDateError = updateErrorMessageOnChange(prev.entity?.clazzStartTime,
                    entity?.clazzStartTime, prev.clazzStartDateError),
                clazzNameError = updateErrorMessageOnChange(prev.entity?.clazzName, entity?.clazzName, prev.clazzNameError)
            )
        }

        scheduleEntityCommitToSavedState(
            entity = entity,
            serializer = ClazzWithHolidayCalendarAndAndTerminology.serializer(),
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
        navigateForResult(ScheduleEditViewModel.DEST_NAME, "Schedule", currentValue = null,
            serializer = Schedule.serializer())
    }

    fun onClickEditSchedule(schedule: Schedule) {
        navigateForResult(
            nextViewName = ScheduleEditViewModel.DEST_NAME,
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
            resultKey = RESULT_KEY_DESCRIPTION,
            title = systemImpl.getString(MR.strings.description)
        )
    }


    fun onAddCourseBlock(blockType: Int) {
        if(blockType == CourseBlock.BLOCK_CONTENT_TYPE) {
            navigateForResult(
                nextViewName = ContentEntryListViewModel.DEST_NAME,
                key = RESULT_KEY_COURSEBLOCK,
                currentValue = null,
                serializer = CourseBlockAndEditEntities.serializer(),
                args = mapOf(
                    UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString(),
                    UstadView.ARG_CLAZZUID to effectiveClazzUid.toString(),
                    CourseBlockEditViewModel.ARG_BLOCK_TYPE to CourseBlock.BLOCK_CONTENT_TYPE.toString(),
                    ContentEntryEditViewModel.ARG_GO_TO_ON_CONTENT_ENTRY_DONE to ContentEntryEditViewModel.GO_TO_COURSE_BLOCK_EDIT.toString(),
                ),
            )

            return
        }


        val (viewName, keyName) = when(blockType) {
            CourseBlock.BLOCK_DISCUSSION_TYPE,
            CourseBlock.BLOCK_TEXT_TYPE,
            CourseBlock.BLOCK_MODULE_TYPE ->
                CourseBlockEditViewModel.DEST_NAME to RESULT_KEY_COURSEBLOCK
            CourseBlock.BLOCK_ASSIGNMENT_TYPE ->
                ClazzAssignmentEditViewModel.DEST_NAME to RESULT_KEY_COURSEBLOCK
            else -> return
        }

        navigateForResult(
            nextViewName = viewName,
            key = keyName,
            currentValue = null,
            args = buildMap {
                put(CourseBlockEditViewModel.ARG_BLOCK_TYPE, blockType.toString())
                put(UstadView.ARG_CLAZZUID, effectiveClazzUid.toString())
                if(blockType == CourseBlock.BLOCK_ASSIGNMENT_TYPE) {
                    //Terminology is required by AssignmentEdit (for marking type)
                    _uiState.value.entity?.terminology?.also { terminology ->
                        put(ARG_TERMINOLOGY,
                            json.encodeToString(CourseTerminology.serializer(), terminology)
                        )
                    }
                }

            },
            serializer = CourseBlockAndEditEntities.serializer(),
        )
    }

    private fun ClazzEditUiState.hasErrors() : Boolean {
        return clazzStartDateError != null || clazzEndDateError != null || clazzNameError != null
    }

    fun onClickSave() {
        val initEntity = _uiState.value.entity ?: return
        if(loadingState == LoadingUiState.INDETERMINATE) {
            Napier.d("onClickSave: indeterminate")
            return
        }

        if (initEntity.clazzStartTime == 0L) {
            Napier.d("onClickSave: clazzstarttime = 0")
            _uiState.update { prev ->
                prev.copy(
                    clazzStartDateError = systemImpl.getString(MR.strings.field_required_prompt)
                )
            }
        }

        if(initEntity.clazzEndTime <= initEntity.clazzStartTime) {
            Napier.d("onClickSave: endbeforestart")
            _uiState.update { prev ->
                prev.copy(clazzEndDateError = systemImpl.getString(MR.strings.end_is_before_start))
            }
        }

        if(initEntity.clazzName.isNullOrBlank()) {
            _uiState.update { prev ->
                prev.copy(
                    clazzNameError = systemImpl.getString(MR.strings.required)
                )
            }
        }

        if(_uiState.value.hasErrors()) {
            Napier.d("onClickSave: hasErrors")
            return
        }

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

        _uiState.update { prev -> prev.copy(entity = entity) }
        launchWithLoadingIndicator(
            onSetFieldsEnabled = { _uiState.update { prev -> prev.copy(fieldsEnabled = it) } }
        ) {
            val initState = savedStateHandle.getJson(KEY_INIT_STATE, ClazzEditUiState.serializer())
                ?: return@launchWithLoadingIndicator

            Napier.d("onClickSave: start transaction")
            val courseBlockListVal = _uiState.value.courseBlockList
            val coursePictureVal = entity.coursePicture
            val updateImage = coursePictureVal != null &&
                    savedStateHandle[INIT_PIC_URI] != (entity.coursePicture?.coursePictureUri ?: "")

            val updatedCourseBlockPictures = courseBlockListVal.mapNotNull { block ->
                val imageUriNow = block.courseBlockPicture?.cbpPictureUri
                val initImageUri = initState.courseBlockList.firstOrNull {
                    it.courseBlockPicture?.cbpUid == block.courseBlockPicture?.cbpUid
                }?.courseBlockPicture?.cbpPictureUri

                block.courseBlockPicture?.takeIf { imageUriNow != initImageUri }
            }

            activeDb.withDoorTransactionAsync {
                if(entityUidArg == 0L) {
                    createNewClazzUseCase(initEntity)
                }else {
                    activeRepoWithFallback.clazzDao().updateAsync(initEntity)
                }

                if(updateImage && coursePictureVal != null) {
                    coursePictureVal.coursePictureLct = systemTimeInMillis()
                    activeDb.coursePictureDao().upsertAsync(coursePictureVal)
                }

                val clazzUid = entity.clazzUid

                val schedulesToCommit = _uiState.value.clazzSchedules.map {
                    it.shallowCopy { scheduleClazzUid = clazzUid }
                }

                activeRepoWithFallback.scheduleDao().upsertListAsync(schedulesToCommit)
                activeRepoWithFallback.scheduleDao().deactivateByUids(
                    initState.clazzSchedules.findKeysNotInOtherList(schedulesToCommit) {
                        it.scheduleUid
                    }, systemTimeInMillis()
                )

                val courseBlockModulesToCommit = updateCourseBlocksOnReorderOrCommitUseCase(
                    courseBlockListVal)
                activeRepoWithFallback.courseBlockDao().upsertListAsync(
                    courseBlockModulesToCommit.map { it.courseBlock }
                )
                activeRepoWithFallback.courseBlockDao().deactivateByUids(
                    initState.courseBlockList.findKeysNotInOtherList(courseBlockModulesToCommit) {
                        it.courseBlock.cbUid
                    }, systemTimeInMillis()
                )

                val assignmentsToUpsert = courseBlockListVal.mapNotNull { it.assignment }
                activeRepoWithFallback.clazzAssignmentDao().upsertListAsync(assignmentsToUpsert)
                val assignmentsToDeactivate = initState.courseBlockList.mapNotNull { it.assignment}
                    .findKeysNotInOtherList(assignmentsToUpsert) { it.caUid }
                activeRepoWithFallback.clazzAssignmentDao().takeIf { assignmentsToDeactivate.isNotEmpty() }
                    ?.updateActiveByList(assignmentsToDeactivate, false, systemTimeInMillis())

                val currentPeerReviewAllocations = courseBlockListVal.flatMap {
                    it.assignmentPeerAllocations
                }
                val prevPeerReviewerAllocations = initState.courseBlockList.flatMap {
                    it.assignmentPeerAllocations
                }

                activeRepoWithFallback.peerReviewerAllocationDao().deactivateByUids(
                    uidList = prevPeerReviewerAllocations.findKeysNotInOtherList(
                        otherList = currentPeerReviewAllocations,
                        key = { it.praUid }
                    ),
                    changeTime = systemTimeInMillis()
                )
                activeRepoWithFallback.peerReviewerAllocationDao().upsertList(currentPeerReviewAllocations)

                //Run the ContentImport for any jobs where this is required.
                courseBlockListVal.mapNotNull {
                    it.contentJobItem
                }.forEach {
                    importContentUseCase.invoke(
                        contentJobItem = it
                    )
                }

                activeDb.courseBlockPictureDao()
                    .takeIf { updatedCourseBlockPictures.isNotEmpty() }
                    ?.upsertListAsync(updatedCourseBlockPictures)
                Napier.d("onClickSave: transaction block done")
            }
            Napier.d("onClickSave: transaction done")

            //Saving the ContentEntry entity can include saving the picture for the content entry.
            //Because enqueueing a save picture must be done only after the entity with the picture
            //itself is committed, SaveContentEntry must be invoked outside the main transaction so
            //that SaveContentEntryUseCase can control the transactions.
            courseBlockListVal.forEach { block ->
                block.contentEntry?.also { contentEntry ->
                    saveContentEntryUseCase(
                        contentEntry = contentEntry,
                        joinToParentUid = null,
                        picture = block.contentEntryPicture,
                        initPictureUri = initState.courseBlockList.firstOrNull {
                            it.courseBlockPicture?.cbpUid == block.courseBlockPicture?.cbpUid
                        }?.courseBlockPicture?.cbpPictureUri
                    )
                }
            }

            enqueueSavePictureUseCase.takeIf { updateImage }?.invoke(
                entityUid = entity.clazzUid,
                tableId = CoursePicture.TABLE_ID,
                pictureUri = coursePictureVal?.coursePictureUri
            )

            updatedCourseBlockPictures.forEach {
                enqueueSavePictureUseCase(
                    entityUid = it.cbpUid,
                    tableId = CourseBlockPicture.TABLE_ID,
                    pictureUri = it.cbpPictureUri
                )
            }

            val entityTimeZone = TimeZone.of(entity.effectiveTimeZone)
            val fromLocalDate = Clock.System.now().toLocalDateTime(entityTimeZone)
                .toLocalMidnight()
            val clazzLogCreatorManager: ClazzLogCreatorManager by di.instance()
            clazzLogCreatorManager.requestClazzLogCreation(
                entity.clazzUid,
                accountManager.currentAccount.endpointUrl,
                fromLocalDate.toInstant(entityTimeZone).toEpochMilliseconds(),
                fromLocalDate.toLocalEndOfDay().toInstant(entityTimeZone).toEpochMilliseconds()
            )
            Napier.d("onClickSave: done")

            finishWithResult(ClazzDetailViewModel.DEST_NAME, entity.clazzUid, entity)
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

    private fun updateCourseBlock(updatedBlock: CourseBlockAndEditEntities){
        viewModelScope.launch {
            updateCourseBlockList(_uiState.value.courseBlockList.replace(updatedBlock) {
                it.courseBlock.cbUid == updatedBlock.courseBlock.cbUid
            })
        }
    }

    fun onClickHideBlockPopupMenu(block: CourseBlockAndEditEntities) {
        updateCourseBlock(
            block.copy(
                courseBlock = block.courseBlock.copy(
                    cbHidden = true
                )
            )
        )
    }

    fun onClickUnHideBlockPopupMenu(block: CourseBlockAndEditEntities) {
        updateCourseBlock(
            block.copy(
                courseBlock = block.courseBlock.copy(
                    cbHidden = false
                )
            )
        )
    }

    fun onClickIndentBlockPopupMenu(block: CourseBlockAndEditEntities) {
        updateCourseBlock(
            block.copy(
                courseBlock = block.courseBlock.copy(
                    cbIndentLevel = block.courseBlock.cbIndentLevel + 1
                )
            )
        )
    }

    fun onClickUnIndentBlockPopupMenu(block: CourseBlockAndEditEntities) {
        updateCourseBlock(
            block.copy(
                courseBlock = block.courseBlock.copy(
                    cbIndentLevel = block.courseBlock.cbIndentLevel - 1
                )
            )
        )
    }

    fun onClickDeleteCourseBlock(block: CourseBlockAndEditEntities) {
        viewModelScope.launch {
            updateCourseBlockList(_uiState.value.courseBlockList.filter {
                it.courseBlock.cbUid != block.courseBlock.cbUid
            })
        }
    }

    fun onClickEditCourseBlock(block: CourseBlockAndEditEntities) {
        when(block.courseBlock.cbType) {
            CourseBlock.BLOCK_CONTENT_TYPE,
            CourseBlock.BLOCK_DISCUSSION_TYPE,
            CourseBlock.BLOCK_TEXT_TYPE,
            CourseBlock.BLOCK_MODULE_TYPE -> {
                navigateForResult(
                    nextViewName = CourseBlockEditViewModel.DEST_NAME,
                    key = RESULT_KEY_COURSEBLOCK,
                    serializer = CourseBlockAndEditEntities.serializer(),
                    currentValue = block,
                )
            }
            CourseBlock.BLOCK_ASSIGNMENT_TYPE -> {
                navigateForResult(
                    nextViewName = ClazzAssignmentEditViewModel.DEST_NAME,
                    key = RESULT_KEY_COURSEBLOCK,
                    serializer = CourseBlockAndEditEntities.serializer(),
                    currentValue = block,
                    args = buildMap {
                        put(UstadView.ARG_CLAZZUID, effectiveClazzUid.toString())
                        _uiState.value.entity?.terminology?.also { terminology ->
                            put(ARG_TERMINOLOGY,
                                json.encodeToString(CourseTerminology.serializer(), terminology)
                            )
                        }
                    }
                )
            }
        }
    }

    companion object {

        const val DEST_NAME = "CourseEdit"

        const val RESULT_KEY_SCHEDULE = "Schedule"

        const val STATE_KEY_SCHEDULES = "schedule"

        /**
         * Result key used for when the user edits text block, module block, or discussion block.
         * Plain CourseBlock will be returned
         */
        const val RESULT_KEY_COURSEBLOCK = "courseblock"

        const val RESULT_KEY_TIMEZONE = "timeZone"

        const val RESULT_KEY_TERMINOLOGY = "terminology"

        const val STATE_KEY_COURSEBLOCKS = "courseblocks"

        /**
         * Should not be the same as CourseBlockEdit - see note on CourseBlockEdit
         */
        const val RESULT_KEY_DESCRIPTION = "clazzDescriptionHtml"

    }

}
