package com.ustadmobile.core.viewmodel.account.addaccountselectneworexisting

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.AdultAccountRequiredException
import com.ustadmobile.core.account.ConsentNotGrantedException
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.credentials.GetCredentialUseCase
import com.ustadmobile.core.domain.credentials.username.ParseCredentialUsernameUseCase
import com.ustadmobile.core.domain.navigation.GetDefaultDestinationUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.appendSelectedAccount
import com.ustadmobile.core.util.ext.base64StringToByteArray
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNINGSPACE_URL
import com.ustadmobile.core.viewmodel.account.addaccountselectusertype.AddAccountSelectNewOrExistingUserTypeViewModel
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel.Companion.ARG_CHILD_DATE_OF_BIRTH
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel.Companion.ARG_CHILD_GENDER
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel.Companion.ARG_CHILD_NAME
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListViewModel
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import com.ustadmobile.core.viewmodel.person.registerminorwaitforparent.RegisterMinorWaitForParentViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.lib.db.entities.Person
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.on

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

    private val impl: UstadMobileSystemImpl by instance()

    private val dontSetCurrentSession: Boolean = savedStateHandle[ARG_DONT_SET_CURRENT_SESSION]
        ?.toBoolean() ?: false

    private val getCredentialUseCase: GetCredentialUseCase? by instanceOrNull()

    val uiState: Flow<AddAccountSelectNewOrExistingUiState> = _uiState.asStateFlow()

    private val parseCredentialUsernameUseCase: ParseCredentialUsernameUseCase by instance()

    init {
        /**
         * this check is for RegisterMinorWaitForParentViewModel
         * If the app is being used for the first time, then it goes back to the start screen (SelectNewOrExisting).
         * If this was reached through the account manager - then go back to the account manager
         */
        if (savedStateHandle[RegisterMinorWaitForParentViewModel.ARG_REFERER_SCREEN]!=null){
            savedStateHandle[RegisterMinorWaitForParentViewModel.ARG_REFERER_SCREEN]=
                DEST_NAME

        }
        val nextDestination = savedStateHandle[UstadView.ARG_NEXT]
        if (nextDestination !=null){
            val questionIndex = nextDestination.indexOf('?')
            val args = if(questionIndex > 0) {
                UMFileUtil.parseURLQueryString(nextDestination.substring(questionIndex))
            }else {
                emptyMap()
            }
            if (args.containsKey(ARG_CHILD_NAME)){
                savedStateHandle[ARG_CHILD_NAME] = args[ARG_CHILD_NAME]
                savedStateHandle[ARG_CHILD_GENDER] = args[ARG_CHILD_GENDER]
                savedStateHandle[ARG_CHILD_DATE_OF_BIRTH] = args[ARG_CHILD_DATE_OF_BIRTH]
            }

        }

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
                        val userHandle = credentialResult.passKeySignInData.userHandle
                            .base64StringToByteArray()
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
                Napier.e(
                    message = "AddAccountSelectNewOrExistingViewModel: getCredentials: Error occurred",
                    throwable = e
                )
            }
        }
    }

    private fun onLoginWithUsernameAndPasswordFromCredentialManager(
        credentialUsername: String,
        password: String
    ) {
        viewModelScope.launch {
            var errorMessage: String? = null
            val (learningSpace, username) = parseCredentialUsernameUseCase(credentialUsername)

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
                if (errorMessage != null){
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

    internal fun navigateUser(isNewUser: Boolean) {
        val userType = if (isNewUser)
            SignUpViewModel.ARG_VAL_NEW_USER
        else
            SignUpViewModel.ARG_VAL_EXISTING_USER

        val args = buildMap {
            putAllFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
            putFromSavedStateIfPresent(ARG_NEXT)
            put(SignUpViewModel.ARG_NEW_OR_EXISTING_USER, userType)
            apiUrlConfig.presetLearningSpaceUrl?.let {
                put(ARG_LEARNINGSPACE_URL, it)
            }
        }

        //Select destination as per KDoc class comments.
        val destination = when {
            /* When there is a preset learning space url, there is no need to ask whether the
             * account is personal or not. Proceed directly to the age check screen for new users
             * or the login screen for existing users.
             */
            !apiUrlConfig.presetLearningSpaceUrl.isNullOrEmpty() -> {
                if (isNewUser) RegisterAgeRedirectViewModel.DEST_NAME else LoginViewModel.DEST_NAME
            }

            /* When there is no learning space url for personal accounts set, there is no need to
             * ask whether an account is personal or not. Proceed directly to the LearningSpaceList
             * screen.
             */
            apiUrlConfig.newPersonalAccountsLearningSpaceUrl.isNullOrEmpty() -> {
                LearningSpaceListViewModel.DEST_NAME
            }

            /* The user might want to create a personal account, or might want to join/create a
             * multi-user learning space. Proceed to the AddAccountSelectNewOrExisting screen.
             */
            else -> AddAccountSelectNewOrExistingUserTypeViewModel.DEST_NAME
        }

        navController.navigate(destination, args)
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

    private fun goToNextDestAfterSignIn(
        person: Person,
        serverUrl: String
    ) {
        val getDefaultDestinationUseCase: GetDefaultDestinationUseCase =
            di.on(LearningSpace(serverUrl)).direct.instance()
        val nextDestVal = savedStateHandle[UstadView.ARG_NEXT] ?: getDefaultDestinationUseCase()
        Napier.d { "AddAccountSelectNewOrExistingViewModel: go to next destination: $nextDestVal" }

        navController.navigateToViewUri(
            viewUri = nextDestVal.appendSelectedAccount(person.personUid, LearningSpace(serverUrl)),
            goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
        )
    }

    companion object {

        const val DEST_NAME = "AddAccountSelectNewOrExisting"

    }
}