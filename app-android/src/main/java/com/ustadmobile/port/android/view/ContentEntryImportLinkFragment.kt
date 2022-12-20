package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentEntryImportLinkBinding
import com.ustadmobile.core.controller.ContentEntryImportLinkPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryImportLinkView
import com.ustadmobile.core.viewmodel.ContentEntryImportLinkUiState
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadTextEditField
import io.ktor.http.*

class ContentEntryImportLinkFragment : UstadBaseFragment(), ContentEntryImportLinkView {

    private var mBinding: FragmentEntryImportLinkBinding? = null

    private var mPresenter: ContentEntryImportLinkPresenter? = null

    override var inProgress: Boolean
        get() = mBinding?.inProgress ?: false
        set(value) {
            loading = value
            mBinding?.inProgress = value
        }


    override var validLink: Boolean = false
        set(value) {
            mBinding?.entryImportLinkTextInput?.isErrorEnabled = value
            mBinding?.entryImportLinkTextInput?.error = if(value) null else getString(R.string.invalid_link)
            field = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentEntryImportLinkBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.entryImportLinkEditText.setOnEditorActionListener { v, actionId, event ->
                val importLinkVal = it.importLink
                if(actionId == EditorInfo.IME_ACTION_GO && importLinkVal != null){
                    mPresenter?.handleClickDone(importLinkVal)
                    true
                }else {
                    false
                }
            }
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ustadFragmentTitle = getString(R.string.enter_url)
        setHasOptionsMenu(true)

        mPresenter = ContentEntryImportLinkPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di).withViewLifecycle()
        mBinding?.mPresenter = mPresenter
        mPresenter?.onCreate(savedInstanceState.toStringMap())
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_done -> {
                mPresenter?.handleClickDone(mBinding?.entryImportLinkEditText?.text.toString())
                return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        mBinding = null

    }


}

@Composable
fun ContentEntryImportLinkScreen(
    uiState: ContentEntryImportLinkUiState,
    onClickNext: () -> Unit = {},
    onUrlChange: (Url?) -> Unit = {}
){
    Column (
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        UstadTextEditField(
            value = uiState.url.toString(),
            label = stringResource(id = R.string.enter_url),
            error = uiState.linkError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onUrlChange(Url(it))
            }
        )

        Text(
            stringResource(R.string.supported_link),
            style = Typography.body2,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        )

        Button(
            onClick = onClickNext,
            enabled = uiState.fieldsEnabled,
            modifier = Modifier
                .padding(top = 26.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.secondaryColor)
            )
        ) {
            Text(
                stringResource(R.string.next).uppercase(),
                color = contentColorFor(
                    colorResource(id = R.color.secondaryColor)
                ),
                modifier = Modifier
                    .padding(8.dp)
            )
        }

    }
}

@Composable
@Preview
fun ContentEntryImportLinkScreenPreview(){
    ContentEntryImportLinkScreen(
        uiState = ContentEntryImportLinkUiState(
            url = Url("this is a link")
        )
    )
}