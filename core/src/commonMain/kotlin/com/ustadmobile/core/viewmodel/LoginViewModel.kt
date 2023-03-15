package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.account.AdultAccountRequiredException
import com.ustadmobile.core.account.ConsentNotGrantedException
import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.putFromSavedStateIfPresent
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.ext.verifySite
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.Site
import io.github.aakira.napier.Napier
import io.ktor.client.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import org.kodein.di.DI
import org.kodein.di.instance

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
    val errorMessage: String? = null
)

class LoginViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle){

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: Flow<LoginUiState> = _uiState.asStateFlow()

    private var nextDestination: String

    private var serverUrl: String

    private val impl: UstadMobileSystemImpl by instance()

    private val httpClient: HttpClient by instance()

    private var verifiedSite: Site? = null

    init {
        nextDestination = PersonListView.VIEW_NAME// savedStateHandle[UstadView.ARG_NEXT] ?: impl.getAppConfigDefaultFirstDest()

        serverUrl = savedStateHandle[UstadView.ARG_SERVER_URL] ?: impl.getAppConfigString(
            AppConfig.KEY_API_URL, "http://localhost"
        ) ?: ""

        _uiState.update { prev ->
            prev.copy(
                versionInfo = "TODO",
                loginIntentMessage = savedStateHandle[UstadView.ARG_INTENT_MESSAGE]
            )
        }

        val baseAppUiState = AppUiState(
            navigationVisible = false,
            title = impl.getString(MessageID.login),
        )

        serverUrl = serverUrl.requirePostfix("/")
        val siteJsonStr: String? = savedStateHandle[UstadView.ARG_SITE]
        if(siteJsonStr != null){
            _appUiState.value = baseAppUiState
            onVerifySite(json.decodeFromString(siteJsonStr))
        }else{
            _uiState.update { prev ->
                prev.copy(
                    fieldsEnabled = false,
                )
            }
            _appUiState.value = baseAppUiState.copy(
                loadingState = LoadingUiState.INDETERMINATE
            )

            viewModelScope.launch {
                while(verifiedSite == null) {
                    try {
                        val site = httpClient.verifySite(serverUrl, 10000)
                        onVerifySite(site) // onVerifySite will set the workspace var, and exit the loop
                    }catch(e: Exception) {
                        Napier.w("Could not load site object for $serverUrl", e)
                        _uiState.update { prev ->
                            prev.copy(
                                errorMessage = impl.getString(MessageID.login_network_error)
                            )
                        }
                        delay(10000)
                    }
                }
            }
        }
    }

    private fun onVerifySite(site: Site) {
        verifiedSite = site
        loadingState = LoadingUiState.NOT_LOADING
        _uiState.update { prev ->
            prev.copy(
                createAccountVisible = site.registrationAllowed,
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
    private fun goToNextDestAfterLoginOrGuestSelected() {
        val goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
        Napier.d { "LoginPresenter: go to next destination: $nextDestination" }

        val viewName = nextDestination.substringBefore('?')
        val args = UMFileUtil.parseURLQueryString(viewName)
        navController.navigate(viewName, args, goOptions)
    }

    fun onClickLogin(){
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

        if(username.isNotEmpty() && password.isNotEmpty()){
            viewModelScope.launch {
                var errorMessage: String? = null
                try {
                    accountManager.login(username.trim(), password.trim(), serverUrl,
                        savedStateHandle[UstadView.ARG_MAX_DATE_OF_BIRTH]?.toLong() ?: 0L)
                    goToNextDestAfterLoginOrGuestSelected()
                }catch(e: AdultAccountRequiredException) {
                    errorMessage = impl.getString(MessageID.adult_account_required)
                } catch(e: UnauthorizedException) {
                    errorMessage = impl.getString(MessageID.wrong_user_pass_combo)
                } catch(e: ConsentNotGrantedException) {
                    errorMessage =  impl.getString(MessageID.your_account_needs_approved)
                }catch(e: Exception) {
                    errorMessage = impl.getString(MessageID.login_network_error)
                }finally {
                    loadingState = LoadingUiState.NOT_LOADING
                    _uiState.update { prev ->
                        prev.copy(
                            fieldsEnabled = true,
                            errorMessage = errorMessage,
                        )
                    }
                }
            }
        }else{
            loadingState = LoadingUiState.NOT_LOADING
            _uiState.update { prev ->
                prev.copy(
                    fieldsEnabled = true,
                    usernameError = if(prev.username.isEmpty()) {
                        impl.getString(MessageID.field_required_prompt)
                    } else {
                        null
                    },
                    passwordError = if(prev.password.isEmpty()) {
                        impl.getString(MessageID.field_required_prompt)
                    }else {
                        null
                    }
                )
            }
        }
    }

    fun onClickCreateAccount(){
        val args = mutableMapOf(
            UstadView.ARG_SERVER_URL to serverUrl,
            SiteTermsDetailView.ARG_SHOW_ACCEPT_BUTTON to true.toString(),
            SiteTermsDetailView.ARG_USE_DISPLAY_LOCALE to true.toString(),
            UstadView.ARG_POPUPTO_ON_FINISH to
                    (savedStateHandle[UstadView.ARG_POPUPTO_ON_FINISH] ?: Login2View.VIEW_NAME))

        args.putFromSavedStateIfPresent(savedStateHandle, UstadView.ARG_NEXT)
        args.putFromSavedStateIfPresent(savedStateHandle, PersonEditView.REGISTER_VIA_LINK)

        navController.navigate(RegisterAgeRedirectView.VIEW_NAME, args)
    }

    fun handleConnectAsGuest(){
//        presenterScope.launch {
//            accountManager.startGuestSession(serverUrl)
//            goToNextDestAfterLoginOrGuestSelected()
//        }
    }

}
