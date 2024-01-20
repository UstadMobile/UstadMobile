package com.ustadmobile.libuicompose.view.pdfcontent

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toFile
import com.rajat.pdfviewer.compose.PdfRendererViewCompose
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.viewmodel.pdfcontent.PdfContentUiState
import com.ustadmobile.core.viewmodel.pdfcontent.PdfContentViewModel
import com.ustadmobile.libuicompose.components.UstadDownloadUrlStatus
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 *
 */
@Composable
actual fun PdfContentScreen(
    viewModel: PdfContentViewModel
) {
    val uiState: PdfContentUiState by viewModel.uiState.collectAsState(PdfContentUiState())
    PdfContentScreen(uiState)
}

@Composable
fun PdfContentScreen(
    uiState: PdfContentUiState
) {
    val di = localDI()

    val getStoragePath: GetStoragePathForUrlUseCase = remember {
        di.direct.instance()
    }

    var cacheDownloadState: GetStoragePathForUrlUseCase.GetStoragePathForUrlState by remember {
        mutableStateOf(GetStoragePathForUrlUseCase.GetStoragePathForUrlState())
    }

    LaunchedEffect(uiState.dataUrl) {
        val url = uiState.dataUrl ?: return@LaunchedEffect
        getStoragePath(
            url = url,
            onStateChange = {
                cacheDownloadState = it
            },
        )
    }
    val fileUri = cacheDownloadState.fileUri
    val file = remember(fileUri) {
        fileUri?.let { Uri.parse(it) }?.toFile()
    }

    when {
        file != null -> {
            PdfRendererViewCompose(
                modifier = Modifier.fillMaxSize(),
                file = file
            )
        }
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                UstadDownloadUrlStatus(
                    modifier = Modifier.fillMaxWidth(),
                    state  = cacheDownloadState,
                )
            }
        }
    }
}