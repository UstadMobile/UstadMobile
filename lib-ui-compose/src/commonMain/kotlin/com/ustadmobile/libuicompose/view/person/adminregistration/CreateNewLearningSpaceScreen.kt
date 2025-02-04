package com.ustadmobile.libuicompose.view.person.adminregistration


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.ustadmobile.core.viewmodel.person.adminregistration.CreateNewLearningSpaceUiState
import com.ustadmobile.core.viewmodel.person.adminregistration.CreateNewLearningSpaceViewModel
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun CreateNewLearningSpaceScreen(viewModel: CreateNewLearningSpaceViewModel) {
    val uiState: CreateNewLearningSpaceUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initial = CreateNewLearningSpaceUiState(), context = Dispatchers.Main.immediate
    )

    CreateNewLearningSpaceScreen(
        uiState = uiState,
        onCreateLearningSpace = viewModel::createLearningSpace,
        onFieldValueChange = viewModel::onFieldValueChange
    )
}

@Composable
fun CreateNewLearningSpaceScreen(
    uiState: CreateNewLearningSpaceUiState,
    onCreateLearningSpace: () -> Unit = {},
    onFieldValueChange: (String, String) -> Unit = { _, _ -> }
) {

}
