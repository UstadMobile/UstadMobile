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
import com.ustadmobile.core.viewmodel.contententry.edit.ContentEntryEditViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode
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

    init {
        _appUiState.value = AppUiState(
            title = systemImpl.getString(MR.strings.activity_import_link),
            actionBarButtonState = ActionBarButtonUiState(
                visible = true,
                text = systemImpl.getString(MR.strings.next),
                onClick = this::onClickNext
            )
        )
    }

    fun onChangeLink(url: String) {
        _uiState.update { prev ->
            prev.copy(
                url = url,
                linkError = if(url != prev.url) null else prev.linkError,
            )
        }
    }

    fun onClickNext() {
        if(!_uiState.value.fieldsEnabled) {
            return
        }

        _uiState.update { prev ->
            prev.copy(fieldsEnabled = false)
        }

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
                        args = mapOf(
                            ContentEntryEditViewModel.ARG_IMPORTED_METADATA to json.encodeToString(
                                serializer = MetadataResult.serializer(),
                                value = metadataResult
                            )
                        )
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

    }
}

