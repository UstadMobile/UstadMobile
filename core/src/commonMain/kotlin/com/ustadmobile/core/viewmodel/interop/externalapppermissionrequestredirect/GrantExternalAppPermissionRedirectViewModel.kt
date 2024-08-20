package com.ustadmobile.core.viewmodel.interop.externalapppermissionrequestredirect

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.navigateToLink
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.interop.externalapppermissionrequest.ExternalAppPermissionRequestViewModel
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * Receives an incoming request from an external app to grant permission and redirects (via account
 * selector as required) to allow the user to approve or deny the request.
 */
class GrantExternalAppPermissionRedirectViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val apiUrlConfig: SystemUrlConfig by instance()

    init {
        val destination = ExternalAppPermissionRequestViewModel.DEST_NAME
        viewModelScope.launch {
            navController.navigateToLink(
                link = destination,
                accountManager = accountManager,
                openExternalLinkUseCase = { _, _ ->  },
                userCanSelectServer = apiUrlConfig.canSelectServer,
                goOptions = UstadMobileSystemCommon.UstadGoOptions(
                    clearStack = true
                ),
                forceAccountSelection = true,
                dontSetCurrentSession = true,
            )
        }
    }

    companion object {

        const val DEST_NAME = "GrantExternalAppPermissionRedirect"
    }
}