package com.ustadmobile.libuicompose.view.htmledit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.HtmlEditUiState
import com.ustadmobile.core.viewmodel.HtmlEditViewModel
import com.ustadmobile.libuicompose.components.UstadRichTextEdit
import com.ustadmobile.libuicompose.util.HideSoftInputEffect
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

    UstadRichTextEdit(
        modifier = Modifier.fillMaxSize(),
        html = uiState.html,
        onHtmlChange = onChangeHtml,
        editInNewScreen = false,
        onClickToEditInNewScreen = { }
    )

    HideSoftInputEffect()
}