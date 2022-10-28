package com.ustadmobile.port.android.view

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentLogin2Binding
import com.ustadmobile.core.controller.Login2Presenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.Login2View


interface DroidAccountTest{

    fun addToAccountManager(username: String)

}

class Login2Fragment : UstadBaseFragment(), Login2View, DroidAccountTest {

    private var mBinding: FragmentLogin2Binding? = null

    private var mPresenter: Login2Presenter? = null

    override var isEmptyPassword: Boolean = false
        set(value) {
            field = false
            mBinding?.passwordView?.isErrorEnabled = value
            if(value){
                mBinding?.passwordView?.error = getString(R.string.field_required_prompt)
            }
        }

    override var errorMessage: String = ""
        set(value) {
            field = value
            mBinding?.loginErrorText?.visibility = View.VISIBLE
            mBinding?.loginErrorText?.text = value
        }

    override var versionInfo: String? = null
        set(value) {
            field = value
            mBinding?.versionInfo = versionInfo
        }

    override var isEmptyUsername: Boolean = false
        set(value) {
            field = false
            mBinding?.usernameView?.isErrorEnabled = value
            if(value){
               mBinding?.usernameView?.error = getString(R.string.field_required_prompt)
            }
        }

    override var inProgress: Boolean = false
        set(value) {
            mBinding?.buttonEnabled = !value
            mBinding?.fieldsEnabled = !value
            mBinding?.passwordView?.isErrorEnabled = !value
            field = value
            loading = inProgress
            if(value){
                mBinding?.loginErrorText?.visibility = View.GONE
            }
        }

    override var createAccountVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.createAccount?.visibility = if(value) View.VISIBLE else View.GONE
        }

    override var connectAsGuestVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.connectAsGuest?.visibility = if(value) View.VISIBLE else View.GONE
        }

    override var loginIntentMessage: String? = null
        set(value) {
            field = value
            mBinding?.intentMessage = value
        }

    override fun clearFields() {
        mBinding?.password = ""
        mBinding?.username = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View
        mBinding = FragmentLogin2Binding.inflate(inflater, container, false).also {
            rootView = it.root
            it.droidAccountTester = this
            it.buttonEnabled = true
            it.fieldsEnabled = true
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = Login2Presenter(requireContext(), arguments.toStringMap(),this,
            di).withViewLifecycle()
        mBinding?.presenter = mPresenter
        mPresenter?.onCreate(savedInstanceState.toStringMap())
    }



    override fun addToAccountManager(username: String) {


//        val accountManager = AccountManager.get(requireContext())
//        if(accountManager.addAccountExplicitly(Account(username, ACCOUNT_TYPE), "password",
//            null)) {
//            Toast.makeText(requireContext(), "Added account", Toast.LENGTH_LONG)
//        }else {
//            Toast.makeText(requireContext(), "Account add FAIL", Toast.LENGTH_LONG)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        mBinding = null
    }

    companion object {



    }

}