@file:Suppress("UnusedImport")

package com.ustadmobile.libuicompose.view.videocontent

import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource

//This import is not being used directly. It is here to ensure that the AV1 decoder is included (e.g.
// compilation would fail). The encoder itself will be autoloaded by setting EXTENSION_RENDERER_MODE_ON
// on the ExoPlayer builder.
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.ustadmobile.core.contentformats.media.MediaContentInfo
import com.ustadmobile.core.domain.contententry.ContentManifestMap
import com.ustadmobile.core.viewmodel.videocontent.VideoContentUiState
import com.ustadmobile.core.viewmodel.videocontent.VideoContentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import okhttp3.OkHttpClient
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * See https://github.com/dsa28s/compose-video
 */
@Composable
actual fun VideoContentScreen(
    viewModel: VideoContentViewModel,
) {
    val uiState by viewModel.uiState.collectAsState(VideoContentUiState())
    VideoContentScreen(
        uiState = uiState,
        onSetFullScreen = viewModel::onSetFullScreen,
        onPlayStateChanged = viewModel::onPlayStateChanged,
        onComplete = viewModel::onComplete,
    )
}

/**
 *
 * Could sideload extra subtitles as per :
 * https://developer.android.com/media/media3/exoplayer/media-items#sideloading-subtitle-tracks
 */
@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerView(
    contentManifestMap: ContentManifestMap,
    mediaContentInfo: MediaContentInfo,
    onSetFullScreen: (Boolean) -> Unit,
    onPlayStateChanged: (VideoContentViewModel.MediaPlayState) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier,
) {
    fun ExoPlayer.mediaState()= VideoContentViewModel.MediaPlayState(
        timeInMillis = contentPosition,
        totalDuration = duration,
        resumed = isPlaying,
    )

    val context = LocalContext.current
    val di = localDI()
    val okHttpClient : OkHttpClient = di.direct.instance()
    val mediaDataSource = remember {
        OkHttpDataSource.Factory(okHttpClient)
    }

    var isPlayingVar by remember {
        mutableStateOf(false)
    }

    val fullScreenListener: PlayerView.FullscreenButtonClickListener = remember(onSetFullScreen) {
        PlayerView.FullscreenButtonClickListener {
            onSetFullScreen(it)
        }
    }

    val exoPlayer = remember {
        //Libgav1VideoRenderer
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(mediaDataSource))
            .setRenderersFactory(DefaultRenderersFactory(context)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON))
            .build()
            .also { player ->
                player.addListener(
                    object: Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            //Fire event
                            isPlayingVar = isPlaying
                            onPlayStateChanged(player.mediaState())
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if(playbackState == Player.STATE_ENDED)
                                onComplete()
                        }
                    }
                )
            }
    }

    LaunchedEffect(isPlayingVar) {
        if(isPlayingVar) {
            while(isActive) {
                delay(200)
                onPlayStateChanged(exoPlayer.mediaState())
            }
        }
    }

    val firstMediaSrc = mediaContentInfo.sources.firstOrNull()
    val mediaDataUrl = remember(contentManifestMap, firstMediaSrc) {
        firstMediaSrc?.let {
            contentManifestMap[it.uri]?.bodyDataUrl
        }
    }

    val subtitleConfigs = remember(mediaDataUrl) {
        mediaContentInfo.subtitles.mapNotNull { subtitle ->
            val bodyDataUrl = contentManifestMap[subtitle.uri]?.bodyDataUrl
            if (bodyDataUrl != null) {
                SubtitleConfiguration.Builder(Uri.parse(bodyDataUrl))
                    .setMimeType(subtitle.mimeType)
                    .also {
                        subtitle.langCode?.also { lang -> it.setLanguage(lang) }
                    }
                    .setLabel(subtitle.title)
                    .build()
            } else {
                Log.w(
                    "VideoContentScreen",
                    "Could not find body data url for subtitle: ${subtitle.uri}"
                )
                null
            }
        }
    }

    val mediaSource = remember(mediaDataUrl) {
        MediaItem.Builder()
            .setUri(mediaDataUrl)
            .apply {
                if(subtitleConfigs.isNotEmpty()) {
                    setSubtitleConfigurations(subtitleConfigs)
                }
            }
            .build()
    }

    LaunchedEffect(mediaSource) {
        exoPlayer.setMediaItem(mediaSource)
        exoPlayer.prepare()
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                setShowSubtitleButton(subtitleConfigs.isNotEmpty())
                setFullscreenButtonClickListener(fullScreenListener)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun VideoContentScreen(
    uiState: VideoContentUiState,
    onSetFullScreen: (Boolean) -> Unit = { },
    onPlayStateChanged: (VideoContentViewModel.MediaPlayState) -> Unit = { },
    onComplete: () -> Unit,
) {
    val mediaContentMap = uiState.contentManifestMap
    val mediaContentInfo = uiState.mediaContentInfo

    if(mediaContentMap != null && mediaContentInfo != null) {
        ExoPlayerView(
            contentManifestMap = mediaContentMap,
            mediaContentInfo = mediaContentInfo,
            modifier = Modifier.fillMaxWidth().let {
                if(uiState.isFullScreen) it.fillMaxHeight() else it.height(200.dp)
            },
            onSetFullScreen = onSetFullScreen,
            onPlayStateChanged = onPlayStateChanged,
            onComplete = onComplete,
        )
    }
}


