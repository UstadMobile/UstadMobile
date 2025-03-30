package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.AdultAccountRequiredException
import com.ustadmobile.core.account.ConsentNotGrantedException
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.credentials.GetCredentialUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.appendSelectedAccount
import com.ustadmobile.core.util.ext.base64StringToByteArray
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNINGSPACE_URL
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListViewModel
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.lib.db.entities.Person
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

data class AddAccountSelectNewOrExistingUiState(
    val currentLanguage: UstadMobileSystemCommon.UiLanguage = UstadMobileSystemCommon.UiLanguage(
        "en",
        "English"
    ),
    val languageList: List<UstadMobileSystemCommon.UiLanguage> = listOf(currentLanguage),
    val showWaitForRestart: Boolean = false,
)

/**
 * Allow the user to select "new user" or "existing user" and then take them to the appropriate next
 * screen.
 *
 * Where the learning space URL is known (by ARG_LEARNINGSPACE_URL argument or
 * SystemUrlConfig.presetLearningSpaceUrl is not null):
 *    a) If new account creation is supported by the given learning space, then show the new user
 *       / existing user buttons and allow user to select. When they click an option, take them
 *       directly to the login or signup screen for the given learning space URL.
 *    b) If new account creation is not supported by the given learning space, go directly to the
 *       login screen for the given learning space url. The navigation should pop this screen off (
 *       e.g. going back will not return here)
 *
 * Where the learning space URL is not known (argument not provided and
 * SystemUrlConfig.presetLearningSpaceUrl is null):
 *    a) Show the new user / existing user buttons
 *    b) If SystemUrlConfig.newPersonalAccountsLearningSpaceUrl != null, then go to the Select
 *       personal account or learning space screen.
 *    c) If SystemUrlConfig.newPersonalAccountsLearningSpaceUrl is null, then the system does not
 *       support personal accounts. Go directly to LearningSpaceList screen for the user to select
 *       a learning space.
 */
class AddAccountSelectNewOrExistingViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(AddAccountSelectNewOrExistingUiState())

    private val supportLangConfig: SupportedLanguagesConfig by instance()

    private val setLanguageUseCase: SetLanguageUseCase by instance()

    private val apiUrlConfig: SystemUrlConfig by instance()

    private var nextDestination: String

    private val impl: UstadMobileSystemImpl by instance()

    private val dontSetCurrentSession: Boolean = savedStateHandle[ARG_DONT_SET_CURRENT_SESSION]
        ?.toBoolean() ?: false

    private val getCredentialUseCase: GetCredentialUseCase? by instanceOrNull()

    val uiState: Flow<AddAccountSelectNewOrExistingUiState>
        get() = _uiState.asStateFlow()

    init {
        nextDestination = savedStateHandle[UstadView.ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME

        _appUiState.value = AppUiState(
            navigationVisible = false,
            hideAppBar = true,
            userAccountIconVisible = false,
        )

        val allLanguages = supportLangConfig.supportedUiLanguagesAndSysDefault(systemImpl)
        val currentLanguage = supportLangConfig.getCurrentLanguage(systemImpl)

        _uiState.update {
            AddAccountSelectNewOrExistingUiState(currentLanguage, allLanguages)
        }

        getCredentials()
    }

    private fun getCredentials() {
        viewModelScope.launch {
            try {
                when (val credentialResult = getCredentialUseCase?.invoke()) {
                    is GetCredentialUseCase.PasskeyCredentialResult -> {
                        val userHandle = credentialResult.passKeySignInData.userHandle.base64StringToByteArray()
                        val endpointUrl= userHandle.decodeToString().substringAfter("@")

                        val account = accountManager.loginWithPasskey(
                            credentialResult.passKeySignInData,
                            apiUrlConfig.systemBaseUrl,
                        )

                        goToNextDestAfterSignIn(account.toPerson(), endpointUrl)
                    }

                    is GetCredentialUseCase.PasswordCredentialResult -> {
                        onLoginWithUsernameAndPasswordFromCredentialManager(
                            credentialUsername = credentialResult.credentialUsername,
                            password = credentialResult.password
                        )
                    }

                    is GetCredentialUseCase.Error -> {
                        Napier.e { "Error occurred: ${credentialResult.message}"}
                    }

                    null -> {
                        //Do nothing
                    }
                }
            } catch (e: Exception) {
                Napier.e { "Error occurred: ${e.message}"}
            }
        }
    }

    private fun onLoginWithUsernameAndPasswordFromCredentialManager(
        credentialUsername: String,
        password: String
    ) {
        viewModelScope.launch {
            var errorMessage: String? = null
            val (learningSpace, username) = GetCredentialUseCase.learningSpaceAndUsernameForCredentialUsername(
                credentialUsername
            )

            try {
                val account = accountManager.login(
                    username = username.trim(),
                    password = password.trim(),
                    endpointUrl = learningSpace.url,
                    maxDateOfBirth = savedStateHandle[UstadView.ARG_MAX_DATE_OF_BIRTH]?.toLong()
                        ?: 0L,
                    dontSetCurrentSession = dontSetCurrentSession,
                )

                goToNextDestAfterSignIn(account.toPerson(), learningSpace.url)
            } catch (e: AdultAccountRequiredException) {
                errorMessage = impl.getString(MR.strings.adult_account_required)
            } catch (e: UnauthorizedException) {
                errorMessage = impl.getString(MR.strings.wrong_user_pass_combo)
            } catch (e: ConsentNotGrantedException) {
                errorMessage = impl.getString(MR.strings.your_account_needs_approved)
            } catch (e: Exception) {
                errorMessage = impl.getString(MR.strings.login_network_error)
            } finally {
                loadingState = LoadingUiState.NOT_LOADING
                if (errorMessage!=null){
                    snackDispatcher.showSnackBar(
                        Snack(errorMessage.toString())
                    )
                }

            }
        }
    }

    fun onClickNewUser() {
        navigateUser(true)
    }

    fun onClickExistingUser() {
        navigateUser(false)
    }

    fun navigateUser(isNewUser: Boolean) {
        val userType = if (isNewUser) "new" else "existing"
        val arg = buildMap {
            putFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
            putFromSavedStateIfPresent(ARG_NEXT)
            put(SignUpViewModel.ARG_NEW_OR_EXISTING_USER, userType)
            apiUrlConfig.presetLearningSpaceUrl?.let {
                put(ARG_LEARNINGSPACE_URL, it)
            }
        }

        val destination = when {
            !apiUrlConfig.presetLearningSpaceUrl.isNullOrEmpty() -> {
                if (isNewUser) RegisterAgeRedirectViewModel.DEST_NAME else LoginViewModel.DEST_NAME
            }

            apiUrlConfig.newPersonalAccountsLearningSpaceUrl.isNullOrEmpty() -> {
                LearningSpaceListViewModel.DEST_NAME
            }
            else -> AddAccountSelectNewOrExistingUserTypeViewModel.DEST_NAME
        }

        navController.navigate(destination, arg)
    }

    fun onLanguageSelected(uiLanguage: UstadMobileSystemCommon.UiLanguage) {
        if (uiLanguage != _uiState.value.currentLanguage) {
            val result = setLanguageUseCase(
                uiLanguage, DEST_NAME, navController
            )

            _uiState.update { previous ->
                previous.copy(
                    currentLanguage = uiLanguage,
                    showWaitForRestart = result.waitForRestart
                )
            }
        }
    }

    private fun goToNextDestAfterSignIn(person: Person, serverUrl: String) {
        val goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
        Napier.d { "LoginPresenter: go to next destination: $nextDestination" }

        if (person.isPersonalAccount) {
            nextDestination = ContentEntryListViewModel.DEST_NAME_HOME
        }

        navController.navigateToViewUri(
            nextDestination.appendSelectedAccount(person.personUid, LearningSpace(serverUrl)),
            goOptions
        )
    }

    companion object {

        const val DEST_NAME = "addAccountSelectNewOrExisting"

    }
}