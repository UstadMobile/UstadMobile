@file:Suppress("UnusedImport")

package com.ustadmobile.libuicompose.view.videocontent

import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource

//This import is not being used directly. It is here to ensure that the AV1 decoder is included (e.g.
// compilation would fail). The encoder itself will be autoloaded by setting EXTENSION_RENDERER_MODE_ON
// on the ExoPlayer builder.
import androidx.media3.decoder.av1.Libgav1VideoRenderer
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.ustadmobile.core.viewmodel.videocontent.VideoContentUiState
import com.ustadmobile.core.viewmodel.videocontent.VideoContentViewModel
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
    )
}

/**
 * See
 * https://medium.com/@munbonecci/how-to-display-videos-using-exoplayer-on-android-with-jetpack-compose-1fb4d57778f4
 *
 * Full screen - can use
 * https://developer.android.com/develop/ui/views/layout/immersive
 *
 * Click listener can be set using
 *
 * https://developer.android.com/reference/androidx/media3/ui/PlayerView#setFullscreenButtonClickListener(androidx.media3.ui.PlayerView.FullscreenButtonClickListener)
 *
 * ... which should make the button appear.
 */
@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerView(
    mediaSrc: String,
    onSetFullScreen: (Boolean) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val di = localDI()
    val okHttpClient : OkHttpClient = di.direct.instance()
    val mediaDataSource = remember {
        OkHttpDataSource.Factory(okHttpClient)
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
    }

    val mediaSource = remember(mediaSrc) {
        MediaItem.fromUri(Uri.parse(mediaSrc))
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
) {
    //val mediaSrc = uiState.mediaSrc
    val mediaSrc = uiState.mediaDataUrl
    val mimeType = uiState.mediaMimeType


    if(mediaSrc != null && mimeType != null) {
        ExoPlayerView(
            mediaSrc  = mediaSrc,
            modifier = Modifier.fillMaxWidth().let {
                if(uiState.isFullScreen) it.fillMaxHeight() else it.height(200.dp)
            },
            onSetFullScreen = onSetFullScreen,
        )
    }
}


