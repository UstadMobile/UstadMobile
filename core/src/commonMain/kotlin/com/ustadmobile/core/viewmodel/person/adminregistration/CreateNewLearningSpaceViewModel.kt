package com.ustadmobile.core.viewmodel.person.adminregistration


import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance


data class CreateNewLearningSpaceUiState(
    val organisationLogo: String = "",
    val adminUsername: String = "",
    val organisationName: String = "",
    val adminContact: String = "",
    val password: String = "",
    val isNotARobot: Boolean = false,
    val fieldsEnabled: Boolean = true,
    val creationError: String? = null,
    val progressVisible: Boolean = false
)

class CreateNewLearningSpaceViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(CreateNewLearningSpaceUiState())
    val uiState: Flow<CreateNewLearningSpaceUiState> = _uiState.asStateFlow()

    private val httpClient: HttpClient by instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = "Create New Learning Space",
                userAccountIconVisible = false,
                navigationVisible = true
            )
        }
    }

    fun createLearningSpace() {
        _uiState.update { it.copy(fieldsEnabled = false) }

        val uiStateValue = _uiState.value
        val formData = mapOf(
            "organisationLogo" to uiStateValue.organisationLogo,
            "adminUsername" to uiStateValue.adminUsername,
            "organisationName" to uiStateValue.organisationName,
            "adminContact" to uiStateValue.adminContact,
            "password" to uiStateValue.password,
            "isNotARobot" to uiStateValue.isNotARobot
        )

        viewModelScope.launch {
            try {
//                loadingState = LoadingUiState.INDETERMINATE
//                val response = httpClient.createLearningSpace(formData)
//                if (response.isSuccessful) {
//                    loadingState = LoadingUiState.NOT_LOADING
//                    _uiState.update { it.copy(creationError = null, progressVisible = false) }
//                    navController.navigate(NextScreenViewModel.DEST_NAME)
//                } else {
//                    _uiState.update {
//                        it.copy(creationError = "Failed to create Learning Space", fieldsEnabled = true)
//                    }
//                }
            } catch (e: Throwable) {
                _uiState.update { it.copy(creationError = "An error occurred: ${e.message}", fieldsEnabled = true) }
            }
        }
    }

    fun onFieldValueChange(field: String, value: String) {
        _uiState.update {
            when (field) {
                "organisationLogo" -> it.copy(organisationLogo = value)
                "adminUsername" -> it.copy(adminUsername = value)
                "organisationName" -> it.copy(organisationName = value)
                "adminContact" -> it.copy(adminContact = value)
                "password" -> it.copy(password = value)
                "isNotARobot" -> it.copy(isNotARobot = value.toBoolean())
                else -> it
            }
        }
    }

    companion object {
        const val DEST_NAME = "CreateNewLearningSpace"
    }
}
