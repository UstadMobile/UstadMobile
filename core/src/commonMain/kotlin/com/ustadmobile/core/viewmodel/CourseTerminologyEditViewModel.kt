package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.encodeStringMapToString
import com.ustadmobile.core.util.ext.encodeToStringMap
import com.ustadmobile.core.util.ext.replace
import com.ustadmobile.core.util.ext.toTerminologyEntries
import com.ustadmobile.core.view.CourseTerminologyEditView
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.TerminologyEntry
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class CourseTerminologyEditUiState(

    val titleError: String? = null,

    val entity: CourseTerminology? = null,

    val fieldsEnabled: Boolean = true,

    val terminologyTermList: List<TerminologyEntry> = emptyList()

)

class CourseTerminologyEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, CourseTerminologyEditView.VIEW_NAME) {

    private val _uiState = MutableStateFlow(CourseTerminologyEditUiState(fieldsEnabled = false))

    val uiState: Flow<CourseTerminologyEditUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = createEditTitle(MessageID.add_new_terminology, MessageID.edit_terminology),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.save),
                    onClick = this::onClickSave
                ),
                loadingState = LoadingUiState.INDETERMINATE
            )
        }


        viewModelScope.launch {
            loadEntity(
                serializer = CourseTerminology.serializer(),
                makeDefault = {
                    CourseTerminology().apply {
                        ctUid = activeDb.doorPrimaryKeyManager.nextIdAsync(CourseTerminology.TABLE_ID)
                    }
                },
                onLoadFromDb = {
                    it.courseTerminologyDao.takeIf { entityUidArg != 0L }
                        ?.findByUidAsync(entityUidArg)
                },
                uiUpdate = { terminology ->
                    _uiState.update { prev ->
                        prev.copy(
                            entity = terminology,
                            terminologyTermList = terminology.toTerminologyEntries(json, systemImpl)
                        )
                    }
                }
            )

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }
        }
    }

    fun onEntityChanged(
        courseTerminology: CourseTerminology?
    ) {
        _uiState.update { prev ->
            prev.copy(
                entity = courseTerminology
            )
        }

        scheduleEntityCommitToSavedState(
            entity = courseTerminology,
            serializer = CourseTerminology.serializer(),
            commitDelay = 200,
        )
    }

    fun onTerminologyTermChanged(
        terminologyEntry: TerminologyEntry
    ) {
        val newTermList = _uiState.value.terminologyTermList.replace(terminologyEntry){
            it.messageId == terminologyEntry.messageId
        }

        _uiState.update { prev ->
            prev.copy(terminologyTermList = newTermList)
        }

        onEntityChanged(_uiState.value.entity?.shallowCopy {
            ctTerminology = newTermList.encodeToStringMap(json)
        })
    }


    fun onClickSave() {
        viewModelScope.launch {
            val terminology = _uiState.value.entity ?: return@launch
            activeDb.courseTerminologyDao.upsertAsync(terminology)
        }
    }

}
