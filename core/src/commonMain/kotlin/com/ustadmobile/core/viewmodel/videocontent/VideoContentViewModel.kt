package com.ustadmobile.core.viewmodel.videocontent

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.media.MediaContentInfo
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.model.XAPI_RESULT_EXTENSION_PROGRESS
import com.ustadmobile.core.domain.xapi.model.XapiActivity
import com.ustadmobile.core.domain.xapi.model.XapiActivityStatementObject
import com.ustadmobile.core.domain.xapi.model.XapiResult
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.model.XapiVerb
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnClearUseCase
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnUnloadUseCase
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.url.UrlKmp
import com.ustadmobile.core.util.ext.bodyAsDecodedText
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.util.requireBodyUrlForUri
import com.ustadmobile.core.util.requireEntryByUri
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentEntry
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.atomicfu.update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * @param mediaSrc the url to the endpoint using the content api e.g.
 *        http://endpoint-server.com/api/content/contentEntryVersionUid/video . This is guaranteed
 *        to serve the request with the expected mime type.
 *
 * @param mediaDataUrl the url for the media as per ContentEntryManifest.bodyDataUrl . This might
 *        not have headers exactly as per the ContentEntryManifest. This seems to be fine for
 *        ExoPlayer.
 */
data class VideoContentUiState(
    val mediaContentInfo: MediaContentInfo? = null,

    val contentEntryVersionUid: Long = 0,

    val endpoint: Endpoint? = null,

    val mediaSrc: String? = null,

    val mediaDataUrl: String? = null,

    val mediaMimeType: String? = null,

    val contentEntry: ContentEntry? = null,

    val isFullScreen: Boolean = false,
) {
    /**
     * The path of the media item within the content
     */
    val firstMediaUri: String?
        get() = mediaContentInfo?.sources?.firstOrNull()?.uri
}

class VideoContentViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

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

    private val playDurationMs = atomic(0L)

    private val maxProgress = atomic(0)

    private val xapiStatementResource: XapiStatementResource by di.onActiveEndpoint().instance()

    private val contentEntryUid = savedStateHandle[ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0

    private val xapiSession = createXapiSession(contentEntryUid = contentEntryUid)

    private val saveStatementOnClearUseCase: SaveStatementOnClearUseCase? by di.onActiveEndpoint()
        .instanceOrNull()

    private val saveStatementOnUnloadUseCase: SaveStatementOnUnloadUseCase? by di.onActiveEndpoint()
        .instanceOrNull()

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true
            )
        }

        viewModelScope.launch {
            val contentEntryVersion = activeRepo.contentEntryVersionDao
                .findByUidAsync(entityUidArg) ?: return@launch

            launch {
                val manifest: ContentManifest = json.decodeFromString(
                    httpClient.get(contentEntryVersion.cevManifestUrl!!).bodyAsDecodedText()
                )

                val mediaInfoUrl = manifest.requireBodyUrlForUri(contentEntryVersion.cevOpenUri!!)

                val mediaInfo: MediaContentInfo = json.decodeFromString(
                    httpClient.get(mediaInfoUrl).bodyAsDecodedText())

                val videoEntry = manifest.requireEntryByUri(
                    mediaInfo.sources.first().uri)

                val mediaSrc = UrlKmp(contentEntryVersion.cevManifestUrl!!).resolve(
                    mediaInfo.sources.first().uri).toString()
                val dataUrl = videoEntry.bodyDataUrl
                val mediaMimeType = mediaInfo.sources.first().mimeType

                _uiState.update { prev ->
                    prev.copy(
                        contentEntryVersionUid = entityUidArg,
                        endpoint = accountManager.activeEndpoint,
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
                        title = contentEntry?.title ?: "",
                    )
                }
            }

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
        if(prevState.resumed) {
            val timeElapsed = systemTimeInMillis() - prevState.timestamp
            playDurationMs.update { it + timeElapsed }
            maxProgress.update { maxOf(it, playState.progressPercent) }
        }
    }

    private fun createXapiStatement(
        totalDuration: Long,
        progress: Int,
        isComplete: Boolean?,
    ): XapiStatement {
        val contentEntryVal = _uiState.value.contentEntry
        return XapiStatement(
            actor = xapiSession.agent,
            verb = XapiVerb(
                id = "http://adlnet.gov/expapi/verbs/completed"
            ),
            `object` = XapiActivityStatementObject(
                id = xapiSession.rootActivityId!!, //This is set in init
                definition = XapiActivity(
                    name = if(contentEntryVal != null) {
                        mapOf("en" to (contentEntryVal.title ?: "")) //TODO: set the locale as per ContentEntry(Version)
                    }else {
                        null
                    }
                )
            ),
            result = XapiResult(
                completion = isComplete,
                duration = totalDuration.milliseconds.toIsoString(),
                extensions = mapOf(
                    XAPI_RESULT_EXTENSION_PROGRESS to JsonPrimitive(progress)
                )
            )
        )

    }

    fun onComplete() {
        val recordDuration = playDurationMs.getAndUpdate { 0 }

        viewModelScope.launch {
            xapiStatementResource.put(
                statement = createXapiStatement(recordDuration, 100, true),
                statementIdParam = uuid4().toString(),
                xapiSession = xapiSession
            )
        }
    }

    internal fun onClear() = onCleared()

    fun SaveStatementOnClearUseCase.saveProgressStatement() {
        val playDurationVal = playDurationMs.value
        val maxProgressVal = maxProgress.value

        if(playDurationVal > 0 || maxProgressVal > 0) {
            //isComplete is false if we hit this e.g. avoid recording completion because 99.5 would
            //be rounded up to 100. Completion is recorded by onComplete only
            this.invoke(
                statements = listOf(
                    createXapiStatement(playDurationVal, maxProgressVal, false)
                ),
                xapiSession = xapiSession,
            )
        }
    }

    override fun onCleared() {
        Napier.d { "VideoContentViewModel: onCleared" }
        saveStatementOnClearUseCase?.saveProgressStatement()

        super.onCleared()
    }

    fun onUnload() {
        saveStatementOnUnloadUseCase?.saveProgressStatement()
    }

    companion object {

        const val DEST_NAME = "Video"

    }
}