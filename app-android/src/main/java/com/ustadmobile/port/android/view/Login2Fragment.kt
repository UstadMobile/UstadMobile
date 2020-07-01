package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentLogin2Binding
import com.ustadmobile.core.controller.Login2Presenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.Login2View


class Login2Fragment : UstadBaseFragment(), Login2View {

    private lateinit var mBinding: FragmentLogin2Binding

    private var mPresenter: Login2Presenter? = null

    override var isEmptyPassword: Boolean = false
        set(value) {
            field = false
            mBinding.passwordView.isErrorEnabled = value
            if(value){
                mBinding.passwordView.error = getString(R.string.field_required_prompt)
            }
        }

    override var isEmptyUsername: Boolean = false
        set(value) {
            field = false
            mBinding.usernameView.isErrorEnabled = value
            if(value){
               mBinding.usernameView.error = getString(R.string.field_required_prompt)
            }
        }

    override var inProgress: Boolean = false
        set(value) {
            mBinding.buttonEnabled = !value
            mBinding.fieldsEnabled = !value
            field = value
        }
    override var createAccountVisible: Boolean = false
        set(value) {
            field = value
            mBinding.createAccount.visibility = if(value) View.VISIBLE else View.GONE
        }

    override var connectAsGuestVisible: Boolean = false
        set(value) {
            field = value
            mBinding.connectAsGuest.visibility = if(value) View.VISIBLE else View.GONE
        }

    override fun clearFields() {
        mBinding.password = ""
        mBinding.username = ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentLogin2Binding.inflate(inflater, container, false).also {
            rootView = it.root
            it.buttonEnabled = true
            it.fieldsEnabled = true
        }

        mPresenter = Login2Presenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),this,
                personRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext()).personDao)
        mPresenter?.onCreate(savedInstanceState.toStringMap())

        mBinding.presenter = mPresenter
        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter = null
    }

}