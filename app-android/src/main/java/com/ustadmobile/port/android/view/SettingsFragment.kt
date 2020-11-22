package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.databinding.FragmentSettingsBinding
import com.ustadmobile.core.controller.SettingsPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SettingsView

class SettingsFragment : UstadBaseFragment(), SettingsView {

    internal var mPresenter: SettingsPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view:View
        val dataBinding = FragmentSettingsBinding.inflate(inflater, container, false).also {
            view = it.root
        }

        mPresenter = SettingsPresenter(requireContext(), arguments.toStringMap(),
                this, di)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        dataBinding.presenter = mPresenter
        dataBinding.fragment = this

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }

    fun handleClickNetworkNodeList() {
        mPresenter?.handleClickNetworkNodeList()
    }
}
