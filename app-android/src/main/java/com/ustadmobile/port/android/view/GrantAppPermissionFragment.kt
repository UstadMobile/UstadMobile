package com.ustadmobile.port.android.view

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.toughra.ustadmobile.databinding.FragmentGrantAppPermissionBinding
import com.ustadmobile.port.android.presenter.GrantAppPermissionPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.GrantAppPermissionView

class GrantAppPermissionFragment: UstadBaseFragment(), GrantAppPermissionView {

    private var mPresenter: GrantAppPermissionPresenter? = null

    private var mBinding: FragmentGrantAppPermissionBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View
        mPresenter = GrantAppPermissionPresenter(requireContext(), requireArguments().toStringMap(),
            this, di).withViewLifecycle()
        mBinding = FragmentGrantAppPermissionBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.grantButton.setOnClickListener {
                mPresenter?.onClickApprove()
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
    }

    override fun onDestroyView() {
        mBinding = null
        mPresenter = null
        super.onDestroyView()
    }

}