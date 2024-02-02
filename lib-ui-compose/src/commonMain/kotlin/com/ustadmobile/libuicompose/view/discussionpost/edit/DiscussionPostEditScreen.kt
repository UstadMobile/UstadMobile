package com.ustadmobile.libuicompose.view.discussionpost.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditUiState
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditViewModel
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadErrorText
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadRichTextEdit
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.HideSoftInputEffect
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
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize(),
    )  {

        OutlinedTextField(
            value = uiState.discussionPost?.discussionPostTitle ?: "",
            modifier = Modifier.testTag("discussion_post_title").fillMaxWidth().defaultItemPadding(),
            label = { Text(stringResource(MR.strings.title) + "*") },
            isError = uiState.discussionPostTitleError != null,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentChanged(uiState.discussionPost?.shallowCopy {
                    discussionPostTitle = it
                })
            },
            supportingText = {
                Text(uiState.discussionPostTitleError ?: stringResource(MR.strings.required))
            }
        )

        uiState.discussionPostDescError?.also { descError ->
            UstadErrorText(
                modifier = Modifier.defaultItemPadding(),
                error = descError
            )
        }

        UstadRichTextEdit(
            modifier = Modifier.fillMaxSize().testTag("discussion_post_body"),
            html = uiState.discussionPost?.discussionPostMessage ?: "",
            onHtmlChange = onDiscussionPostBodyChanged,
            editInNewScreen = false,
            onClickToEditInNewScreen = { },
            placeholderText = stringResource(MR.strings.compose_post),
        )
        HideSoftInputEffect()
    }
}