package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.DI

data class AddAccountSelectNewUserTypeUiState(
    val error: Boolean = false,
)

class AddAccountSelectNewUserTypeViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(AddAccountSelectNewUserTypeUiState())

    val uiState: Flow<AddAccountSelectNewUserTypeUiState>
        get() = _uiState.asStateFlow()

    init {
        _appUiState.value = AppUiState(
            navigationVisible = false,
            hideAppBar =false,
            userAccountIconVisible = false,
            title = systemImpl.getString(MR.strings.new_user),
        )



    }
    fun onClickPersonalAccount(){
        navController.navigate(RegisterAgeRedirectViewModel.DEST_NAME, emptyMap())

    }
    fun onClickJoinLearningSpace(){

    }
    fun onClickNewLearningSpace(){

    }



    companion object {

        const val DEST_NAME = "AddAccountSelectNewUserType"
        const val PREF_TAG = "AddAccountSelectNewUserType_screen"

    }
}