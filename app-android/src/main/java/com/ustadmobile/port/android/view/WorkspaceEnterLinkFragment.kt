package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentWorkSpaceEnterLinkBinding
import com.ustadmobile.core.controller.WorkspaceEnterLinkPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.WorkspaceEnterLinkView

class WorkspaceEnterLinkFragment : UstadBaseFragment(), WorkspaceEnterLinkView{

    private lateinit var mBinding: FragmentWorkSpaceEnterLinkBinding

    private var mPresenter: WorkspaceEnterLinkPresenter? = null

    override var workspaceLink: String? = null
        get() = field
        set(value) {
            field = value
            mBinding.workspaceLink = value
        }
    override var validLink: Boolean = false
        set(value) {
            mBinding.enabledButton = value
            mBinding.workspaceLinkView.isErrorEnabled = !value
            mBinding.workspaceLinkView.error = getString(R.string.invalid_url)
            field = value
        }
    override var progressVisible: Boolean = false
        set(value) {
            field = value
            mBinding.progressBar.visibility = if(value) View.VISIBLE else View.GONE
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentWorkSpaceEnterLinkBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.enabledButton = false
        }

        mPresenter = WorkspaceEnterLinkPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),this)
        mPresenter?.onCreate(savedInstanceState.toStringMap())

        mBinding.organisationLink.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mPresenter?.checkLinkValidity()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter = null
    }
}