package com.ustadmobile.core.viewmodel.contententry.importlink

import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.util.ext.putFromSavedStateIfPresent
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.instance

data class ContentEntryImportLinkUiState(
    val url: String = "",
    val linkError: String? = null,
    val fieldsEnabled: Boolean = true,
)

class ContentEntryImportLinkViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ContentEntryImportLinkUiState())

    val uiState: Flow<ContentEntryImportLinkUiState> = _uiState.asStateFlow()

    private val httpClient: HttpClient by instance()

    private var commitLinkToSavedStateJob: Job? = null

    init {
        _appUiState.value = AppUiState(
            title = systemImpl.getString(MR.strings.activity_import_link),
            actionBarButtonState = ActionBarButtonUiState(
                visible = true,
                text = systemImpl.getString(MR.strings.next),
                onClick = this::onClickNext
            )
        )
        _uiState.update { prev ->
            prev.copy(
                url = savedStateHandle[STATE_KEY_IMPORTURL] ?: ""
            )
        }
    }

    fun onChangeLink(url: String) {
        _uiState.update { prev ->
            prev.copy(
                url = url,
                linkError = if(url != prev.url) null else prev.linkError,
            )
        }
        commitLinkToSavedStateJob?.cancel()
        commitLinkToSavedStateJob = viewModelScope.launch {
            delay(200)
            savedStateHandle[STATE_KEY_IMPORTURL] = url
        }
    }

    fun onClickNext() {
        if(!_uiState.value.fieldsEnabled) {
            return
        }

        if(_uiState.value.url.isBlank()) {
            Napier.d("link is blank")
            _uiState.update { prev ->
                prev.copy(
                    linkError = systemImpl.getString(MR.strings.field_required_prompt)
                )
            }
            return
        }

        _uiState.update { prev ->
            prev.copy(fieldsEnabled = false)
        }

        commitLinkToSavedStateJob?.cancel()
        savedStateHandle[STATE_KEY_IMPORTURL] = _uiState.value.url

        viewModelScope.launch {
            try {
                val response = httpClient.post {
                    url(accountManager.activeEndpoint.url + "import/validateLink")
                    parameter("url", _uiState.value.url)
                    expectSuccess = false
                }

                if(response.status != HttpStatusCode.OK) {
                    _uiState.update { prev ->
                        prev.copy(
                            linkError = systemImpl.getString(MR.strings.invalid_link),
                            fieldsEnabled = true,
                        )
                    }
                    return@launch
                }

                val metadataResult: MetadataResult = response.body()

                if(expectedResultDest != null) {
                    finishWithResult(metadataResult)
                }else {
                    //go to ContentEntryEdit
                    navController.navigate(
                        viewName = ContentEntryEditViewModel.DEST_NAME,
                        args = buildMap {
                            put(
                                key = ContentEntryEditViewModel.ARG_IMPORTED_METADATA,
                                value = json.encodeToString(
                                    serializer = MetadataResult.serializer(),
                                    value = metadataResult
                                )
                            )
                            putFromSavedStateIfPresent(savedStateHandle,
                                ContentEntryEditViewModel.ARG_COURSEBLOCK)
                        }
                    )
                }
            }catch(e: Exception) {
                snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.error) + ": $e"))
            }finally {
                _uiState.update { prev ->
                    prev.copy(fieldsEnabled = true)
                }
            }
        }


    }

    companion object {

        const val DEST_NAME = "ContentEntryImportLink"

        private const val STATE_KEY_IMPORTURL = "importUrl"

    }
}

