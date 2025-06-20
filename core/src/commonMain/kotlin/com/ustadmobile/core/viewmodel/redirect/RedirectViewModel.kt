package com.ustadmobile.core.viewmodel.redirect

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.navigation.GetDefaultDestinationUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.navigateToLink
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.door.ext.DoorTag
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

/**
 * Redirect the user on first open. Take user to the first screen (if there is an active session),
 * or to the enter link screen / login screen.
 */
class RedirectViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val nextViewArg = savedStateHandle[UstadView.ARG_NEXT]
    private val deepLink = savedStateHandle[UstadView.ARG_OPEN_LINK]

    private val apiUrlConfig: SystemUrlConfig by instance()

    init {
        _appUiState.value = AppUiState(
            navigationVisible = false,
            hideAppBar = true,
        )
        val destinationArg = deepLink ?: nextViewArg

        /**
         * "Thank you, Google"
         * Using PagingSource immediately when an update is happening causes disaster
         * https://issuetracker.google.com/issues/192269858
         *
         * Seems to be the same as :
         * https://github.com/sqlcipher/android-database-sqlcipher/issues/640
         *
         * Which references:
         * https://issuetracker.google.com/issues/65820362
         *
         */
        val activeLearningSpace = accountManager.activeLearningSpace
        if (!activeLearningSpace.url.contains("localhost")) {
            val db = di.direct.on(activeLearningSpace).instance<UmAppDatabase>(
                tag = DoorTag.TAG_DB
            )
            println(db)
        }

        viewModelScope.launch {
            val destination = destinationArg ?: di.on(accountManager.currentUserSession.learningSpace)
                .direct.instance<GetDefaultDestinationUseCase>().invoke()

            navController.navigateToLink(
                link = destination,
                accountManager = accountManager,
                openExternalLinkUseCase = { _, _ ->  },
                userCanSelectServer = apiUrlConfig.canSelectServer,
                goOptions = UstadMobileSystemCommon.UstadGoOptions(
                    clearStack = true
                ),
                forceAccountSelection = destinationArg != null,
                checkRegistrationAllowedUseCase = { di.on(it).direct.instance() },
                presetLearningSpaceUrl = apiUrlConfig.presetLearningSpaceUrl
            )
        }

    }

    companion object {

        const val DEST_NAME = ""

    }
}