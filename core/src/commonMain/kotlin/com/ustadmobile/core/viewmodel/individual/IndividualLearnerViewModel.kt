package com.ustadmobile.core.viewmodel.individual

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.appendSelectedAccount
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class IndividualLearnerViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, LoginViewModel.DEST_NAME) {

    private val impl: UstadMobileSystemImpl by instance()
    private var nextDestination: String =
        savedStateHandle[UstadView.ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME


    init {
        _appUiState.update { prev ->
            prev.copy(
                title = impl.getString(MR.strings.individual_action_title),
                userAccountIconVisible = false,
                navigationVisible = false,
                hideBottomNavigation = true,
            )
        }
    }


    private fun goToNextDestAfterLoginOrGuestSelected(personUid: Long, endpoint: Endpoint) {
        val goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
        navController.navigateToViewUri(
            nextDestination.appendSelectedAccount(
                personUid, Endpoint(
                    endpoint.toString()
                )
            ), goOptions
        )


    }

    fun onClickContinueWithoutLogin() {
        loadingState = LoadingUiState.INDETERMINATE

        viewModelScope.launch {
            accountManager.createLocalAccount()
            goToNextDestAfterLoginOrGuestSelected(
                accountManager.createLocalAccount().userSession.usPersonUid,
                accountManager.createLocalAccount().endpoint
            )
        }
    }

    fun onClickRestoreFile() {
        CoroutineScope(Dispatchers.Main).launch {

        }
    }

    companion object {
        const val DEST_NAME = "individualLearner"
    }
}