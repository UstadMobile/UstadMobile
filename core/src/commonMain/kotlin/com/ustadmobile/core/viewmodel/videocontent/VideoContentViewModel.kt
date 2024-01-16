package com.ustadmobile.core.viewmodel.videocontent

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.media.MediaContentInfo
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.url.UrlKmp
import com.ustadmobile.core.util.requireBodyUrlForUri
import com.ustadmobile.core.util.requireEntryByUri
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.lib.db.entities.ContentEntry
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
 * @param mediaSrc the uri within the ContentEntry as per ContentManifestEntry.uri . This will be
 *        served with the expected mime type by the content endpoint api.
 * @param mediaDataUrl the url for the media as per ContentEntryManifest.bodyDataUrl . This might
 *        not have headers exactly as per the ContentEntryManifest. This seems to be fine for
 *        ExoPlayer.
 */
data class VideoContentUiState(
    val mediaContentInfo: MediaContentInfo? = null,

    val mediaSrc: String? = null,

    val mediaDataUrl: String? = null,

    val mediaMimeType: String? = null,

    val contentEntry: ContentEntry? = null,
)

class VideoContentViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val entityUidArg: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val _uiState = MutableStateFlow(VideoContentUiState())

    val uiState: Flow<VideoContentUiState> = _uiState.asStateFlow()

    private val httpClient: HttpClient by instance()

    init {
        viewModelScope.launch {
            val contentEntryVersion = activeRepo.contentEntryVersionDao
                .findByUidAsync(entityUidArg) ?: return@launch

            launch {
                val manifest: ContentManifest = httpClient.get(contentEntryVersion.cevManifestUrl!!)
                    .body()

                val mediaInfoUrl = manifest.requireBodyUrlForUri(contentEntryVersion.cevOpenUri!!)

                val mediaInfo: MediaContentInfo = httpClient.get(mediaInfoUrl).body()

                val videoEntry = manifest.requireEntryByUri(
                    mediaInfo.sources.first().uri)

                val mediaSrc = UrlKmp(contentEntryVersion.cevManifestUrl!!).resolve(
                    mediaInfo.sources.first().uri).toString()
                val dataUrl = videoEntry.bodyDataUrl
                val mediaMimeType = mediaInfo.sources.first().mimeType

                _uiState.update { prev ->
                    prev.copy(
                        mediaContentInfo = mediaInfo,
                        mediaDataUrl = dataUrl,
                        mediaSrc = mediaSrc,
                        mediaMimeType = mediaMimeType,
                    )
                }
            }

            launch {
                val contentEntry = activeRepo.contentEntryDao.findByUidAsync(
                    contentEntryVersion.cevContentEntryUid)
                _uiState.update { prev ->
                    prev.copy(contentEntry = contentEntry)
                }

                _appUiState.update { prev ->
                    prev.copy(
                        title = contentEntry?.title ?: ""
                    )
                }
            }

        }
    }

    companion object {

        const val DEST_NAME = "Video"

    }
}