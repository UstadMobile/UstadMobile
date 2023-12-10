package com.ustadmobile.core.viewmodel.site.edit

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.viewmodel.site.detail.SiteDetailViewModel

data class SiteEditUiState(
    val site: Site? = null,
    val siteTerms: List<SiteTermsWithLanguage> = emptyList(),
    val fieldsEnabled: Boolean = true,
    val siteNameError: String? = null
) {
    val hasErrors: Boolean = siteNameError != null
}

class SiteEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(SiteEditUiState())

    val uiState: Flow<SiteEditUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                title = systemImpl.getString(MR.strings.edit_site)
            )
        }

        viewModelScope.launch {
            loadEntity(
                serializer = Site.serializer(),
                onLoadFromDb = { db ->
                    db.siteDao.getSiteAsync()
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

            _appUiState.update { prev ->
                prev.copy(
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = systemImpl.getString(MR.strings.save),
                        onClick = this@SiteEditViewModel::onClickSave
                    )
                )
            }
        }
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

        if(_uiState.value.hasErrors)
            return

        viewModelScope.launch {
            activeRepo.siteDao.updateAsync(siteToSave)

            finishWithResult(
                detailViewName = SiteDetailViewModel.DEST_NAME,
                entityUid = siteToSave.siteUid,
                result = siteToSave
            )
        }

    }

    companion object {

        const val DEST_NAME = "SiteEdit"

    }

}