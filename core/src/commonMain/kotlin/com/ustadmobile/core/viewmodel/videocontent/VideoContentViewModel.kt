package com.ustadmobile.core.viewmodel.videocontent

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.media.MediaContentInfo
import com.ustadmobile.core.domain.contententry.ContentManifestMap
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.bodyAsDecodedText
import com.ustadmobile.core.util.requireBodyUrlForUri
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.noninteractivecontent.AbstractNonInteractiveContentViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentEntry
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

/**
 */
data class VideoContentUiState(
    val mediaContentInfo: MediaContentInfo? = null,

    val contentEntryVersionUid: Long = 0,

    val manifestUrl: String? = null,

    val contentManifestMap: ContentManifestMap? = null,

    val contentEntry: ContentEntry? = null,

    val isFullScreen: Boolean = false
)

class VideoContentViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): AbstractNonInteractiveContentViewModel(di, savedStateHandle, DEST_NAME) {

    /**
     * @param timestamp the system time (in millis) when video playing started
     * @param timeInMillis the position within the video (in milliseconds) the video was at when play started
     * @param totalDuration the total duration of the video (in milliseconds).
     */
    data class MediaPlayState(
        val timestamp: Long = systemTimeInMillis(),
        val timeInMillis: Long = 0,
        val totalDuration: Long = 0,
        val resumed: Boolean = false,
    ) {
        val progressPercent: Int
            get() = if(totalDuration > 0) {
                ((timeInMillis * 100) / totalDuration).toInt()
            }else {
                0
            }

    }

    private val _mediaPlayState = MutableStateFlow(MediaPlayState())

    private val entityUidArg: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val _uiState = MutableStateFlow(VideoContentUiState())

    val uiState: Flow<VideoContentUiState> = _uiState.asStateFlow()

    private val httpClient: HttpClient by instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true
            )
        }

        viewModelScope.launch {
            val contentEntryVersion = activeRepo.contentEntryVersionDao()
                .findByUidAsync(entityUidArg) ?: return@launch

            launch {
                val manifest: ContentManifest = json.decodeFromString(
                    httpClient.get(contentEntryVersion.cevManifestUrl!!).bodyAsDecodedText()
                )

                val mediaInfoUrl = manifest.requireBodyUrlForUri(contentEntryVersion.cevOpenUri!!)

                val mediaInfo: MediaContentInfo = json.decodeFromString(
                    httpClient.get(mediaInfoUrl).bodyAsDecodedText()
                )

                _uiState.update { prev ->
                    prev.copy(
                        contentEntryVersionUid = entityUidArg,
                        mediaContentInfo = mediaInfo,
                        manifestUrl = contentEntryVersion.cevManifestUrl!!,
                        contentManifestMap = ContentManifestMap(manifest),
                    )
                }
            }

            launch {
                val contentEntry = activeRepo.contentEntryDao().findByUidAsync(
                    contentEntryVersion.cevContentEntryUid)
                _uiState.update { prev ->
                    prev.copy(contentEntry = contentEntry)
                }

                _appUiState.update { prev ->
                    prev.copy(
                        title = contentEntry?.title ?: "",
                    )
                }
            }

        }
    }

    override val titleAndLangCode: TitleAndLangCode?
        get() {
            return _uiState.value.contentEntry?.title?.let {
                TitleAndLangCode(it, "en") //TODO: set language based on content entry
            }
        }

    fun onSetFullScreen(isFullScreen: Boolean) {
        _appUiState.update { it.copy(hideAppBar = isFullScreen) }
        _uiState.update { it.copy(isFullScreen = isFullScreen) }
    }

    /**
     * Called by the video player; allows the ViewModel to track playback duration and progress
     * for progress tracking purposes.
     */
    fun onPlayStateChanged(playState: MediaPlayState) {
        val prevState = _mediaPlayState.getAndUpdate { playState }
        if(prevState.resumed != playState.resumed) {
            onActiveChanged(playState.resumed)
        }

        onProgressed(playState.progressPercent)
    }

    internal fun onClear() = onCleared()


    companion object {

        const val DEST_NAME = "Video"

    }
}