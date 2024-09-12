package com.ustadmobile.core.viewmodel.redirect

import com.russhwolf.settings.Settings
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.navigateToLink
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.AddAccountSelectNewOrExistingViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
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
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val nextViewArg = savedStateHandle[UstadView.ARG_NEXT]
    private val deepLink = savedStateHandle[UstadView.ARG_OPEN_LINK]

    private val apiUrlConfig: SystemUrlConfig by instance()

    private val settings: Settings by instance()

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
        if(!activeLearningSpace.url.contains("localhost")) {
            val db = di.direct.on(activeLearningSpace).instance<UmAppDatabase>(
                tag = DoorTag.TAG_DB
            )
            println(db)
        }

        val clazzType =if (accountManager.currentUserSession.person.isPersonalAccount){
            ContentEntryListViewModel.DEST_NAME_HOME
        }else{
            ClazzListViewModel.DEST_NAME_HOME
        }


        val destination = destinationArg ?: clazzType

        viewModelScope.launch {
            navController.navigateToLink(
                link = destination,
                accountManager = accountManager,
                openExternalLinkUseCase = { _, _ ->  },
                userCanSelectServer = apiUrlConfig.canSelectServer,
                goOptions = UstadMobileSystemCommon.UstadGoOptions(
                    clearStack = true
                ),
                forceAccountSelection = destinationArg != null,
            )
        }

    }

    companion object {

        const val DEST_NAME = ""

    }
}

