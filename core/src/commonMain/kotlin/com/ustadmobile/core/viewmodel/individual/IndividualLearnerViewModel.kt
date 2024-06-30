package com.ustadmobile.core.viewmodel.individual

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.backup.UnzipFileUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.appendSelectedAccount
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance


data class IndividualLearnerUiState(
    val selectedFileUri: String? = null,
    val selectedFileName: String? = null,
    val extractionStatus: ExtractionStatus = ExtractionStatus.Idle,
    val extractionProgress: Float = 0f
)

enum class ExtractionStatus {
    Idle, Extracting, Completed, Error
}


class IndividualLearnerViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, LoginViewModel.DEST_NAME) {

    private val impl: UstadMobileSystemImpl by instance()
    private var nextDestination: String =
        savedStateHandle[UstadView.ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME

    private val unzipFileUseCase: UnzipFileUseCase by instance()

    private val _uiState = MutableStateFlow(IndividualLearnerUiState())
    val uiState: StateFlow<IndividualLearnerUiState> = _uiState.asStateFlow()
    init {
        _appUiState.update { prev ->
            prev.copy(
                title = impl.getString(MR.strings.individual_action_title),
                userAccountIconVisible = false,
                navigationVisible = false,
                hideBottomNavigation = true,
            )
        }
    }
    fun onRestoreFileSelected(fileUri: String, fileName: String) {
        _uiState.update {
            it.copy(
                selectedFileUri = fileUri,
                selectedFileName = fileName,
                extractionStatus = ExtractionStatus.Extracting,
                extractionProgress = 0f
            )
        }
        extractZipFile(fileUri)
    }

    private fun extractZipFile(fileUri: String) {
        viewModelScope.launch {
            try {
                val outputDirectory = "/storage/emulated/0/"
                println("Output Directory: $outputDirectory")

                unzipFileUseCase(fileUri, outputDirectory).collect { progress ->
                    println("Extraction Progress: ${progress.progress}")
                    _uiState.update {
                        it.copy(
                            extractionProgress = progress.progress,
                            extractionStatus = ExtractionStatus.Extracting
                        )
                    }
                }
                _uiState.update { it.copy(extractionStatus = ExtractionStatus.Completed) }
                println("Extraction Completed Successfully")
            } catch (e: Exception) {
                println("Extraction Failed: ${e.message}")
                e.printStackTrace()
                _uiState.update { it.copy(extractionStatus = ExtractionStatus.Error) }
            }
        }
    }    private fun goToNextDestAfterLoginOrGuestSelected(personUid: Long, endpoint: Endpoint) {
        val goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
        navController.navigateToViewUri(
            nextDestination.appendSelectedAccount(
                personUid, Endpoint(
                    endpoint.toString()
                )
            ), goOptions
        )
    }

    fun onClickContinueWithoutLogin() {
        loadingState = LoadingUiState.INDETERMINATE

        viewModelScope.launch {
            accountManager.createLocalAccount()
            val goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
            navController.navigate(ContentEntryListViewModel.DEST_NAME_HOME, emptyMap(),goOptions)

        }
    }

    companion object {
        const val DEST_NAME = "IndividualLearner"
    }
}