package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
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
import com.ustadmobile.port.android.view.composable.UstadTextEditField

class DiscussionPostEditFragment: UstadBaseMvvmFragment(){

    private val viewModel: DiscussionPostEditViewModel by ustadViewModels(::DiscussionPostEditViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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
        onContentChanged = viewModel::onEntityChanged
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
){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    )  {

        UstadTextEditField(
            value = uiState.discussionPost?.discussionPostTitle ?: "",
            label = stringResource(id = R.string.title),
            error = uiState.discussionPostTitleError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentChanged(uiState.discussionPost?.shallowCopy {
                    discussionPostTitle = it
                })
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        UstadTextEditField(
            value = uiState.discussionPost?.discussionPostMessage ?: "",
            label = stringResource(id = R.string.message),
            error = uiState.discussionPostDescError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onContentChanged(uiState.discussionPost?.shallowCopy {
                    discussionPostMessage = it
                    })
            }
        )

    }

}