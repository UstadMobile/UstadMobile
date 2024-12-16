package com.ustadmobile.libuicompose.view.downloaduploadstatus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListUiState
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import kotlinx.coroutines.flow.Flow


@Composable
fun DownloadUploadStatusScreen(
    viewModel: DownloadUploadStatusViewModel
) {
   // val uiState by viewModel.uiState.collectAsState(DownloadUploadStatusUiState())

    DownloadUploadStatusScreen(
      //  uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow,
    )
}
@Composable
fun DownloadUploadStatusScreen(
   // uiState: ContentEntryDetailAttemptsPersonListUiState,
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
) {

}