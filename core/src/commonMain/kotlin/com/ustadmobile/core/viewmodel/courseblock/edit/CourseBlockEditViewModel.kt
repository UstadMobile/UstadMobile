package com.ustadmobile.core.viewmodel.courseblock.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadEditView.Companion.DEFAULT_COMMIT_DELAY
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.CourseBlockViewModelConstants.CompletionCriteria
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.composites.ContentEntryAndContentJob
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockPicture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.kodein.di.DI

@kotlinx.serialization.Serializable
data class CourseBlockEditUiState(

    val block: CourseBlockAndEditEntities? = null,

    val canEditSelectedContentEntry: Boolean = false,

    val completionCriteriaOptions: List<CompletionCriteria> = emptyList(),

    val fieldsEnabled: Boolean = false,

    val caHideUntilDateError: String? = null,

    val caTitleError: String? = null,

    val caDeadlineError: String? = null,

    val caMaxPointsError: String? = null,

    val maxPointsRequired: Boolean = false,

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
            val selectedContentEntry = savedStateHandle.getJson(
                ARG_SELECTED_CONTENT_ENTRY, ContentEntryAndContentJob.serializer()
            )

            val loadedEntity = loadEntity(
                serializer = CourseBlockAndEditEntities.serializer(),
                makeDefault = {
                    val newUid = activeDb.doorPrimaryKeyManager.nextIdAsync(CourseBlock.TABLE_ID)
                    CourseBlockAndEditEntities(
                        courseBlock = CourseBlock().apply {
                            cbUid = newUid
                            cbActive = true
                            cbType = savedStateHandle[ARG_BLOCK_TYPE]?.toInt() ?: CourseBlock.BLOCK_MODULE_TYPE
                            cbTitle = selectedContentEntry?.entry?.title
                            cbDescription = selectedContentEntry?.entry?.description

                            if(selectedContentEntry != null) {
                                cbEntityUid = selectedContentEntry.entry?.contentEntryUid ?: 0
                                cbType = CourseBlock.BLOCK_CONTENT_TYPE
                            }
                        },
                        contentEntry = selectedContentEntry?.entry,
                        contentJob = selectedContentEntry?.contentJob,
                        contentJobItem = selectedContentEntry?.contentJobItem,
                        courseBlockPicture = CourseBlockPicture(cbpUid = newUid),
                        contentEntryPicture = selectedContentEntry?.picture
                    )
                },
                onLoadFromDb = { null }, //Does not load from database - always JSON passed from ClazzEdit
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(block = it)
                    }
                }
            )

            val contentEntryVal = loadedEntity?.contentEntry
            val blockVal = loadedEntity?.courseBlock
            if(contentEntryVal != null && blockVal != null) {
                val canEditContentEntry = when {
                    //When the user has just imported or selected something, they can go back to edit.
                    //Clicking, then going to the same screen they just came from would be confusing
                    savedStateHandle[ARG_ENTITY_JSON] == null -> false

                    contentEntryVal.contentOwnerType == ContentEntry.OWNER_TYPE_COURSE &&
                            contentEntryVal.contentOwner == blockVal.cbUid -> {
                        true
                    }

                    contentEntryVal.contentOwnerType == ContentEntry.OWNER_TYPE_COURSE -> {
                        activeRepo.coursePermissionDao().personHasPermissionWithClazzAsync2(
                            accountPersonUid = activeUserPersonUid,
                            clazzUid = contentEntryVal.contentOwner,
                            permission = PermissionFlags.COURSE_EDIT
                        )
                    }
                    else -> false
                }

                _uiState.update { it.copy(canEditSelectedContentEntry = canEditContentEntry) }
            }

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }

            _appUiState.update { prev ->
                prev.copy(
                    title = when(blockVal?.cbType) {
                        CourseBlock.BLOCK_MODULE_TYPE ->
                            createEditTitle(MR.strings.add_module, MR.strings.edit_module)
                        CourseBlock.BLOCK_TEXT_TYPE ->
                            createEditTitle(MR.strings.add_text, MR.strings.edit_text)
                        CourseBlock.BLOCK_DISCUSSION_TYPE ->
                            createEditTitle(MR.strings.add_discussion, MR.strings.edit_discussion)
                        CourseBlock.BLOCK_CONTENT_TYPE -> systemImpl.getString(MR.strings.edit_content_block)
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
                    onEntityChanged(
                        _uiState.value.block?.courseBlock?.copy(cbDescription = descriptionHtml)
                    )
                }
            }

            launch {
                resultReturner.filteredResultFlowForKey(KEY_CONTENT_ENTRY_EDIT_RESULT).collect { result ->
                    val contentEntryResult = result.result
                            as? ContentEntryAndContentJob ?: return@collect
                    _uiState.update { prev ->
                        prev.copy(
                            block = prev.block?.copy(
                                contentEntry = contentEntryResult.entry,
                                contentJobItem = contentEntryResult.contentJobItem,
                                contentJob = contentEntryResult.contentJob,
                            )
                        )
                    }
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

        val blockAndEntities = _uiState.updateAndGet { prev ->
            prev.copy(
                block = prev.block?.copy(
                    courseBlock = courseBlock
                ),
                caTitleError = updateErrorMessageOnChange(
                    prev.block?.courseBlock?.cbTitle,
                    courseBlock.cbTitle, prev.caTitleError)
            )
        }.block

        scheduleEntityCommitToSavedState(
            entity = blockAndEntities,
            serializer = CourseBlockAndEditEntities.serializer(),
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
            currentValue = _uiState.value.block?.asContentEntryAndJob(),
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

    fun onPictureChanged(pictureUri: String?) {
        val blockAndEntities = _uiState.updateAndGet { prev ->
            prev.copy(
                block = prev.block?.copy(
                    courseBlockPicture = prev.block.courseBlockPicture?.copy(
                        cbpPictureUri = pictureUri
                    )
                )
            )
        }.block

        scheduleEntityCommitToSavedState(
            entity = blockAndEntities,
            serializer = CourseBlockAndEditEntities.serializer(),
            commitDelay = DEFAULT_COMMIT_DELAY
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

        finishWithResult(_uiState.value.block)
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

        /**
         * These arguments are passed through screens when the user goes from ClazzEdit to add a
         * new ContentEntry block
         */
        val COURSE_BLOCK_CONTENT_ENTRY_PASS_THROUGH_ARGS = listOf(
            ARG_BLOCK_TYPE,
            ARG_CLAZZUID,
            ContentEntryEditViewModel.ARG_GO_TO_ON_CONTENT_ENTRY_DONE,
            ARG_BLOCK_TYPE,
            UstadView.ARG_RESULT_DEST_VIEWNAME,
            UstadView.ARG_RESULT_DEST_KEY,
            UstadListViewModel.ARG_LISTMODE,
        )

    }

}
