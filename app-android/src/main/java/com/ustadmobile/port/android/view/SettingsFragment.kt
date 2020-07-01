package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.databinding.FragmentSettingsBinding
import com.ustadmobile.core.controller.SettingsPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.GetStartedView
import com.ustadmobile.core.view.SettingsView

class SettingsFragment : UstadBaseFragment(), SettingsView {

    internal lateinit var mPresenter: SettingsPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        //val view = super.onCreateView(inflater, container, savedInstanceState)
        val view:View
        val dataBinding = FragmentSettingsBinding.inflate(inflater, container, false).also {
            view = it.root
        }

        mPresenter = SettingsPresenter(requireContext(), arguments.toStringMap(),
                this, UstadMobileSystemImpl.instance)
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState))

        dataBinding.presenter = mPresenter

        return view
    }

    override val viewContext: Any
        get() = requireContext()
}
