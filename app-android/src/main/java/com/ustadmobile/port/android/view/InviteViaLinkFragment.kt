package com.ustadmobile.port.android.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentInviteViaLinkBinding
import com.ustadmobile.core.controller.InviteViaLinkPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.InviteViaLinkView


interface InvitationLinkHandler{
    fun handleClickCopyLink(link: String)
    fun handleClickShareLink(link: String)
}
class InviteViaLinkFragment: UstadBaseFragment(), InviteViaLinkView, InvitationLinkHandler {

    override var inviteLink: String? = null
        set(value) {
            mBinding?.link = value
            field = value
        }

    override var entityName: String? = null
        set(value) {
            mBinding?.entityName = value
            field = value
        }

    private var mBinding: FragmentInviteViaLinkBinding? = null

    private var mPresenter: InviteViaLinkPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentInviteViaLinkBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ustadFragmentTitle = requireContext().getString(R.string.join_existing,
                requireContext().getString(R.string.clazz))
        mPresenter = InviteViaLinkPresenter(requireContext(), arguments.toStringMap(), this,
            di)
        mPresenter?.onCreate(null)
    }


    override fun onDestroyView() {
        super.onDestroyView()

        mBinding = null
    }

    override fun handleClickCopyLink(link: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData(ClipData.newPlainText("link", link)))
        showSnackBar(requireContext().getString(R.string.copied_to_clipboard))
    }

    override fun handleClickShareLink(link: String) {
        //TODO
    }


}