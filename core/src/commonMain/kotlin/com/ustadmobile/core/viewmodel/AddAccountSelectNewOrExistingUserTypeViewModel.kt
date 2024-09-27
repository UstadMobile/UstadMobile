package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.requireHttpPrefix
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNINGSPACE_URL
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListViewModel
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel.Companion.ARG_IS_PERSONAL_ACCOUNT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.instance

data class AddAccountSelectNewOrExistingUserTypeUiState(
    val showAddPersonalAccount: Boolean = false,
)

class AddAccountSelectNewOrExistingUserTypeViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val apiUrlConfig: SystemUrlConfig by instance()

    private val _uiState = MutableStateFlow(AddAccountSelectNewOrExistingUserTypeUiState())
    private val isNewUser = savedStateHandle[SignUpViewModel.ARG_NEW_OR_EXISTING_USER]=="new"

    val uiState: Flow<AddAccountSelectNewOrExistingUserTypeUiState>
        get() = _uiState.asStateFlow()

    init {
        _appUiState.value = AppUiState(
            navigationVisible = false,
            hideAppBar =false,
            userAccountIconVisible = false,
            title =if (isNewUser) systemImpl.getString(MR.strings.new_user) else systemImpl.getString(MR.strings.existing_user)  ,
        )
        if (apiUrlConfig.newPersonalAccountsLearningSpaceUrl != null) {
            _uiState.update { prev->
                prev.copy(
                    showAddPersonalAccount = true
                )
            }
        }


    }
    fun onClickPersonalAccount(){
        val viewName =if(savedStateHandle[SignUpViewModel.ARG_NEW_OR_EXISTING_USER]=="new"){
            RegisterAgeRedirectViewModel.DEST_NAME
        }else{
            LoginViewModel.DEST_NAME
        }
        navController.navigate(
            viewName,
            args = buildMap {
                putFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
                put(ARG_IS_PERSONAL_ACCOUNT,true.toString())
                put(ARG_LEARNINGSPACE_URL,apiUrlConfig.newPersonalAccountsLearningSpaceUrl?.requireHttpPrefix()
                    ?.requirePostfix("/")?:"")
            }
        )

    }
    fun onClickJoinLearningSpace(){
        navController.navigate(
            LearningSpaceListViewModel.DEST_NAME,
            args = buildMap {
                putFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
            }
        )


    }
    fun onClickNewLearningSpace(){

    }



    companion object {

        const val DEST_NAME = "AddAccountSelectNewUserType"

    }
}