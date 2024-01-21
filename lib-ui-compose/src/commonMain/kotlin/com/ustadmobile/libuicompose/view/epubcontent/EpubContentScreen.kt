package com.ustadmobile.libuicompose.view.epubcontent

import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentViewModel

@Composable
expect fun EpubContentScreen(
    viewModel: EpubContentViewModel
)
