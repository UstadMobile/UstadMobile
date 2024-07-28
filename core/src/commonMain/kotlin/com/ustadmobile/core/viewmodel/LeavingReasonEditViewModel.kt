package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.LeavingReason
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class LeavingReasonEditUiState(
    val leavingReason: LeavingReason? = null,
    val reasonTitleError: String? = null,
    val fieldsEnabled: Boolean = true,
)

class LeavingReasonEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(LeavingReasonEditUiState(fieldsEnabled = false))

    val uiState: Flow<LeavingReasonEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadEntity(
                onLoadFromDb = {
                    it.leavingReasonDao().findByUidAsync(savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L)
                },
                makeDefault = {
                    LeavingReason().apply {
                        leavingReasonTitle = ""
                    }
                },
                serializer = LeavingReason.serializer(),
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(leavingReason = it)
                    }
                }
            )

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }
        }
    }

    fun onEntityChanged(leavingReason: LeavingReason?) {
        _uiState.update { prev ->
            prev.copy(leavingReason = leavingReason)
        }

        scheduleEntityCommitToSavedState(
            entity = leavingReason,
            serializer = LeavingReason.serializer(),
            commitDelay = 200,
        )
    }

    fun onClickSave() {
        _uiState.update { prev ->
            prev.copy(fieldsEnabled = false)
        }

        viewModelScope.launch {
            val leavingReason = _uiState.value.leavingReason ?: return@launch

            if(leavingReason.leavingReasonUid == 0L) {
                activeDb.leavingReasonDao().insertAsync(leavingReason)
            }else {
                activeDb.leavingReasonDao().updateAsync(leavingReason)
            }
        }
    }

    companion object {

        const val DEST_NAME = "LeavingReasonEdit"
    }

}
