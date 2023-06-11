package com.ustadmobile.core.viewmodel.scopedgrant.edit

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.lib.db.entities.ScopedGrant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ScopedGrantEditUiState(
    val entity: ScopedGrant? = null,
    )

class ScopedGrantEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, ScopedGrantEditView.VIEW_NAME) {

    private val _uiState = MutableStateFlow(ScopedGrantEditUiState())

    val uiState: Flow<ScopedGrantEditUiState> = _uiState.asStateFlow()

    init{
        _appUiState.update { prev ->
            prev.copy(
                title = createEditTitle(
                    MessageID.add_permission_for_a_person, MessageID.edit_permissions),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.done),
                    onClick = this::onClickSave
                )
            )
        }

        viewModelScope.launch {
            // TODO
        }
    }


    fun onClickSave(){
        val scopedGrant = _uiState.value.entity ?: return

        _uiState.update { prev ->
            prev.copy(
                //Any validations ?
            )
        }



        finishWithResult(scopedGrant)
    }

}