package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.individual.IndividualLearnerViewModel
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.DI

data class AddAccountExistingUserUiState(
    val showWaitForRestart: Boolean = false,
)

class AddAccountExistingUserViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(AddAccountExistingUserUiState())

    val uiState: Flow<AddAccountExistingUserUiState>
        get() = _uiState.asStateFlow()

    init {
        _appUiState.value = AppUiState(
            navigationVisible = false,
            hideAppBar =false,
            userAccountIconVisible = false,
            title = systemImpl.getString(MR.strings.existing_user),
        )
    }

    fun onClickIndividual() {
        navController.navigate(IndividualLearnerViewModel.DEST_NAME, emptyMap())
    }

    fun onClickLearningSpace() {
        navController.navigate(
            LearningSpaceListViewModel.DEST_NAME,
            args = buildMap {
                putFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
            }
        )
    }


    companion object {

        const val DEST_NAME = "AddAccountExistingUser"
        const val PREF_TAG = "AddAccountExistingUser_screen"

    }
}