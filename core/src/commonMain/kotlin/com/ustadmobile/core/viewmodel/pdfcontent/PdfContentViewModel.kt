package com.ustadmobile.core.viewmodel.pdfcontent

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.url.UrlKmp
import com.ustadmobile.core.util.bodyDataUrlForUri
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * @param pdfUrl The URL to the PDF using the content endpoint e.g. /api/content/versionUid/content.pdf
 *        This should be used by the web client because it guarantees that the request will be
 *        served with the headers specified in the Manifest.
 *
 * @param dataUrl The URL to the to the ContentManifestEntry.bodyDataUrl . This should be used by
 *        Android/Desktop clients.
 */
data class PdfContentUiState(
    val pdfUrl: String? = null,
    val dataUrl: String? = null,
)

class PdfContentViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME){

    private val entityUidArg: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val _uiState = MutableStateFlow(PdfContentUiState())

    val uiState: Flow<PdfContentUiState> = _uiState.asStateFlow()

    private val httpClient: HttpClient by di.instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true
            )
        }

        viewModelScope.launch {
            val contentEntryVersion = activeRepo.contentEntryVersionDao
                .findByUidAsync(entityUidArg) ?: return@launch
            val manifestUrl = contentEntryVersion.cevManifestUrl!!
            val manifest: ContentManifest = httpClient.get(manifestUrl).body()

            val pdfEntryUri = contentEntryVersion.cevOpenUri ?: return@launch
            val pdfUrl = UrlKmp(manifestUrl).resolve(pdfEntryUri).toString()
            val dataUrl = manifest.bodyDataUrlForUri(pdfEntryUri)

            _uiState.update { prev ->
                prev.copy(
                    pdfUrl = pdfUrl,
                    dataUrl = dataUrl,
                )
            }

            val contentEntry = activeRepo.contentEntryDao.findByUidAsync(
                contentEntryVersion.cevContentEntryUid
            )
            _appUiState.update { prev ->
                prev.copy(title = contentEntry?.title ?: "")
            }
        }
    }

    companion object {

        const val DEST_NAME = "PdfContent"

    }
}