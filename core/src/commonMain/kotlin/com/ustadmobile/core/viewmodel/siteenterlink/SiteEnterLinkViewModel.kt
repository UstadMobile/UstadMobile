package com.ustadmobile.core.viewmodel.siteenterlink

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.requireHttpPrefix
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.ext.verifySite
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_SITE
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import io.github.aakira.napier.Napier
import io.ktor.client.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.kodein.di.DI
import org.kodein.di.instance

data class SiteEnterLinkUiState(
    val siteLink: String = "",
    val validLink: Boolean = false,
    val progressVisible: Boolean = false,
    val linkError: String? = null,
    val fieldsEnabled: Boolean = true,
)

class SiteEnterLinkViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(SiteEnterLinkUiState())

    val uiState: Flow<SiteEnterLinkUiState> = _uiState.asStateFlow()

    private val httpClient: HttpClient by instance()

    private val impl: UstadMobileSystemImpl by instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = impl.getString(MR.strings.enter_link),
                userAccountIconVisible = false,
                navigationVisible = false,
            )
        }

        _uiState.update { prev ->
            prev.copy(siteLink = savedStateHandle[KEY_LINK] ?: "")
        }
    }

    fun onClickNext() {
        _uiState.update {
            it.copy(fieldsEnabled = false)
        }

        loadingState = LoadingUiState.INDETERMINATE

        val endpointUrl = _uiState.value.siteLink.requireHttpPrefix()
            .requirePostfix("/")
        viewModelScope.launch {
            try {
                val site = httpClient.verifySite(endpointUrl, json = json)
                val args = mutableMapOf(
                    UstadView.ARG_LEARNINGSPACE_URL to endpointUrl,
                    ARG_SITE to json.encodeToString(site),
                )
                ARGS_TO_PASS_THROUGH.forEach { argName ->
                    args.putFromSavedStateIfPresent(argName)
                }

                loadingState = LoadingUiState.NOT_LOADING
                _uiState.update { previous ->
                    previous.copy(validLink =  true, linkError = null, fieldsEnabled = true)
                }

                navController.navigate(LoginViewModel.DEST_NAME, args)
            }catch(e: Throwable) {
                Napier.d(throwable = e) { "SiteEnterLink: not working: $endpointUrl" }
                _uiState.update { previous ->
                    loadingState = LoadingUiState.NOT_LOADING
                    previous.copy(
                        validLink = false,
                        fieldsEnabled = true,
                        linkError = impl.getString(MR.strings.invalid_link)
                    )
                }
            }
        }
    }

    fun onSiteLinkUpdated(siteLink: String) {
        _uiState.update { previous ->
            previous.copy(siteLink = siteLink)
        }
        savedStateHandle[KEY_LINK] = siteLink
    }

    companion object {

        const val DEST_NAME = "SiteEnterLink"

        val ARGS_TO_PASS_THROUGH = listOf(
            ARG_NEXT, UstadView.ARG_INTENT_MESSAGE, ARG_DONT_SET_CURRENT_SESSION,
        )

        val KEY_LINK = "stateUrl"

    }

}
