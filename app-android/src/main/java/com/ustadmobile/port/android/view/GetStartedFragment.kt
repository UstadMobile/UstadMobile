package com.ustadmobile.port.android.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.databinding.FragmentGetStartedBinding
import com.ustadmobile.core.controller.GetStartedPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.GetStartedView


class GetStartedFragment : UstadBaseFragment(), GetStartedView {

    private var mBinding: FragmentGetStartedBinding? = null

    private var mPresenter: GetStartedPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentGetStartedBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = GetStartedPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),this,
            di)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        mBinding?.presenter = mPresenter
        return rootView
    }

    override fun createNewWorkSpace() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ustadmobile.com/lms/hosting/"))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        mBinding = null
    }
}