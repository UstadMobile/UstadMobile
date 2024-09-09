package com.ustadmobile.core.viewmodel.login

import com.ustadmobile.core.account.AdultAccountRequiredException
import com.ustadmobile.core.account.ConsentNotGrantedException
import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.MR
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.getversion.GetVersionUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.passkey.LoginWithPasskeyUseCase
import com.ustadmobile.core.domain.passkey.PassKeySignInData
import com.ustadmobile.core.domain.showpoweredby.GetShowPoweredByUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.appendSelectedAccount
import com.ustadmobile.core.util.ext.requireHttpPrefix
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.ext.verifySite
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.core.viewmodel.person.registerageredirect.RegisterAgeRedirectViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.door.ext.doorIdentityHashCode
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import io.github.aakira.napier.Napier
import io.ktor.client.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val fieldsEnabled: Boolean = true,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val versionInfo: String = "v42",
    val createAccountVisible: Boolean = false,
    val connectAsGuestVisible: Boolean = false,
    val loginIntentMessage: String? = null,
    val errorMessage: String? = null,
    val currentLanguage: UstadMobileSystemCommon.UiLanguage =
        UstadMobileSystemCommon.UiLanguage("en", "English"),
    val languageList: List<UstadMobileSystemCommon.UiLanguage> = listOf(currentLanguage),
    val showWaitForRestart: Boolean = false,
    val showPoweredBy: Boolean = false,
)

class LoginViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: Flow<LoginUiState> = _uiState.asStateFlow()

    private var nextDestination: String

    private var serverUrl: String

    private val impl: UstadMobileSystemImpl by instance()

    private val loginWithPasskeyUseCase: LoginWithPasskeyUseCase? by instanceOrNull()

    private val httpClient: HttpClient by instance()

    private val apiUrlConfig: SystemUrlConfig by instance()

    private var verifiedSite: Site? = null

    private val setLanguageUseCase: SetLanguageUseCase by instance()

    private val languagesConfig: SupportedLanguagesConfig by instance()


    private val getVersionUseCase: GetVersionUseCase? by instanceOrNull()

    private val getShowPoweredByUseCase: GetShowPoweredByUseCase? by instanceOrNull()

    private val dontSetCurrentSession: Boolean = savedStateHandle[ARG_DONT_SET_CURRENT_SESSION]
        ?.toBoolean() ?: false

    init {
        nextDestination = savedStateHandle[UstadView.ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME

        serverUrl =
            savedStateHandle[UstadView.ARG_LEARNINGSPACE_URL] ?: apiUrlConfig.presetLearningSpaceUrl
                    ?: "http://localhost"

        _uiState.update { prev ->
            prev.copy(
                versionInfo = "${systemImpl.getString(MR.strings.version)}: " +
                        getVersionUseCase?.invoke()?.versionString,
                loginIntentMessage = savedStateHandle[UstadView.ARG_INTENT_MESSAGE],
                currentLanguage = languagesConfig.getCurrentLanguage(systemImpl),
                languageList = languagesConfig.supportedUiLanguagesAndSysDefault(systemImpl),
                showPoweredBy = getShowPoweredByUseCase?.invoke() ?: false,
            )
        }

        val baseAppUiState = AppUiState(
            navigationVisible = false,
            userAccountIconVisible = false,
            title = impl.getString(MR.strings.login),
        )

        serverUrl = serverUrl.requirePostfix("/")
        val siteJsonStr: String? = savedStateHandle[UstadView.ARG_SITE]
        if (siteJsonStr != null) {
            _appUiState.value = baseAppUiState
            onSiteVerified(json.decodeFromString(siteJsonStr))
        } else {
            _uiState.update { prev ->
                prev.copy(
                    fieldsEnabled = false,
                )
            }
            _appUiState.value = baseAppUiState.copy(
                loadingState = LoadingUiState.INDETERMINATE
            )

            viewModelScope.launch {
                while (verifiedSite == null) {
                    try {
                        val site = httpClient.verifySite(serverUrl, 10000, json)
                        onSiteVerified(site) // onSiteVerified will set the workspace var, and exit the loop
                    } catch (e: Exception) {
                        Napier.w("Could not load site object for $serverUrl", e)
                        _uiState.update { prev ->
                            prev.copy(
                                errorMessage = impl.getString(MR.strings.login_network_error)
                            )
                        }
                        delay(10000)
                    }
                }
            }
        }
        onSignInWithPassKey()
    }

    private fun onSiteVerified(site: Site) {
        verifiedSite = site
        loadingState = LoadingUiState.NOT_LOADING
        _uiState.update { prev ->
            prev.copy(
                createAccountVisible = false,
                connectAsGuestVisible = site.guestLogin,
                fieldsEnabled = true,
                errorMessage = null,
            )
        }
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { prev ->
            prev.copy(username = username)
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { prev ->
            prev.copy(password = password)
        }
    }

    /**
     * After the user has logged in successfully or selected to proceed as a guest, go to the next
     * destination as per the arguments. This includes popping off the stack (using ARG_POPUPTO_ON_FINISH
     * or at least removing the login screen itself from the stack).
     */
    private fun goToNextDestAfterLoginOrGuestSelected(person: Person) {
        val goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
        Napier.d { "LoginPresenter: go to next destination: $nextDestination" }
        if (person.isPersonalAccount){
            nextDestination= ContentEntryListViewModel.DEST_NAME_HOME
        }
        navController.navigateToViewUri(
            nextDestination.appendSelectedAccount(person.personUid, LearningSpace(serverUrl)),
            goOptions
        )
    }

    fun onClickLogin() {
        _uiState.update { prev ->
            prev.copy(
                username = prev.username.trim(),
                password = prev.password.trim(),
                fieldsEnabled = false,
                passwordError = null,
                usernameError = null,
            )
        }

        val username = _uiState.value.username
        val password = _uiState.value.password

        if (username.isNotEmpty() && password.isNotEmpty()) {
            loadingState = LoadingUiState.INDETERMINATE
            viewModelScope.launch {
                var errorMessage: String? = null
                try {
                    val account = accountManager.login(
                        username = username.trim(),
                        password = password.trim(),
                        endpointUrl = serverUrl,
                        maxDateOfBirth = savedStateHandle[UstadView.ARG_MAX_DATE_OF_BIRTH]?.toLong()
                            ?: 0L,
                        dontSetCurrentSession = dontSetCurrentSession,
                    )
                    //this emit the passkeydata to show prompt to user to create passkey
                    accountManager.createPassKeyPrompt(
                        username.trim(), account.personUid, di.doorIdentityHashCode.toString(),
                        systemTimeInMillis(), serverUrl
                    )

                    goToNextDestAfterLoginOrGuestSelected(account.toPerson())
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
                    _uiState.update { prev ->
                        prev.copy(
                            fieldsEnabled = true,
                            errorMessage = errorMessage,
                        )
                    }
                }
            }
        } else {
            loadingState = LoadingUiState.NOT_LOADING
            _uiState.update { prev ->
                prev.copy(
                    fieldsEnabled = true,
                    usernameError = if (prev.username.isEmpty()) {
                        impl.getString(MR.strings.field_required_prompt)
                    } else {
                        null
                    },
                    passwordError = if (prev.password.isEmpty()) {
                        impl.getString(MR.strings.field_required_prompt)
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun onClickCreateAccount() {
        val args = mutableMapOf(
            UstadView.ARG_LEARNINGSPACE_URL to serverUrl,
            SiteTermsDetailView.ARG_SHOW_ACCEPT_BUTTON to true.toString(),
            UstadView.ARG_POPUPTO_ON_FINISH to
                    (savedStateHandle[UstadView.ARG_POPUPTO_ON_FINISH] ?: DEST_NAME)
        )

        args.putFromSavedStateIfPresent(PersonEditViewModel.REGISTRATION_ARGS_TO_PASS)

        navController.navigate(RegisterAgeRedirectViewModel.DEST_NAME, args)
    }

    fun onChangeLanguage(
        uiLanguage: UstadMobileSystemCommon.UiLanguage
    ) {
        if (uiLanguage != _uiState.value.currentLanguage) {
            val result = setLanguageUseCase(
                uiLanguage, DEST_NAME, navController,
                navArgs = buildMap {
                    putFromSavedStateIfPresent(UstadView.ARG_NEXT)
                    putFromSavedStateIfPresent(UstadView.ARG_LEARNINGSPACE_URL)
                    putFromSavedStateIfPresent(UstadView.ARG_SITE)
                }
            )

            _uiState.update { previous ->
                previous.copy(
                    currentLanguage = uiLanguage,
                    showWaitForRestart = result.waitForRestart
                )
            }
        }
    }

    fun onClickConnectAsGuest() {
        viewModelScope.launch {
            val guestPerson = accountManager.startGuestSession(serverUrl)
            goToNextDestAfterLoginOrGuestSelected(guestPerson.person)
        }
    }

     fun onSignInWithPassKey() {
        viewModelScope.launch {
            try {

                loginWithPasskeyUseCase?.let {
                    val passKeySignInData = it.invoke(
                        serverUrl.removePrefix("http://")
                            .removePrefix("https://")
                            .removeSuffix("/")
                    )
                    if (passKeySignInData != null) {
                        val account = accountManager.loginWithPasskey(passKeySignInData, serverUrl)
                        goToNextDestAfterLoginOrGuestSelected(account.toPerson())


                    } else {
                        snackDispatcher.showSnackBar(Snack("Account not found"))
                    }
                }

            } catch (e: Exception) {
                snackDispatcher.showSnackBar(Snack(message = "error occurred :" + e.message))

            }
        }
    }

    companion object {

        const val DEST_NAME = "Login"

    }
}
