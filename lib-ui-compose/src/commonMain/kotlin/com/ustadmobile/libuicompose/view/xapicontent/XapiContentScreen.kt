package com.ustadmobile.libuicompose.view.xapicontent

import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentUiState

@Composable
expect fun XapiContentScreen(
    uiState: XapiContentUiState
)