package com.ustadmobile.libuicompose.view.htmledit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.HtmlEditUiState
import com.ustadmobile.core.viewmodel.HtmlEditViewModel
import com.ustadmobile.libuicompose.components.UstadHtmlEditPlaceholder
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun HtmlEditScreen(
    viewModel: HtmlEditViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        HtmlEditUiState(), Dispatchers.Main.immediate
    )

    HtmlEditScreen(
        uiState = uiState,
        onChangeHtml = viewModel::onHtmlChanged
    )
}

@Composable
fun HtmlEditScreen(
    uiState: HtmlEditUiState,
    onChangeHtml: (String) -> Unit,
) {
    UstadHtmlEditPlaceholder(
        htmlTextTmp = uiState.html,
        onChangeHtmlTmp = onChangeHtml,
        editInNewScreenTmp = false,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    )
}