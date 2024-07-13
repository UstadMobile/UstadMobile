package com.ustadmobile.core.viewmodel.redirect

import com.russhwolf.settings.Settings
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.navigateToLink
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.OnBoardingViewModel
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
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

    private val settings: Settings by instance()

    init {
        _appUiState.value = AppUiState(
            navigationVisible = false,
            hideAppBar = true,
        )
        val destinationArg = deepLink ?: nextViewArg

        if(settings.getStringOrNull(OnBoardingViewModel.PREF_TAG) != true.toString()) {
            navController.navigate(OnBoardingViewModel.DEST_NAME, buildMap {
                putFromSavedStateIfPresent(ARG_NEXT)
                putFromSavedStateIfPresent(ARG_OPEN_LINK)
            })
        }else {
            val destination = destinationArg ?: ClazzListViewModel.DEST_NAME_HOME

            viewModelScope.launch {
                navController.navigateToLink(
                    link = destination,
                    accountManager = accountManager,
                    openExternalLinkUseCase = { _, _ ->  },
                    userCanSelectServer = apiUrlConfig.canSelectServer,
                    goOptions = UstadMobileSystemCommon.UstadGoOptions(
                        clearStack = false
                    ),
                    forceAccountSelection = destinationArg != null,
                )
            }
        }
    }

    companion object {

        const val DEST_NAME = ""

    }
}
