package com.ustadmobile.libuicompose.view.xapicontent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentUiState
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentViewModel

@Composable
fun XapiContentScreen(
    viewModel: XapiContentViewModel
) {
    val uiState by viewModel.uiState.collectAsState(XapiContentUiState())
    XapiContentScreen(uiState)
}
