package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.databinding.FragmentRegisterAgeRedirectBinding
import com.ustadmobile.core.controller.RegisterAgeRedirectPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.RegisterAgeRedirectView
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY

class RegisterAgeRedirectFragment() : UstadBaseFragment(), RegisterAgeRedirectView {

    private var mBinding: FragmentRegisterAgeRedirectBinding? = null

    private var mPresenter: RegisterAgeRedirectPresenter? = null

    override var dateOfBirth: Long
        get() = mBinding?.dateOfBirth ?: 0
        set(value) {
            mBinding?.dateOfBirth = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentRegisterAgeRedirectBinding.inflate(inflater, container, false).also {
            it.datePicker.maxDate = systemTimeInMillis()
        }
        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = RegisterAgeRedirectPresenter(requireContext(), arguments.toStringMap(),
            this, di).withViewLifecycle()
        mBinding?.presenter = mPresenter
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        mBinding = null
    }
}