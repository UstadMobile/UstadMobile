package com.ustadmobile.core.viewmodel.redirect

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.navigateToLink
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * Redirect the user on first open. Take user to the first screen (if there is an active session),
 * or to the enter link screen / login screen.
 */
class RedirectViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val nextViewArg = savedStateHandle[UstadView.ARG_NEXT]
    private val deepLink = savedStateHandle[UstadView.ARG_OPEN_LINK]

    private val apiUrlConfig: ApiUrlConfig by instance()

    init {
        val destination = deepLink ?: nextViewArg ?: ClazzList2View.VIEW_NAME_HOME

        viewModelScope.launch {
            navController.navigateToLink(
                link = destination,
                accountManager = accountManager,
                browserLinkOpener = { },
                userCanSelectServer = apiUrlConfig.canSelectServer,
                goOptions = UstadMobileSystemCommon.UstadGoOptions(
                    popUpToViewName = DEST_NAME,
                    popUpToInclusive = true,
                )
            )
        }

    }

    companion object {

        const val DEST_NAME = ""

    }
}
