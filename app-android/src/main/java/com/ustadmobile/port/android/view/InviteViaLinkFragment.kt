package com.ustadmobile.port.android.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.themeadapter.appcompat.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentInviteViaLinkBinding
import com.ustadmobile.core.controller.InviteViaLinkPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.InviteViaLinkView
import com.ustadmobile.core.viewmodel.InviteViaLinkUiState


interface InvitationLinkHandler{
    fun handleClickCopyLink(link: String)
    fun handleClickShareLink(link: String)
    fun handleClickCopyCode(code: String)
}
class InviteViaLinkFragment: UstadBaseFragment(), InviteViaLinkView, InvitationLinkHandler {

    override var inviteLink: String? = null
        set(value) {
            mBinding?.link = value
            field = value
        }
    override var inviteCode: String? = null
        set(value) {
            mBinding?.code = value
            field = value
        }

    override var entityName: String? = null
        set(value) {
            mBinding?.entityName = value
            field = value
        }

    private var mBinding: FragmentInviteViaLinkBinding? = null

    private var mPresenter: InviteViaLinkPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mBinding = FragmentInviteViaLinkBinding.inflate(inflater, container,
                false).also {
        }
        mBinding?.activityEventHandler = this

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    InviteViaLinkScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ustadFragmentTitle = requireContext().getString(R.string.invite_with_link)
        mPresenter = InviteViaLinkPresenter(requireContext(), arguments.toStringMap(), this,
            di).withViewLifecycle()
        mPresenter?.onCreate(null)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    override fun handleClickCopyLink(link: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE)
                as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData(ClipData.newPlainText("link", link)))
        showSnackBar(requireContext().getString(R.string.copied_to_clipboard))
    }

    override fun handleClickCopyCode(code: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE)
                as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData(ClipData.newPlainText("link", code)))
        showSnackBar(requireContext().getString(R.string.copied_to_clipboard))
    }

    override fun handleClickShareLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, link)
        startActivity(Intent.createChooser(intent, getString(R.string.share_link)))
    }


}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun InviteViaLinkScreen(
    uiState: InviteViaLinkUiState = InviteViaLinkUiState(), 
    onClickCopyLink: () -> Unit = {},
    onClickShareLink: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )  {

        Text(text = String.format(
            stringResource(id = R.string.invite_link_desc),
            uiState.entityName)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            onClick = onClickCopyLink
        ) {
            Row{

                Image(
                    painter = painterResource(id = R.drawable.ic_insert_link_black_24dp),
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(text = uiState.inviteLink ?: "")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Divider(thickness = 1.dp)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onClickCopyLink,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(stringResource(R.string.copy_link).uppercase())
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onClickShareLink,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(stringResource(R.string.copy_link).uppercase())
        }
    }
}

@Composable
@Preview
fun  InviteViaLinkScreenPreview() {
    val uiStateVal = InviteViaLinkUiState(
        entityName = stringResource(id = R.string.invite_link_desc),
        inviteLink = "http://wwww.ustadmobile.com/ClazzJoin?code=12ASDncd",
    )
    
    MdcTheme {
        InviteViaLinkScreen(
            uiState = uiStateVal
        )
    }
}