package com.ustadmobile.core.viewmodel.appearance

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.file.UriFileUseCase
import com.ustadmobile.core.domain.theme.ThemeUploadUseCase
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.http.isSuccess
import io.ktor.utils.io.core.ByteReadPacket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

data class AppearanceEditUiState(
    val organisationName: String? = null,
    val organisationLogo: String? = null,
    val jetpackComposeTheme: String? = null,
    val muiTheme: String? = null,
    val fieldsEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showSuccessMessage: Boolean = false
)

class AppearanceEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String = DEST_NAME
) : UstadEditViewModel(di, savedStateHandle, destName) {

    private val _uiState: MutableStateFlow<AppearanceEditUiState> = MutableStateFlow(AppearanceEditUiState())
    val uiState = _uiState.asStateFlow()


    private val httpClient: HttpClient by instance()
    private val uriFileUseCase: UriFileUseCase by instance()

    init {
        loadingState = LoadingUiState.NOT_LOADING

        _appUiState.update {
            AppUiState(
                title = systemImpl.getString(MR.strings.appearance),
                hideBottomNavigation = false
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.save),
                    onClick = this@AppearanceEditViewModel::onClickSave
                )
            )
        }
    }

    fun onOrganisationNameChanged(name: String) {
        _uiState.update { prev ->
            prev.copy(
                organisationName = name,
                errorMessage = null,
                showSuccessMessage = false
            )
        }
    }

    fun onOrganisationLogoChanged(logoUri: String?) {
        _uiState.update { prev ->
            prev.copy(
                organisationLogo = logoUri,
                errorMessage = null,
                showSuccessMessage = false
            )
        }
    }

    fun onJetpackComposeThemeChanged(theme: String?) {
        _uiState.update { prev ->
            prev.copy(
                jetpackComposeTheme = theme,
                errorMessage = null,
                showSuccessMessage = false
            )
        }
    }

    fun onMuiThemeChanged(theme: String?) {
        _uiState.update { prev ->
            prev.copy(
                muiTheme = theme,
                errorMessage = null,
                showSuccessMessage = false
            )
        }
    }

    fun onClickSave() {
        viewModelScope.launch {
            try {
                loadingState = LoadingUiState.INDETERMINATE
                _uiState.update { it.copy(errorMessage = null) }

                val apiBaseUrl = accountManager.activeEndpoint.url

                val formParts = mutableListOf<PartData>()

                _uiState.value.organisationName?.let { orgName ->
                    formParts.add(
                        PartData.FormItem(
                            value = orgName,
                            dispose = {},
                            partHeaders = Headers.build {
                                append(HttpHeaders.ContentDisposition, "form-data; name=\"organisationName\"")
                            }
                        )
                    )
                }

                _uiState.value.organisationLogo?.let { logoUri ->
                    val uriFileData = uriFileUseCase(logoUri)

                    if (uriFileData.bytes != null) {
                        formParts.add(
                            PartData.FileItem(
                                provider = { ByteReadPacket(uriFileData.bytes) },
                                dispose = {},
                                partHeaders = Headers.build {
                                    append(HttpHeaders.ContentDisposition,
                                        "form-data; name=\"orgLogo\"; filename=\"${uriFileData.filename}\"")
                                    append(HttpHeaders.ContentType, uriFileData.mimeType)
                                }
                            )
                        )
                    } else {
                        formParts.add(
                            PartData.FormItem(logoUri, { "orgLogo" }, Headers.build {})
                        )
                    }
                }

                _uiState.value.jetpackComposeTheme?.let { themeUri ->
                    val themeName = themeUri.substringAfterLast("/").substringBefore(".")
                    formParts.add(
                        PartData.FormItem(
                            value = themeName,
                            dispose = {},
                            partHeaders = Headers.build {
                                append(HttpHeaders.ContentDisposition, "form-data; name=\"jetpackComposeThemeName\"")
                            }
                        )
                    )
                    val uriFileData = uriFileUseCase(themeUri)

                    if (uriFileData.bytes != null) {
                        formParts.add(
                            PartData.FileItem(
                                provider = { ByteReadPacket(uriFileData.bytes) },
                                dispose = {},
                                partHeaders = Headers.build {
                                    append(HttpHeaders.ContentDisposition,
                                        "form-data; name=\"jetpackComposeTheme\"; filename=\"${uriFileData.filename}\"")
                                    append(HttpHeaders.ContentType, uriFileData.mimeType)
                                }
                            )
                        )
                    } else {
                        formParts.add(
                            PartData.FormItem(themeUri, { "jetpackComposeTheme" }, Headers.build {})
                        )
                    }
                }

                _uiState.value.muiTheme?.let { themeUri ->
                    val themeName = themeUri.substringAfterLast("/").substringBefore(".")
                    formParts.add(
                        PartData.FormItem(
                            value = themeName,
                            dispose = {},
                            partHeaders = Headers.build {
                                append(HttpHeaders.ContentDisposition, "form-data; name=\"muiThemeName\"")
                            }
                        )
                    )
                    val uriFileData = uriFileUseCase(themeUri)

                    if (uriFileData.bytes != null) {
                        formParts.add(
                            PartData.FileItem(
                                provider = { ByteReadPacket(uriFileData.bytes) },
                                dispose = {},
                                partHeaders = Headers.build {
                                    append(HttpHeaders.ContentDisposition,
                                        "form-data; name=\"muiTheme\"; filename=\"${uriFileData.filename}\"")
                                    append(HttpHeaders.ContentType, uriFileData.mimeType)
                                }
                            )
                        )
                    } else {
                        formParts.add(
                            PartData.FormItem(themeUri, { "muiTheme" }, Headers.build {})
                        )
                    }
                }

                val multipartContent = MultiPartFormDataContent(formParts)

                val response = httpClient.post("${apiBaseUrl}api/theme/upload") {
                    setBody(multipartContent)
                }

                val result = response.body<ThemeUploadUseCase.ThemeUploadResponse>()

                if (response.status.isSuccess() && result.success) {
                    loadingState = LoadingUiState.NOT_LOADING
                    _uiState.update { it.copy(
                        showSuccessMessage = true,
                        errorMessage = null
                    )}
                } else {
                    loadingState = LoadingUiState.NOT_LOADING
                    _uiState.update { it.copy(
                        errorMessage = result.message
                    )}
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadingState = LoadingUiState.NOT_LOADING
                _uiState.update { it.copy(
                    errorMessage = e.message
                )}
            }
        }
    }

    companion object {
        const val DEST_NAME = "AppearanceEditView"
    }
}