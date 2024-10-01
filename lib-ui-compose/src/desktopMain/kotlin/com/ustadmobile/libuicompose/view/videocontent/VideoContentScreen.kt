package com.ustadmobile.libuicompose.view.videocontent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ustadmobile.core.domain.contententry.getlocalurlforcontent.GetLocalUrlForContentUseCase
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.videocontent.VideoContentUiState
import com.ustadmobile.core.viewmodel.videocontent.VideoContentViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import org.jetbrains.compose.videoplayer.VideoPlayer
import org.jetbrains.compose.videoplayer.rememberVideoPlayerState
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.time.Duration.Companion.seconds

/**
 * See https://github.com/caprica/vlcj
 */
@Composable
actual fun VideoContentScreen(
    viewModel: VideoContentViewModel,
) {

    val uiState by viewModel.uiState.collectAsState(VideoContentUiState())
    VideoContentScreen(
        uiState = uiState,
        onPlayStateChanged = viewModel::onPlayStateChanged,
        onCompleted = viewModel::onComplete,
    )
}

@Composable
fun VideoContentScreen(
    uiState: VideoContentUiState,
    onPlayStateChanged: (VideoContentViewModel.MediaPlayState) -> Unit,
    onCompleted: () ->  Unit,
) {
    val mediaSrc = uiState.firstMediaUri
    val endpoint = uiState.learningSpace

    VlcCheck {
        if(mediaSrc != null && endpoint != null) {
            val di = localDI()
            val getLocalUrlForContentUseCase: GetLocalUrlForContentUseCase = remember {
                di.onActiveEndpoint().direct.instance()
            }

            val url = remember(uiState.contentEntryVersionUid, mediaSrc) {
                getLocalUrlForContentUseCase(uiState.contentEntryVersionUid, mediaSrc)
            }

            val state = rememberVideoPlayerState()
            val progress by state.progress

            val progressVal = state.progress.value
            val totalTime = remember(progressVal.length) {
                if(progressVal.length > 0) {
                    (progressVal.length / 1000).seconds.toString()
                }else {
                    null
                }
            }
            val currentTime = remember(progressVal.timeMillis) {
                if(progressVal.timeMillis >= 0){
                    (progressVal.timeMillis / 1000).seconds.toString()
                }else {
                    null
                }
            }

            LaunchedEffect(state.isResumed, progress) {
                onPlayStateChanged(
                    VideoContentViewModel.MediaPlayState(
                        timestamp = systemTimeInMillis(),
                        timeInMillis = progress.timeMillis,
                        totalDuration = progress.length,
                        resumed = state.isResumed,
                    )
                )
            }

            Column {
                VideoPlayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = 0.8f),
                    url = url,
                    state = state,
                    onFinish = {
                        onCompleted()
                        state.stopPlayback()
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = state::toggleResume
                    ) {
                        Icon(
                            if(state.isResumed) Icons.Default.PauseCircleOutline else Icons.Default.PlayCircleOutline,
                            contentDescription = "play/pause - localize me"
                        )
                    }

                    Slider(
                        value = state.progress.value.fraction,
                        onValueChange = { state.seek = it },
                        modifier = Modifier.weight(1f)
                    )

                    if(totalTime != null && currentTime != null) {
                        Text(
                            "$currentTime / $totalTime",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                }
            }

        }
    }
}


