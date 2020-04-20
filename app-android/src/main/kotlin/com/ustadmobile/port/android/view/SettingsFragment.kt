package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.databinding.ActivitySettings2Binding
import com.ustadmobile.core.controller.SettingsPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SettingsView

class SettingsFragment : UstadBaseFragment(), SettingsView {

    internal lateinit var mPresenter: SettingsPresenter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val dataBinding = DataBindingUtil.setContentView<ActivitySettings2Binding>(activity?,
//                R.layout.activity_settings2)
//
//        mPresenter = SettingsPresenter(this,
//                UMAndroidUtil.bundleToMap(activity?.intent?.extras), this)
//        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))
//
//        dataBinding.presenter = mPresenter
//
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        //val view = super.onCreateView(inflater, container, savedInstanceState)
        val view:View
        val dataBinding = ActivitySettings2Binding.inflate(inflater, container, false).also {
            view = it.root
        }

        mPresenter = SettingsPresenter(requireContext(), arguments.toStringMap(),
                this, UstadMobileSystemImpl.instance)
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState))

        dataBinding.presenter = mPresenter

        return view
    }


    override fun finish() {
        //TODO:
    }

    override val viewContext: Any
        get() = requireContext()
}
