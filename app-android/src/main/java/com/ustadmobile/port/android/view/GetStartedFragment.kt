package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.databinding.FragmentGetStartedBinding
import com.ustadmobile.core.controller.GetStartedPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.AccountGetStartedView


class GetStartedFragment : UstadBaseFragment(), AccountGetStartedView {

    private lateinit var mBinding: FragmentGetStartedBinding

    private var mPresenter: GetStartedPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentGetStartedBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = GetStartedPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),this)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter = null
    }

    override fun createNewWorkSpace() {

    }
}