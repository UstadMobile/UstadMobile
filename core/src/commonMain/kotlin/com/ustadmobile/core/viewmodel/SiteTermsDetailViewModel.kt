package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.SiteTermsDetailView.Companion.ARG_SHOW_ACCEPT_BUTTON
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.lib.db.entities.SiteTerms
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import com.ustadmobile.core.MR
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.UstadView.Companion.ARG_API_URL
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.direct
import org.kodein.di.on

data class SiteTermsDetailUiState(

    val siteTerms: SiteTerms? = null,

    val acceptButtonVisible: Boolean = false,

    val error: String? = null,

)

class SiteTermsDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): DetailViewModel<SiteTerms>(di, savedStateHandle, DEST_NAME) {

    private val supportedLangConfig: SupportedLanguagesConfig by instance()

    private val _uiState = MutableStateFlow(SiteTermsDetailUiState())

    val uiState: Flow<SiteTermsDetailUiState> = _uiState.asStateFlow()

    init {
        val acceptButtonMode = savedStateHandle[ARG_SHOW_ACCEPT_BUTTON]?.toBoolean() ?: false
        val apiUrl = savedStateHandle[ARG_API_URL]

        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = !acceptButtonMode,
                userAccountIconVisible = !acceptButtonMode,
                title = systemImpl.getString(MR.strings.terms_and_policies)
            )
        }

        viewModelScope.launch {
            val repo: UmAppDatabase = if(acceptButtonMode && apiUrl != null) {
                di.direct.on(Endpoint(apiUrl)).instance(tag = DoorTag.TAG_REPO)
            }else {
                activeRepo
            }


            val localeArg = savedStateHandle[ARG_LOCALE]
            val termsLocale = if(localeArg != null) {
                localeArg
            }else {
                val preferredLocales =  listOf(supportedLangConfig.displayedLocale) +
                        supportedLangConfig.systemLocales
                val availableLocales = repo.siteTermsDao
                    .findAvailableSiteTermLanguages(1).filterNotNull()
                preferredLocales.firstOrNull { it in availableLocales } ?: FALLBACK
            }

            val displayTerms = repo.siteTermsDao.findLatestByLanguage(termsLocale)

            if(displayTerms != null) {
                _uiState.update { prev ->
                    prev.copy(
                        acceptButtonVisible = acceptButtonMode,
                        siteTerms = displayTerms
                    )
                }
            }else {
                _uiState.update { prev ->
                    prev.copy(
                        error = systemImpl.getString(MR.strings.login_network_error)
                    )
                }
            }

        }
    }

    fun onClickAccept() {
        navController.navigate(
            PersonEditViewModel.DEST_NAME_REGISTER,
            args = buildMap {
                putFromSavedStateIfPresent(PersonEditViewModel.REGISTRATION_ARGS_TO_PASS)
            }
        )
    }


    companion object {

        const val ARG_LOCALE = "locale"

        const val DEST_NAME = "Terms"

        const val FALLBACK = "en"

    }
}
