package com.ustadmobile.core.viewmodel.site.edit

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.lib.db.entities.Site
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.core.util.ext.replace
import com.ustadmobile.core.viewmodel.site.detail.SiteDetailViewModel
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.instance

data class SiteEditUiState(
    val site: Site? = null,
    val siteTerms: List<SiteTerms> = emptyList(),
    val uiLangs: List<UstadMobileSystemCommon.UiLanguage> =
        listOf(UstadMobileSystemCommon.UiLanguage("en", "English")),
    val fieldsEnabled: Boolean = true,
    val siteNameError: String? = null,
    val registrationEnabledError: String? = null,
    val currentSiteTermsLang: UstadMobileSystemCommon.UiLanguage = uiLangs.first(),
) {
    val hasErrors: Boolean = (siteNameError != null || registrationEnabledError != null)

    val currentSiteTerms: SiteTerms?
        get() = siteTerms.firstOrNull { it.sTermsLang == currentSiteTermsLang.langCode }

    val currentSiteTermsHtml: String?
        get() = currentSiteTerms?.termsHtml

}

class SiteEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(SiteEditUiState())

    val uiState: Flow<SiteEditUiState> = _uiState.asStateFlow()

    private val languagesConfig: SupportedLanguagesConfig by instance()

    private var saveTermsHtmlJob: Job? = null

    init {
        val supportedLangs = languagesConfig.supportedUiLanguages

        val supportedLangCodes = supportedLangs.map { it.langCode }

        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                title = systemImpl.getString(MR.strings.edit_site)
            )
        }

        _uiState.update {  prev ->
            prev.copy(
                uiLangs = supportedLangs,
                currentSiteTermsLang = supportedLangs.first {
                    it.langCode == (savedStateHandle[KEY_SITE_TERMS_LANG] ?: languagesConfig.displayedLocale)
                }
            )
        }

        launchIfHasPermission(
            setLoadingState = true,
            onSetFieldsEnabled = { enabled ->
                _uiState.update { it.copy(fieldsEnabled = enabled) }
            },
            permissionCheck = { db ->
                db.systemPermissionDao().personHasSystemPermission(
                    activeUserPersonUid, PermissionFlags.MANAGE_SITE_SETTINGS
                )
            }
        ) {
            loadEntity(
                serializer = Site.serializer(),
                onLoadFromDb = { db ->
                    db.siteDao().getSiteAsync()
                },
                makeDefault = {
                    Site()
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(
                            site = it
                        )
                    }
                }
            )

            loadEntity(
                serializer = ListSerializer(SiteTerms.serializer()),
                loadFromStateKeys = listOf(KEY_SITE_TERMS_LIST),
                savedStateKey = KEY_SITE_TERMS_LIST,
                onLoadFromDb = {
                    val termsLoadedFromDb = it.siteTermsDao().findAllTerms(1).filter {
                        it.sTermsLang in supportedLangCodes
                    }
                    val langsLoadedFromDb = termsLoadedFromDb.mapNotNull { it.sTermsLang }

                    buildList {
                        addAll(termsLoadedFromDb)

                        //Add a SiteTerms object for all those that were not loaded from the database.
                        addAll(supportedLangCodes.filter { it !in langsLoadedFromDb }.map { langCode ->
                            SiteTerms().apply {
                                sTermsLang = langCode
                            }
                        })
                    }
                },
                makeDefault = {
                    null
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(
                            siteTerms = it ?: emptyList()
                        )
                    }
                }
            )

            _appUiState.update { prev ->
                prev.copy(
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = systemImpl.getString(MR.strings.save),
                        onClick = this@SiteEditViewModel::onClickSave
                    )
                )
            }

            launch {
                navResultReturner.filteredResultFlowForKey(RESULT_KEY_TERMS_HTML).collect {
                    val newTerms = it.result as? String ?: return@collect
                    onChangeTermsHtml(newTerms)
                }
            }
        }
    }

    fun onChangeTermsLanguage(
        uiLang: UstadMobileSystemCommon.UiLanguage
    ) {
        savedStateHandle[KEY_SITE_TERMS_LANG] = uiLang.langCode
        _uiState.update { prev ->
            prev.copy(
                currentSiteTermsLang = uiLang,
            )
        }
    }

    fun onChangeTermsHtml(
        html: String
    ) {
        val currentSiteTerms = _uiState.value.currentSiteTerms ?: return

        val newTerms = _uiState.updateAndGet { prev ->
            prev.copy(
                siteTerms = prev.siteTerms.replace(
                    element = currentSiteTerms.shallowCopy {
                        this.termsHtml = html
                    },
                    replacePredicate = {
                        it.sTermsLang == prev.currentSiteTermsLang.langCode
                    },
                ),
                registrationEnabledError = null,
            )
        }.siteTerms

        saveTermsHtmlJob?.cancel()
        saveTermsHtmlJob = viewModelScope.launch {
            delay(200)
            savedStateHandle.setJson(
                key = KEY_SITE_TERMS_LIST,
                serializer = ListSerializer(SiteTerms.serializer()),
                value = newTerms
            )
        }
    }

    fun onClickEditTermsInNewScreen() {
        navigateToEditHtml(
            currentValue = _uiState.value.currentSiteTermsHtml ?: "",
            resultKey = RESULT_KEY_TERMS_HTML,
            title = systemImpl.getString(MR.strings.terms_and_policies)
        )
    }


    fun onEntityChanged(entity: Site?) {
        _uiState.update { prev ->
            prev.copy(
                site = entity,
                siteNameError = updateErrorMessageOnChange(prev.site?.siteName,
                    entity?.siteName, prev.siteNameError)
            )
        }
        scheduleEntityCommitToSavedState(
            entity = entity,
            serializer = Site.serializer(),
            commitDelay = 200,
        )
    }

    fun onClickSave() {
        val siteToSave = _uiState.value.site ?: return
        if(siteToSave.siteName.isNullOrBlank()) {
            _uiState.update { prev ->
                prev.copy(
                    siteNameError = systemImpl.getString(MR.strings.required)
                )
            }
        }

        val siteTermsPlainText = _uiState.value.siteTerms.map {
            (it.sTermsLang ?: "") to (it.termsHtml?.htmlToPlainText() ?: "")
        }.toMap()

        if(siteToSave.registrationAllowed && siteTermsPlainText.all { it.value.isBlank() }) {
            _uiState.update { prev ->
                prev.copy(
                    registrationEnabledError = systemImpl.getString(MR.strings.terms_required_if_registration_enabled)
                )
            }
        }


        if(_uiState.value.hasErrors)
            return

        viewModelScope.launch {
            activeRepoWithFallback.siteDao().updateAsync(siteToSave)
            activeRepoWithFallback.siteTermsDao().upsertList(
                _uiState.value.siteTerms.filter {
                    (!it.termsHtml?.htmlToPlainText().isNullOrBlank() || it.sTermsUid != 0L)
                }.map {
                    it.shallowCopy {
                        sTermsActive = !it.termsHtml?.htmlToPlainText().isNullOrBlank()
                    }
                }
            )

            finishWithResult(
                detailViewName = SiteDetailViewModel.DEST_NAME,
                entityUid = siteToSave.siteUid,
                result = siteToSave
            )
        }

    }

    companion object {

        const val DEST_NAME = "SiteEdit"

        const val KEY_SITE_TERMS_LANG = "siteTermsLang"

        const val KEY_SITE_TERMS_LIST = "termsList"

        const val RESULT_KEY_TERMS_HTML = "siteTermsHtml"

    }

}