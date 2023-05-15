package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import org.kodein.di.DI
import org.kodein.di.instance


@kotlinx.serialization.Serializable
data class QRCodeScannerUiState(

    val barCodeVal: String = "",

    val fieldsEnabled: Boolean = true,

)

class QRCodeScannerViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(QRCodeScannerUiState())

    val uiState: Flow<QRCodeScannerUiState> = _uiState.asStateFlow()

    private val snackDisaptcher: SnackBarDispatcher by instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                loadingState = LoadingUiState.INDETERMINATE,
                title = createEditTitle(MessageID.new_assignment, MessageID.edit_assignment),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.done),
                    onClick = this::onClickSave,
                )
            )
        }

        _uiState.update { prev ->
            prev.copy(fieldsEnabled = false)
        }

        viewModelScope.launch {

            if(savedStateHandle[KEY_INIT_STATE] == null) {
                savedStateHandle[KEY_INIT_STATE] = withContext(Dispatchers.Default) {
                    json.encodeToString(_uiState.value)
                }
            }

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }

        }
    }


    fun onQRCodeDetected(){

    }

    private fun QRCodeScannerUiState.hasErrors() : Boolean {
        return true
    }

    fun onClickSave() {
        if(!_uiState.value.fieldsEnabled)
            return

        _uiState.update { prev ->
            prev.copy(fieldsEnabled = false)
        }

        viewModelScope.launch {
            val initState = savedStateHandle[KEY_INIT_STATE]?.let { initStateJson ->
                withContext(Dispatchers.Default) {
                    json.decodeFromString(QRCodeScannerUiState.serializer(), initStateJson)
                }
            } ?: return@launch



            if(_uiState.value.hasErrors()) {
                return@launch
            }

        }
    }

    companion object {

        const val DEST_NAME = "QrCodeScan"

    }
}
