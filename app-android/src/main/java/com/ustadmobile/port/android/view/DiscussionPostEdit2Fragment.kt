package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentDiscussionPostEditBinding
import com.ustadmobile.core.controller.DiscussionPostEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.DiscussionPostEditView
import com.ustadmobile.core.viewmodel.DiscussionPostEditUiState
import com.ustadmobile.lib.db.entities.ContentEntryWithBlockAndLanguage
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.view.composable.UstadTextEditField

class DiscussionPostEdit2Fragment: UstadEditFragment<DiscussionPost>(),
    DiscussionPostEditView {


    private var mBinding: FragmentDiscussionPostEditBinding? = null

    private var mPresenter: DiscussionPostEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, DiscussionPost>?
        get() = mPresenter



    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView: View
        mBinding = FragmentDiscussionPostEditBinding.inflate(inflater, container,
            false).also {
            rootView = it.root
        }

        mPresenter = DiscussionPostEditPresenter(requireContext(),
            arguments.toStringMap(), this, di, viewLifecycleOwner).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.post, R.string.edit_topic)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var loading: Boolean = false

    override var entity: DiscussionPost? = null
        set(value) {
            field = value
            mBinding?.discussionPost = value
        }

    override var blockTitleError: String? = null
        set(value) {
            field = value
            mBinding?.blockTitleError = value
        }

    override var fieldsEnabled: Boolean = false
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
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
//                onContentChanged(uiState.discussionPost?.shallowCopy {
//                    discussionPostTitle = it
//                })
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        UstadTextEditField(
            value = uiState.discussionPost?.discussionPostMessage ?: "",
            label = stringResource(id = R.string.message),
            error = uiState.discussionPostDescError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
//                onContentChanged(uiState.entity?.shallowCopy {
//                    discussionPostMessage = it
//                    })
            }
        )

    }

}


@Composable
@Preview
fun DiscussionPostEditScreenPreview(){
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
