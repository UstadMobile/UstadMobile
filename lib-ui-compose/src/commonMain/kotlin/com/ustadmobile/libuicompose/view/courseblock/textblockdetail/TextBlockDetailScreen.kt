package com.ustadmobile.libuicompose.view.courseblock.textblockdetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.courseblock.textblockdetail.TextBlockDetailUiState
import com.ustadmobile.core.viewmodel.courseblock.textblockdetail.TextBlockDetailViewModel
import com.ustadmobile.libuicompose.components.UstadHtmlText
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun TextBlockDetailScreen(
    viewModel: TextBlockDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        TextBlockDetailUiState())

    TextBlockDetailScreen(uiState)
}

@Composable
fun TextBlockDetailScreen(
    uiState: TextBlockDetailUiState
) {
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        UstadHtmlText(
            modifier = Modifier.padding(16.dp),
            html = uiState.courseBlock?.cbDescription ?: ""
        )
    }
}

