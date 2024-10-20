package com.ustadmobile.libuicompose.view.contententry.subtitleedit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.contentformats.media.SubtitleTrack
import com.ustadmobile.core.viewmodel.contententry.subtitleedit.SubtitleEditUiState
import com.ustadmobile.core.viewmodel.contententry.subtitleedit.SubtitleEditViewModel
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import kotlinx.coroutines.Dispatchers
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SubtitleEditScreen(
    viewModel: SubtitleEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState(SubtitleEditUiState(), Dispatchers.Main.immediate)

    SubtitleEditScreen(
        uiState = uiState,
        onEntityChanged = viewModel::onEntityChanged,
    )
}

@Composable
fun SubtitleEditScreen(
    uiState: SubtitleEditUiState,
    onEntityChanged: (SubtitleTrack?) -> Unit = { },
) {
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("title"),
            value = uiState.entity?.title ?: "",
            onValueChange = {
                onEntityChanged(
                    uiState.entity?.copy(
                        title = it
                    )
                )
            },
            label = { Text(stringResource(MR.strings.title)) }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("language"),
            value = uiState.entity?.langCode ?: "",
            label = { Text(stringResource(MR.strings.language)) },
            onValueChange = {
                onEntityChanged(
                    uiState.entity?.copy(
                        langCode = it,
                    )
                )
            }
        )
    }
}
