package com.ustadmobile.core.viewmodel.site.detail

import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.lib.db.composites.SiteTermsAndLangName
import com.ustadmobile.lib.db.entities.Site
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.viewmodel.site.termsdetail.SiteTermsDetailViewModel
import com.ustadmobile.core.viewmodel.site.edit.SiteEditViewModel
import kotlinx.coroutines.flow.combine

data class SiteDetailUiState(

    val site: Site? = null,
    val siteTerms: List<SiteTermsAndLangName> = emptyList()

)

class SiteDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
)  : DetailViewModel<Site>(di, savedStateHandle, DEST_NAME) {


    private val _uiState = MutableStateFlow(SiteDetailUiState())

    val uiState: Flow<SiteDetailUiState> = _uiState.asStateFlow()

    init {
        val supportLangConfig: SupportedLanguagesConfig = direct.instance()
        val uiLangList = supportLangConfig.supportedUiLanguages

        _appUiState.update { prev ->
            prev.copy(
                fabState = FabUiState(
                    text = systemImpl.getString(MR.strings.edit),
                    onClick = this::onClickEdit,
                    icon = FabUiState.FabIcon.EDIT,
                    visible = false,
                ),
                title = systemImpl.getString(MR.strings.site),
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                val siteFlow =activeRepoWithFallback.siteDao().getSiteAsFlow()
                val permissionFlow = activeRepoWithFallback.systemPermissionDao().personHasSystemPermissionAsFlow(
                    activeUserPersonUid, PermissionFlags.MANAGE_SITE_SETTINGS
                )

                launch {
                    siteFlow.combine(permissionFlow) { site, hasAdminPermission ->
                        Pair(site, hasAdminPermission)
                    }.collect {
                        val (entity, hasEditSitePermission) = it
                        _uiState.update { prev ->
                            prev.copy(
                                site = entity,
                            )
                        }

                        _appUiState.update { prev ->
                            prev.copy(
                                fabState = prev.fabState.copy(
                                    visible = it.first != null && hasEditSitePermission
                                )
                            )
                        }
                    }
                }

                launch {
                    activeRepoWithFallback.siteTermsDao().findAllTermsAsListFlow(1).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                siteTerms = it.mapNotNull { siteTerms ->
                                    uiLangList.firstOrNull {
                                        it.langCode == siteTerms.sTermsLang
                                    }?.let { uiLang ->
                                        SiteTermsAndLangName(
                                            siteTerms, uiLang.langDisplay
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    fun onClickEdit() {
        navController.navigate(
            SiteEditViewModel.DEST_NAME,
            mapOf(ARG_ENTITY_UID to (_uiState.value.site?.siteUid?.toString() ?: "-1"))
        )
    }

    fun onClickTerms(termsAndLang: SiteTermsAndLangName) {
        navController.navigate(
            SiteTermsDetailViewModel.DEST_NAME,
            mapOf(SiteTermsDetailViewModel.ARG_LOCALE to (termsAndLang.terms.sTermsLang ?: ""))
        )
    }


    companion object {

        const val DEST_NAME = "Community"

    }
}




