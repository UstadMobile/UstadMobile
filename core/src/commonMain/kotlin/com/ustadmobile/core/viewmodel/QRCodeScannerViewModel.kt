package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.instance


class QRCodeScannerViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val impl: UstadMobileSystemImpl by instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                loadingState = LoadingUiState.NOT_LOADING,
                title = impl.getString(MessageID.qr_code_scan)
            )
        }
    }

    fun onQRCodeDetected(qrCode: String){
        finishWithResult(qrCode)
    }

    companion object {

        const val DEST_NAME = "QrCodeScan"

    }
}
