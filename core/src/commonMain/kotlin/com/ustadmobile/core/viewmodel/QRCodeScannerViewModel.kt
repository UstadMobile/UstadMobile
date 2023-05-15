package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
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


@kotlinx.serialization.Serializable
data class QRCodeScannerUiState(

    val qrCode: String = "",

    val qrCodeError: String? = null,

)

class QRCodeScannerViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(QRCodeScannerUiState())

    val uiState: Flow<QRCodeScannerUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                loadingState = LoadingUiState.INDETERMINATE,
                title = createEditTitle(MessageID.qr_code_scan, MessageID.qr_code_scan),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.done),
                    onClick = this::onClickSave,
                )
            )
        }

        viewModelScope.launch {

            if(savedStateHandle[KEY_INIT_STATE] == null) {
                savedStateHandle[KEY_INIT_STATE] = withContext(Dispatchers.Default) {
                    json.encodeToString(_uiState.value)
                }
            }
        }
    }

    fun onQRCodeDetected(qrCode: String){
        _uiState.update { prev ->
            prev.copy(qrCode = qrCode)
        }

        onClickSave()
    }

    fun onClickSave() {
        val siteLink = _uiState.value.qrCode

        if(_uiState.value.qrCode.isNotBlank()) {
            finishWithResult(siteLink)
        }
    }

    companion object {

        const val DEST_NAME = "QrCodeScan"

    }
}
