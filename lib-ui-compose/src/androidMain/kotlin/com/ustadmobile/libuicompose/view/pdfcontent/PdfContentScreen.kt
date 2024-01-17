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
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.VerticalPdfReaderState
import com.ustadmobile.core.viewmodel.pdfcontent.PdfContentUiState
import com.ustadmobile.core.viewmodel.pdfcontent.PdfContentViewModel
import com.ustadmobile.libuicompose.components.UstadDownloadUrlStatus
import com.ustadmobile.libuicompose.util.downloadurl.DownloadUrlState
import com.ustadmobile.libuicompose.util.downloadurl.downloadUrlViaCacheAndGetLocalUri
import io.ktor.client.HttpClient
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
    val httpClient: HttpClient = remember {
        di.direct.instance()
    }

    var cacheDownloadState: DownloadUrlState by remember {
        mutableStateOf(DownloadUrlState())
    }

    LaunchedEffect(uiState.dataUrl) {
        val url = uiState.dataUrl ?: return@LaunchedEffect
        downloadUrlViaCacheAndGetLocalUri(
            url = url,
            httpClient = httpClient,
            onStateChange = {
                cacheDownloadState = it
            }
        )
    }
    val fileUri = cacheDownloadState.fileUri

    when {
        fileUri != null -> {
            VerticalPDFReader(
                state = VerticalPdfReaderState(
                    resource = ResourceType.Local(Uri.parse(fileUri)),
                    isZoomEnable = true,
                ),
                modifier = Modifier.fillMaxSize()
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