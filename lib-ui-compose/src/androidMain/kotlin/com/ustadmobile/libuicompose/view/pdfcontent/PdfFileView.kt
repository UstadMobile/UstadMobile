package com.ustadmobile.libuicompose.view.pdfcontent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rajat.pdfviewer.compose.PdfRendererViewCompose
import java.io.File

@Composable
actual fun PdfFileComponent(
    pdfFile: File,
    modifier: Modifier,
) {
    PdfRendererViewCompose(
        modifier = modifier,
        file = pdfFile
    )
}
