package com.ustadmobile.libuicompose.view.videocontent

import android.os.Build
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.media3.datasource.okhttp.OkHttpDataSource
import com.ustadmobile.core.viewmodel.videocontent.VideoContentUiState
import com.ustadmobile.core.viewmodel.videocontent.VideoContentViewModel
import io.sanghun.compose.video.ResizeMode
import io.sanghun.compose.video.VideoPlayer
import io.sanghun.compose.video.uri.VideoPlayerMediaItem
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
    VideoContentScreen(uiState)
}

@Composable
fun VideoContentScreen(
    uiState: VideoContentUiState
) {
    //val mediaSrc = uiState.mediaSrc
    val mediaSrc = uiState.mediaDataUrl
    val mimeType = uiState.mediaMimeType
    val di = localDI()
    val okHttpClient : OkHttpClient = di.direct.instance()
    val mediaDataSource = remember {
        OkHttpDataSource.Factory(okHttpClient)
    }

    if(Build.VERSION.SDK_INT >= 23) {
        if(mediaSrc != null && mimeType != null) {
            VideoPlayer(
                modifier = Modifier.fillMaxWidth(),
                mediaItems = listOf(
                    VideoPlayerMediaItem.NetworkMediaItem(
                        url = mediaSrc,
                        mimeType = mimeType,
                    )
                ),
                resizeMode = ResizeMode.FIXED_WIDTH,
                httpDataSourceFactory = mediaDataSource,
            )
        }
    }else {
        Text("Sorry, video player requires Android 6+")
    }

}


