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
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.interop.externalapppermission.DeclineExternalAppPermissionUseCase
import com.ustadmobile.core.domain.interop.externalapppermission.GrantExternalAppPermissionUseCase
import com.ustadmobile.core.impl.appstate.Snack
import org.kodein.di.direct
import org.kodein.di.on

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

    private val declineExternalAppPermissionUseCase: DeclineExternalAppPermissionUseCase by
        instance()

    private val permissionForPersonUid = savedStateHandle[ARG_SELECTED_ACCOUNT_PERSON_UID]?.toLong() ?: 0

    private val permissionForEndpoint = savedStateHandle[ARG_SELECTED_ACCOUNT_ENDPOINT_URL]

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
                title = systemImpl.getString(MR.strings.grant_permission),
                userAccountIconVisible = false,
            )
        }
    }

    fun onClickAccept() {
        viewModelScope.launch {
            if(permissionForPersonUid != 0L && permissionForEndpoint != null) {
                val grantExternalAppPermissionUseCase: GrantExternalAppPermissionUseCase =
                    di.on(Endpoint(permissionForEndpoint)).direct.instance()

                grantExternalAppPermissionUseCase(permissionForPersonUid)
            }else {
                snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.error)))
            }
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