package com.ustadmobile.libuicompose.view.discussionpost.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditUiState
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditViewModel
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.libuicompose.components.UstadErrorText
import com.ustadmobile.libuicompose.components.UstadInputFieldLayout
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@Composable
fun DiscussionPostEditScreenForViewModel(viewModel: DiscussionPostEditViewModel) {
    val uiState: DiscussionPostEditUiState by viewModel.uiState.collectAsState(
        DiscussionPostEditUiState()
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
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp).fillMaxWidth()
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

        // TODO error
//        UstadEditableHtmlField(
//            html = uiState.discussionPost?.discussionPostMessage ?: "",
//            onHtmlChange = {
//                onDiscussionPostBodyChanged(it)
//            },
//            modifier = Modifier
//                .weight(1f, fill = true)
//                .fillMaxWidth()
//        )

        uiState.discussionPostDescError?.also { descError ->
            UstadErrorText(error = descError)
        }

    }

}