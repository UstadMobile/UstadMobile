package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.databinding.FragmentWorkSpaceEnterLinkBinding
import com.ustadmobile.core.controller.WorkspaceEnterLinkPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.WorkspaceEnterLinkView

class WorkspaceEnterLinkFragment : UstadBaseFragment(), WorkspaceEnterLinkView{

    private lateinit var mBinding: FragmentWorkSpaceEnterLinkBinding

    private var mPresenter: WorkspaceEnterLinkPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentWorkSpaceEnterLinkBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = WorkspaceEnterLinkPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),this)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter = null
    }

    override var workspaceLink: String? = null
        get() = field
        set(value) {
            field = value
            mBinding.workspaceLink = value
        }
}