package com.ustadmobile.libuicompose.view.pdfcontent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.io.File

@Composable
expect fun PdfFileComponent(
    pdfFile: File,
    onActiveChanged: (Boolean) -> Unit,
    onProgressed: (Int) -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
)
