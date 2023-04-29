package com.ustadmobile.core.viewmodel.courseblock.edit

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
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
import org.kodein.di.DI

@kotlinx.serialization.Serializable
data class CourseBlockEditUiState(

    val courseBlock: CourseBlock? = null,

    val completionCriteriaOptions: List<CompletionCriteria> = CompletionCriteria.values().toList(),

    val fieldsEnabled: Boolean = true,

    val caHideUntilDateError: String? = null,

    val caTitleError: String? = null,

    val caDeadlineError: String? = null,

    val deadlineVisible: Boolean = false,

    val caMaxPointsError: String? = null,

    val caGracePeriodError: String? = null,

    val gracePeriodVisible: Boolean = false,

    val timeZone: String = "UTC",
) {
    val minScoreVisible: Boolean
        get() = courseBlock?.cbCompletionCriteria == ContentEntry.COMPLETION_CRITERIA_MIN_SCORE
}

class CourseBlockEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(CourseBlockEditUiState(fieldsEnabled = false))

    val uiState: Flow<CourseBlockEditUiState> = _uiState.asStateFlow()
    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.done),
                    onClick = this::onClickSave,
                )
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
                            createEditTitle(MessageID.add_module, MessageID.edit_module)
                        CourseBlock.BLOCK_TEXT_TYPE ->
                            createEditTitle(MessageID.add_text, MessageID.edit_text)
                        CourseBlock.BLOCK_DISCUSSION_TYPE ->
                            createEditTitle(MessageID.add_discussion, MessageID.edit_discussion)
                        else -> ""
                    }
                )
            }

            launch {
                resultReturner.filteredResultFlowForKey(RESULT_KEY_HTML_DESC).collect { result ->
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
            resultKey = RESULT_KEY_HTML_DESC
        )
    }

    fun onClickSave() {
        finishWithResult(_uiState.value.courseBlock)
    }


    companion object {

        const val DEST_NAME = "CourseBlockEdit"

        const val ARG_BLOCK_TYPE = "blockType"


    }

}
