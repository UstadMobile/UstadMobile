package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.databinding.FragmentRegisterMinorWaitForParentBinding
import com.ustadmobile.core.controller.RegisterMinorWaitForParentPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.RegisterMinorWaitForParentView

class RegisterMinorWaitForParentFragment: UstadBaseFragment(), RegisterMinorWaitForParentView {

    var mBinding: FragmentRegisterMinorWaitForParentBinding? = null

    var mPresenter: RegisterMinorWaitForParentPresenter? = null

    override var username: String?
        get() = mBinding?.username
        set(value) {
            mBinding?.username = value
        }
    override var password: String?
        get() = mBinding?.password
        set(value) {
            mBinding?.password = value
        }

    override var parentContact: String?
        get() = mBinding?.parentContact
        set(value) {
            mBinding?.parentContact = value
        }

    override var passwordVisible: Boolean
        get() = mBinding?.passwordVisible ?: false
        set(value) {
            mBinding?.passwordVisible = value
            mBinding?.passwordToggle?.isSelected = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentRegisterMinorWaitForParentBinding.inflate(inflater, container, false)
        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = RegisterMinorWaitForParentPresenter(requireContext(), arguments.toStringMap(),
            this, di).withViewLifecycle()
        mBinding?.presenter = mPresenter
        mPresenter?.onCreate(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mPresenter = null
        mBinding = null
    }

}