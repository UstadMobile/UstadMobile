package com.ustadmobile.core.viewmodel.interop.externalapppermissionrequest

import com.ustadmobile.core.domain.interop.InteropIcon
import com.ustadmobile.core.domain.interop.externalapppermission.GetExternalAppPermissionRequestInfoUseCase
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.interop.externalapppermission.DeclineExternalAppPermissionUseCase
import com.ustadmobile.core.domain.interop.externalapppermission.GrantExternalAppPermissionUseCase
import com.ustadmobile.core.util.ext.onActiveEndpoint

data class ExternalAppPermissionRequestUiState(
    val appName: String = "",
    val icon: InteropIcon? = null,
)

/**
 * Where the user will accept or reject a request for permission
 */
class ExternalAppPermissionRequestViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(
        ExternalAppPermissionRequestUiState()
    )

    val uiState: Flow<ExternalAppPermissionRequestUiState> = _uiState.asStateFlow()

    private val getExternalAppPermissionRequestInfo: GetExternalAppPermissionRequestInfoUseCase by
        instance()

    private val grantExternalAppPermissionUseCase: GrantExternalAppPermissionUseCase by
        di.onActiveEndpoint().instance()

    private val declineExternalAppPermissionUseCase: DeclineExternalAppPermissionUseCase by
        instance()

    init {
        viewModelScope.launch {
            val appInfo = getExternalAppPermissionRequestInfo()
            _uiState.update {
                it.copy(
                    appName = appInfo.appDisplayName,
                    icon = appInfo.icon,
                )
            }
        }


        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = false,
                title = systemImpl.getString(MR.strings.grant_permission)
            )
        }
    }

    fun onClickAccept() {
        viewModelScope.launch {
            grantExternalAppPermissionUseCase(activeUserPersonUid)
        }
    }

    fun onClickDecline() {
        viewModelScope.launch {
            declineExternalAppPermissionUseCase()
        }
    }

    companion object {

        const val DEST_NAME = "GrantExternalAppPermission"

    }
}