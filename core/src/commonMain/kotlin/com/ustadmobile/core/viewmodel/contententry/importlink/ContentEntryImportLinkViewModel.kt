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
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
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

/**
 * ContentEntryImportLink can be used in navigation paths as follows:
 *
 * 1) User is adding to library folder: arg_next will be set to
 *    ContentEntryEdit, and popup to on finish will be set to
 *    ContentEntryImportLinkViewModel.DEST_NAME so that when the user
 *    finishes saving content, the import link screen is popped off the stack.
 * 2) User is adding to course: the expected result destination and key will be set, s
 *    so the normal navigate for result logic will apply.
 * 3) User is changing updating an existing content entry: again, the expected result
 *    destination and key will be set and normal result logic will apply.
 */
class ContentEntryImportLinkViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ContentEntryImportLinkUiState())

    val uiState: Flow<ContentEntryImportLinkUiState> = _uiState.asStateFlow()

    private val httpClient: HttpClient by instance()

    private var commitLinkToSavedStateJob: Job? = null

    private val nextDest = savedStateHandle[ARG_NEXT]

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
                    url(accountManager.activeLearningSpace.url + "api/import/validateLink")
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

                val metadataResult: MetadataResult = json.decodeFromString(response.bodyAsText())

                when (nextDest) {
                    ContentEntryEditViewModel.DEST_NAME -> {
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
                                putFromSavedStateIfPresent(ContentEntryEditViewModel.ARG_COURSEBLOCK)
                                putFromSavedStateIfPresent(ContentEntryEditViewModel.ARG_GO_TO_ON_CONTENT_ENTRY_DONE)
                                putFromSavedStateIfPresent(UstadView.ARG_RESULT_DEST_VIEWNAME)
                                putFromSavedStateIfPresent(UstadView.ARG_RESULT_DEST_KEY)
                                putFromSavedStateIfPresent(ARG_PARENT_UID)
                                put(ARG_POPUPTO_ON_FINISH, DEST_NAME)
                            }
                        )
                    }
                    else -> {
                        finishWithResult(metadataResult)
                    }
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

