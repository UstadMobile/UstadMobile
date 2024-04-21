package com.ustadmobile.core.viewmodel.courseblock.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.core.view.UstadEditView.Companion.DEFAULT_COMMIT_DELAY
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.CourseBlockViewModelConstants.CompletionCriteria
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.composites.ContentEntryAndContentJob
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.kodein.di.DI

@kotlinx.serialization.Serializable
data class CourseBlockEditUiState(

    val block: CourseBlockAndEditEntities? = null,

    val selectedContentEntry: ContentEntryAndContentJob? = null,

    val canEditSelectedContentEntry: Boolean = false,

    val completionCriteriaOptions: List<CompletionCriteria> = emptyList(),

    val fieldsEnabled: Boolean = false,

    val caHideUntilDateError: String? = null,

    val caTitleError: String? = null,

    val caDeadlineError: String? = null,

    val caMaxPointsError: String? = null,

    val caGracePeriodError: String? = null,

    /**
     * The timezone that will be used when formatting timestamps. This should be set to the system
     * default timezone by the viewmodel.
     */
    val timeZone: String = "UTC",
) {
    val minScoreVisible: Boolean
        get() = block?.courseBlock?.cbCompletionCriteria == ContentEntry.COMPLETION_CRITERIA_MIN_SCORE

    val gracePeriodVisible: Boolean
        get() = deadlineVisible && block?.courseBlock?.cbDeadlineDate.isDateSet()

    val latePenaltyVisible: Boolean
        get() = gracePeriodVisible && block?.courseBlock?.cbGracePeriodDate.isDateSet()

    val completionCriteriaVisible: Boolean
        get() = completionCriteriaOptions.isNotEmpty()
     
    val deadlineVisible: Boolean
        get() = block?.courseBlock?.cbType == CourseBlock.BLOCK_ASSIGNMENT_TYPE ||
            block?.courseBlock?.cbType == CourseBlock.BLOCK_CONTENT_TYPE

    //For now - really the same as the deadline
    val maxPointsVisible: Boolean
        get() = block?.courseBlock?.cbType == CourseBlock.BLOCK_ASSIGNMENT_TYPE ||
            block?.courseBlock?.cbType == CourseBlock.BLOCK_CONTENT_TYPE

    val hasErrors: Boolean
        get() = caTitleError != null || caDeadlineError != null || caGracePeriodError != null ||
                caMaxPointsError != null

}

class CourseBlockEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(
        CourseBlockEditUiState(timeZone = TimeZone.currentSystemDefault().id)
    )

    val uiState: Flow<CourseBlockEditUiState> = _uiState.asStateFlow()
    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }

        viewModelScope.launch {
            val selectedContentJson = savedStateHandle[KEY_SAVED_STATE_SELECTED_CONTENT_ENTRY]
                ?: savedStateHandle[ARG_SELECTED_CONTENT_ENTRY]
            if(selectedContentJson != null) {
                _uiState.update { prev ->
                    prev.copy(
                        selectedContentEntry = json.decodeFromString(
                            ContentEntryAndContentJob.serializer(), selectedContentJson
                        )
                    )
                }
            }

            loadEntity(
                serializer = CourseBlockAndEditEntities.serializer(),
                makeDefault = {
                    CourseBlockAndEditEntities(
                        courseBlock = CourseBlock().apply {
                            cbUid = activeDb.doorPrimaryKeyManager.nextIdAsync(CourseBlock.TABLE_ID)
                            cbActive = true
                            cbType = savedStateHandle[ARG_BLOCK_TYPE]?.toInt() ?: CourseBlock.BLOCK_MODULE_TYPE
                        }
                    )
                },
                onLoadFromDb = { null }, //Does not load from database - always JSON passed from ClazzEdit
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(block = it)
                    }
                }
            )

            val contentEntryVal = _uiState.value.selectedContentEntry
            val blockVal = _uiState.value.block
            if(contentEntryVal != null && blockVal != null) {
                val canEditContentEntry = when {
                    contentEntryVal.entry?.contentOwnerType == ContentEntry.OWNER_TYPE_COURSE &&
                            contentEntryVal.entry?.contentOwner == blockVal.courseBlock.cbUid -> {
                        true
                    }

                    contentEntryVal.entry?.contentOwnerType == ContentEntry.OWNER_TYPE_COURSE -> {
                        activeRepo.coursePermissionDao.personHasPermissionWithClazzAsync2(
                            accountPersonUid = activeUserPersonUid,
                            clazzUid = contentEntryVal.entry?.contentOwner ?: 0L,
                            permission = PermissionFlags.COURSE_EDIT
                        )
                    }
                    else -> false
                }

                println("CourseBlock: can edit content entry = $canEditContentEntry ownerType= ${contentEntryVal.entry?.contentOwnerType} owner=${contentEntryVal.entry?.contentOwner}")
                _uiState.update { it.copy(canEditSelectedContentEntry = canEditContentEntry) }
            }

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }

            _appUiState.update {prev ->
                prev.copy(
                    title = when(_uiState.value.block?.courseBlock?.cbType) {
                        CourseBlock.BLOCK_MODULE_TYPE ->
                            createEditTitle(MR.strings.add_module, MR.strings.edit_module)
                        CourseBlock.BLOCK_TEXT_TYPE ->
                            createEditTitle(MR.strings.add_text, MR.strings.edit_text)
                        CourseBlock.BLOCK_DISCUSSION_TYPE ->
                            createEditTitle(MR.strings.add_discussion, MR.strings.edit_discussion)
                        CourseBlock.BLOCK_CONTENT_TYPE ->
                            createEditTitle(MR.strings.add_content, MR.strings.edit_content_block)
                        else -> ""
                    },
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = systemImpl.getString(MR.strings.done),
                        onClick = this@CourseBlockEditViewModel::onClickSave,
                    )
                )
            }

            launch {
                resultReturner.filteredResultFlowForKey(KEY_HTML_DESCRIPTION).collect { result ->
                    val descriptionHtml = result.result as? String ?: return@collect
                    onEntityChanged(_uiState.value.block?.courseBlock?.shallowCopy {
                        cbDescription = descriptionHtml
                    })
                }
            }

            launch {
                resultReturner.filteredResultFlowForKey(KEY_CONTENT_ENTRY_EDIT_RESULT).collect { result ->
                    val contentEntryResult = result.result
                            as? ContentEntryAndContentJob ?: return@collect
                    _uiState.update { it.copy(selectedContentEntry = contentEntryResult) }
                    savedStateHandle[KEY_SAVED_STATE_SELECTED_CONTENT_ENTRY] = json.encodeToString(
                        ContentEntryAndContentJob.serializer(), contentEntryResult
                    )
                }
            }
        }
    }

    fun onEntityChanged(courseBlock: CourseBlock?) {
        if(courseBlock == null)
            return

        _uiState.update { prev ->
            prev.copy(
                block = prev.block?.copy(
                    courseBlock = courseBlock
                ),
                caTitleError = updateErrorMessageOnChange(
                    prev.block?.courseBlock?.cbTitle,
                    courseBlock.cbTitle, prev.caTitleError)
            )
        }

        scheduleEntityCommitToSavedState(
            entity = courseBlock,
            serializer = CourseBlock.serializer(),
            commitDelay = DEFAULT_COMMIT_DELAY
        )
    }

    //Take the user to a separate screen with rich text editor.
    fun onClickEditDescription() {
        navigateToEditHtml(
            currentValue = _uiState.value.block?.courseBlock?.cbDescription,
            resultKey = KEY_HTML_DESCRIPTION,
            title = systemImpl.getString(MR.strings.description),
        )
    }

    fun onClickEditContentEntry() {
        navigateForResult(
            nextViewName = ContentEntryEditViewModel.DEST_NAME,
            key = KEY_CONTENT_ENTRY_EDIT_RESULT,
            currentValue = _uiState.value.selectedContentEntry,
            serializer = ContentEntryAndContentJob.serializer(),
            args = buildMap {
                _uiState.value.block?.also {
                    this[ContentEntryEditViewModel.ARG_COURSEBLOCK] = json.encodeToString(
                        CourseBlock.serializer(), it.courseBlock
                    )
                    this[ContentEntryEditViewModel.ARG_GO_TO_ON_CONTENT_ENTRY_DONE] = ContentEntryEditViewModel.FINISH_WITHOUT_SAVE_TO_DB.toString()
                }
            }
        )
    }

    fun onClickSave() {
        val courseBlockVal = _uiState.value.block ?: return
        if(courseBlockVal.courseBlock.cbTitle.isNullOrBlank()) {
            _uiState.update { prev ->
                prev.copy(
                    caTitleError = systemImpl.getString(MR.strings.required)
                )
            }
        }

        if(_uiState.value.hasErrors)
            return

        finishWithResult(
            _uiState.value
        )
    }


    companion object {

        const val DEST_NAME = "CourseBlockEdit"

        const val ARG_SELECTED_CONTENT_ENTRY = "SelectedContentEntry"

        const val ARG_BLOCK_TYPE = "blockType"

        /**
         * If this key is the same as used in ClazzEdit, then editing the value for CourseBlock
         * results in it being picked up by ClazzEdit as well as CourseBlock
         */
        const val KEY_HTML_DESCRIPTION = "courseBlockDesc"

        const val KEY_CONTENT_ENTRY_EDIT_RESULT = "courseBlockEditContentEntry"

        const val KEY_SAVED_STATE_SELECTED_CONTENT_ENTRY = "SavedSelectedContentEntry"

    }

}
