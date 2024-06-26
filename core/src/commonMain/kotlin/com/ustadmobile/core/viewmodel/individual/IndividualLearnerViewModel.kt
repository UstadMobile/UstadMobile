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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

data class IndividualLearnerUiState(
    val selectedFileUri: String? = null,
    val selectedFileName: String? = null
)


class IndividualLearnerViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, LoginViewModel.DEST_NAME) {

    private val impl: UstadMobileSystemImpl by instance()
    private var nextDestination: String =
        savedStateHandle[UstadView.ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME


    private val _uiState = MutableStateFlow(IndividualLearnerUiState())
    val uiState: StateFlow<IndividualLearnerUiState> = _uiState.asStateFlow()
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

    fun onRestoreFileSelected(fileUri: String, fileName: String) {
        _uiState.update { it.copy(selectedFileUri = fileUri, selectedFileName = fileName) }
        // add logic to handle the selected file, e.g., start the restore process
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

    companion object {
        const val DEST_NAME = "IndividualLearner"
    }
}