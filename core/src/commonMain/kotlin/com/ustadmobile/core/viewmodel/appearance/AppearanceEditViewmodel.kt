package com.ustadmobile.core.viewmodel.appearance

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.theme.ThemeUploadUseCase
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.isSuccess
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
                println("🟢 Save button clicked - starting theme upload process")
                loadingState = LoadingUiState.INDETERMINATE
                _uiState.update { it.copy(errorMessage = null) }

                val apiBaseUrl = accountManager.activeEndpoint.url
                println("Using API base URL: $apiBaseUrl")

                println("Preparing form data with:")
                println("  - orgName: ${_uiState.value.organisationName}")
                println("  - orgLogo: ${_uiState.value.organisationLogo}")
                println("  - jetpackComposeTheme: ${_uiState.value.jetpackComposeTheme}")
                println("  - muiTheme: ${_uiState.value.muiTheme}")

                val formData = formData {
                    _uiState.value.organisationName?.let {
                        append("orgName", it)
                        println("  - Added orgName to form data")
                    }
                    _uiState.value.organisationLogo?.let {
                        append("orgLogo", it)
                        println("  - Added orgLogo to form data")
                    }
                    _uiState.value.jetpackComposeTheme?.let { themeUri ->
                        append("jetpackComposeThemeUri", themeUri)
                        println("  - Added jetpackComposeThemeUri to form data")
                    }
                    _uiState.value.muiTheme?.let { themeUri ->
                        append("muiThemeUri", themeUri)
                        println("  - Added muiThemeUri to form data")
                    }
                }

                println("Sending request to: ${apiBaseUrl}api/theme/upload")
                val response = httpClient.submitFormWithBinaryData(
                    url = "${apiBaseUrl}api/theme/upload",
                    formData = formData
                )

                println("Received response with status: ${response.status}")

                val result = response.body<ThemeUploadUseCase.ThemeUploadResponse>()
                println("Response body: success=${result.success}, message=${result.message}")

                if (response.status.isSuccess() && result.success) {
                    println("✅ Theme upload successful!")
                    loadingState = LoadingUiState.NOT_LOADING
                    _uiState.update { it.copy(
                        showSuccessMessage = true,
                        errorMessage = null
                    )}
                } else {
                    println("Theme upload failed: ${result.message}")
                    loadingState = LoadingUiState.NOT_LOADING
                    _uiState.update { it.copy(
                        errorMessage = result.message
                    )}
                }
            } catch (e: Exception) {
                println("Exception during theme upload: ${e.message}")
                e.printStackTrace()
                loadingState = LoadingUiState.NOT_LOADING
                _uiState.update { it.copy(
                    errorMessage = "Error: ${e.message}"
                )}
            }
        }
    }

    companion object {
        const val DEST_NAME = "AppearanceEditView"
    }
}