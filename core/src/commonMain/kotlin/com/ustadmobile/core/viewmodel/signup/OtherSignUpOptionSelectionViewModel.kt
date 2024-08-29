package com.ustadmobile.core.viewmodel.signup

import com.ustadmobile.core.MR
import com.ustadmobile.core.MR.strings.date
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.localaccount.GetLocalAccountsSupportedUseCase
import com.ustadmobile.core.domain.passkey.CreatePasskeyParams
import com.ustadmobile.core.domain.passkey.CreatePasskeyUseCase
import com.ustadmobile.core.domain.passkey.PasskeyResult
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.appendSelectedAccount
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.person.child.AddChildProfileViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.core.viewmodel.site.termsdetail.SiteTermsDetailViewModel
import com.ustadmobile.door.ext.doorIdentityHashCode
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_UNSET
import com.ustadmobile.lib.db.entities.PersonPicture
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


data class OtherSignUpOptionSelectionUiState(
    val showCreateLocaleAccount: Boolean = false
)

class OtherSignUpOptionSelectionViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String = DEST_NAME
) : UstadEditViewModel(di, savedStateHandle, destName) {

    private val _uiState: MutableStateFlow<OtherSignUpOptionSelectionUiState> =
        MutableStateFlow(OtherSignUpOptionSelectionUiState())

    private val getLocalAccountsSupportedUseCase: GetLocalAccountsSupportedUseCase by instance()

    val uiState: Flow<OtherSignUpOptionSelectionUiState> = _uiState.asStateFlow()


    init {
        loadingState = LoadingUiState.INDETERMINATE
        val title =
            systemImpl.getString(MR.strings.other_options)
        if (getLocalAccountsSupportedUseCase.invoke()) {
            _uiState.update { prev->
                prev.copy(
                    showCreateLocaleAccount = true
                )
            }
        }
        _appUiState.update {
            AppUiState(
                title = title,
                userAccountIconVisible = false,
                hideBottomNavigation = true,
            )
        }


    }

    fun onSignUpWithPasskey() {
        navController.popBackStack(SignUpViewModel.DEST_NAME,false)

    }
    fun onClickCreateLocalAccount() {
        loadingState = LoadingUiState.INDETERMINATE

        viewModelScope.launch {
            try {
                accountManager.createLocalAccount()
                val goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
                navController.navigate(ContentEntryListViewModel.DEST_NAME_HOME, emptyMap(), goOptions)
            } catch (e: Exception) {
                Napier.e("Error during login: ${e.message}", e)
            } finally {
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }
    fun onclickSignUpWithUsernameAdPassword() {

        val args = buildMap {
            put(SignUpViewModel.SIGN_WITH_USERNAME_AND_PASSWORD, "true")
        }

            navController.navigate(SignUpViewModel.DEST_NAME, args)


    }


    companion object {


        const val DEST_NAME = "otheroption"



    }

}