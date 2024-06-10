package com.ustadmobile.libuicompose.view.pdfcontent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.rajat.pdfviewer.PdfRendererView
import com.rajat.pdfviewer.compose.PdfRendererViewCompose
import com.ustadmobile.libuicompose.components.LifecycleActiveEffect
import java.io.File

@Composable
actual fun PdfFileComponent(
    pdfFile: File,
    onActiveChanged: (Boolean) -> Unit,
    onProgressed: (Int) -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier,
) {
    LifecycleActiveEffect(onActiveChanged)

    PdfRendererViewCompose(
        modifier = modifier,
        file = pdfFile,
        statusCallBack = remember {
            object : PdfRendererView.StatusCallBack {
                override fun onPageChanged(currentPage: Int, totalPage: Int) {
                    if(totalPage == 0)
                        return

                    if(currentPage == (totalPage -1)) {
                        onCompleted()
                    }else {
                        onProgressed(((currentPage + 1) * 100) / totalPage)
                    }
                }
            }
        }
    )
}
