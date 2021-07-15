package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.databinding.FragmentSettingsBinding
import com.ustadmobile.core.controller.SettingsPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SettingsView

class SettingsFragment : UstadBaseFragment(), SettingsView {

    var mPresenter: SettingsPresenter? = null

    private var mBinding: FragmentSettingsBinding? = null

    override var workspaceSettingsVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.workspaceSettingsVisible = value
        }
    override var holidayCalendarVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.holidayCalendarVisible = value
        }

    override var reasonLeavingVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.reasonLeavingVisible = value
        }

    override var langListVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.langListVisible = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view:View
        mBinding = FragmentSettingsBinding.inflate(inflater, container, false).also {
            view = it.root
        }

        mPresenter = SettingsPresenter(requireContext(), arguments.toStringMap(),
                this, di).withViewLifecycle()
        mPresenter?.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState))

        mBinding?.presenter = mPresenter

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
    }

}
