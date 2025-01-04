package com.ustadmobile.libuicompose.view.report.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun ReportDetailScreen(viewModel: ReportDetailViewModel) {
    val uiState: ReportDetailUiState by viewModel.uiState.collectAsStateWithLifecycle(
        ReportDetailUiState(), Dispatchers.Main.immediate
    )
    ReportDetailScreen(
        uiState = uiState,
    )
}

@Composable
fun ReportDetailScreen(uiState: ReportDetailUiState) {
    UstadVerticalScrollColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(text = "Detail Screen")
    }
}