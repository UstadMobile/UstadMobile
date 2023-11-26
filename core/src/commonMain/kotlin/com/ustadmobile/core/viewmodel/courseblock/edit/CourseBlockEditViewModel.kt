package com.ustadmobile.core.viewmodel.courseblock.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.core.view.UstadEditView.Companion.DEFAULT_COMMIT_DELAY
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.CourseBlockViewModelConstants.CompletionCriteria
import com.ustadmobile.door.ext.doorPrimaryKeyManager
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

    val courseBlock: CourseBlock? = null,

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
        get() = courseBlock?.cbCompletionCriteria == ContentEntry.COMPLETION_CRITERIA_MIN_SCORE

    val gracePeriodVisible: Boolean
        get() = deadlineVisible && courseBlock?.cbDeadlineDate.isDateSet()

    val latePenaltyVisible: Boolean
        get() = gracePeriodVisible && courseBlock?.cbGracePeriodDate.isDateSet()

    val completionCriteriaVisible: Boolean
        get() = completionCriteriaOptions.isNotEmpty()
     
    val deadlineVisible: Boolean
        get() = courseBlock?.cbType == CourseBlock.BLOCK_ASSIGNMENT_TYPE ||
            courseBlock?.cbType == CourseBlock.BLOCK_CONTENT_TYPE

    //For now - really the same as the deadline
    val maxPointsVisible: Boolean
        get() = courseBlock?.cbType == CourseBlock.BLOCK_ASSIGNMENT_TYPE ||
            courseBlock?.cbType == CourseBlock.BLOCK_CONTENT_TYPE

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
            loadEntity(
                serializer = CourseBlock.serializer(),
                makeDefault = {
                    CourseBlock().apply {
                        cbUid = activeDb.doorPrimaryKeyManager.nextIdAsync(CourseBlock.TABLE_ID)
                        cbActive = true
                        cbType = savedStateHandle[ARG_BLOCK_TYPE]?.toInt() ?: CourseBlock.BLOCK_MODULE_TYPE
                    }
                },
                onLoadFromDb = { null }, //Does not load from database - always JSON passed from ClazzEdit
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(courseBlock = it)
                    }
                }
            )

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }

            _appUiState.update {prev ->
                prev.copy(
                    title = when(_uiState.value.courseBlock?.cbType) {
                        CourseBlock.BLOCK_MODULE_TYPE ->
                            createEditTitle(MR.strings.add_module, MR.strings.edit_module)
                        CourseBlock.BLOCK_TEXT_TYPE ->
                            createEditTitle(MR.strings.add_text, MR.strings.edit_text)
                        CourseBlock.BLOCK_DISCUSSION_TYPE ->
                            createEditTitle(MR.strings.add_discussion, MR.strings.edit_discussion)
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
                    onEntityChanged(_uiState.value.courseBlock?.shallowCopy {
                        cbDescription = descriptionHtml
                    })
                }
            }
        }
    }

    fun onEntityChanged(courseBlock: CourseBlock?) {
        _uiState.update { prev ->
            prev.copy(courseBlock = courseBlock)
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
            currentValue = _uiState.value.courseBlock?.cbDescription,
            resultKey = KEY_HTML_DESCRIPTION,
            title = systemImpl.getString(MR.strings.description),
        )
    }

    fun onClickSave() {
        finishWithResult(_uiState.value.courseBlock)
    }


    companion object {

        const val DEST_NAME = "CourseBlockEdit"

        const val ARG_BLOCK_TYPE = "blockType"

        /**
         * If this key is the same as used in ClazzEdit, then editing the value for CourseBlock
         * results in it being picked up by ClazzEdit as well as CourseBlock
         */
        const val KEY_HTML_DESCRIPTION = "courseBlockDesc"


    }

}
