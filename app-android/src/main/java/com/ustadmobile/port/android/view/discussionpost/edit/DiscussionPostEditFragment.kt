package com.ustadmobile.port.android.view.discussionpost.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditUiState
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditViewModel
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.AztecEditor
import com.ustadmobile.port.android.view.composable.UstadErrorText
import com.ustadmobile.port.android.view.composable.UstadInputFieldLayout
import com.ustadmobile.core.R as CR

class DiscussionPostEditFragment: UstadBaseMvvmFragment(){

    private val viewModel: DiscussionPostEditViewModel by ustadViewModels(::DiscussionPostEditViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    DiscussionPostEditFragmentScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun DiscussionPostEditFragmentScreen(viewModel: DiscussionPostEditViewModel) {
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
@Preview
fun DiscussionPostEditFragmentPreview(){
    val uiStateVal = DiscussionPostEditUiState(
        discussionPost = DiscussionPost().apply {
            discussionPostTitle = "How do I upload my homework?"
            discussionPostMessage= "Hi everyone, how do I finish and upload my homework to this moduel? Thanks! "

        }

    )

    MdcTheme {
        DiscussionPostEditScreen(
            uiState = uiStateVal
        )
    }
}


@Composable
private fun DiscussionPostEditScreen(
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
                label = { Text(stringResource(id = CR.string.title)) },
                isError = uiState.discussionPostTitleError != null,
                enabled = uiState.fieldsEnabled,
                onValueChange = {
                    onContentChanged(uiState.discussionPost?.shallowCopy {
                        discussionPostTitle = it
                    })
                }
            )
        }

        AztecEditor(
            html = uiState.discussionPost?.discussionPostMessage ?: "",
            onChange = {
                onDiscussionPostBodyChanged(it)
            },
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth()
        )

        uiState.discussionPostDescError?.also { descError ->
            UstadErrorText(error = descError)
        }

    }

}