package com.ustadmobile.libuicompose.view.pdfcontent

import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.pdfcontent.PdfContentViewModel

@Composable
expect fun PdfContentScreen(
    viewModel: PdfContentViewModel
)