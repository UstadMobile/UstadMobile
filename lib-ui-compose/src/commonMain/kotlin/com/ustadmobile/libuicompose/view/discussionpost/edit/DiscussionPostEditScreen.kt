package com.ustadmobile.libuicompose.view.discussionpost.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditUiState
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditViewModel
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadErrorText
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadHtmlEditPlaceholder
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun DiscussionPostEditScreen(viewModel: DiscussionPostEditViewModel) {
    val uiState: DiscussionPostEditUiState by viewModel.uiState.collectAsStateWithLifecycle(
        DiscussionPostEditUiState(), Dispatchers.Main.immediate
    )

    DiscussionPostEditScreen(
        uiState,
        onContentChanged = viewModel::onEntityChanged,
        onDiscussionPostBodyChanged = viewModel::onDiscussionPostBodyChanged,
    )
}

@Composable
fun DiscussionPostEditScreen(
    uiState: DiscussionPostEditUiState = DiscussionPostEditUiState(),
    onContentChanged: (DiscussionPost?) -> Unit = {},
    onDiscussionPostBodyChanged: (String) -> Unit = { },
){
    Column(
        modifier = Modifier.fillMaxSize(),
    )  {
        UstadInputFieldLayout(
            errorText = uiState.discussionPostTitleError,
            modifier = Modifier.defaultItemPadding().fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.discussionPost?.discussionPostTitle ?: "",
                modifier = Modifier.testTag("discussion_post_title").fillMaxWidth(),
                label = { Text(stringResource(MR.strings.title)) },
                isError = uiState.discussionPostTitleError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onContentChanged(uiState.discussionPost?.shallowCopy {
                        discussionPostTitle = it
                    })
                }
            )
        }

        UstadHtmlEditPlaceholder(
            htmlTextTmp = uiState.discussionPost?.discussionPostMessage ?: "",
            onChangeHtmlTmp = onDiscussionPostBodyChanged,
            editInNewScreenTmp = false,
            modifier = Modifier.defaultItemPadding().weight(1.0f).fillMaxWidth()
                .testTag("discussion_post_body"),
            isError = uiState.discussionPostDescError != null,

        )

        uiState.discussionPostDescError?.also { descError ->
            UstadErrorText(error = descError)
        }

    }

}