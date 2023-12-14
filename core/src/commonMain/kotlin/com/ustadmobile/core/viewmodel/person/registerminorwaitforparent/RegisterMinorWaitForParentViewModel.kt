package com.ustadmobile.core.viewmodel.person.registerminorwaitforparent

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR

data class RegisterMinorWaitForParentUiState(

    val username: String = "",

    val password: String = "",

    val parentContact: String = "",

)

class RegisterMinorWaitForParentViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(RegisterMinorWaitForParentUiState())

    val uiState : Flow<RegisterMinorWaitForParentUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.register),
                navigationVisible = false,
                userAccountIconVisible = false,
            )
        }
        _uiState.update { prev ->
            prev.copy(
                username = savedStateHandle[ARG_USERNAME] ?: "",
                password = savedStateHandle[ARG_PASSWORD] ?: "",
                parentContact = savedStateHandle[ARG_PARENT_CONTACT] ?: "",
            )
        }
    }

    fun onClickOK() {
        /**
         * PersonEdit will pop off up to RegisterAgeRedirect inclusive (e.g. includes register age
         * redirect and person edit itself), so if the user goes back, they go back to the login
         * screen.
         */
        navController.popBackStack(DEST_NAME, true)
    }

    companion object {

        const val DEST_NAME = "WaitForParent"

        const val ARG_USERNAME = "username"

        const val ARG_PASSWORD = "password"

        const val ARG_PARENT_CONTACT = "parentContact"

    }
}
