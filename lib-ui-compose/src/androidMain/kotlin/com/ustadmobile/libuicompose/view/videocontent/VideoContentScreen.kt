package com.ustadmobile.libuicompose.view.videocontent

import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.videocontent.VideoContentUiState
import com.ustadmobile.core.viewmodel.videocontent.VideoContentViewModel
import io.sanghun.compose.video.VideoPlayer
import io.sanghun.compose.video.uri.VideoPlayerMediaItem

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
    val mediaSrc = uiState.mediaSrc
    val mimeType = uiState.mediaMimeType
    if(Build.VERSION.SDK_INT >= 23) {
        if(mediaSrc != null && mimeType != null) {
            VideoPlayer(
                modifier = Modifier.fillMaxSize(),
                mediaItems = listOf(
                    VideoPlayerMediaItem.NetworkMediaItem(
                        url = mediaSrc,
                    )
                ),

                playerInstance = {


                }
            )
        }
    }else {
        Text("Sorry, video player requires Android 6+")
    }

}


