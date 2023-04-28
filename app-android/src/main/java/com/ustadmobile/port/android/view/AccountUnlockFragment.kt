package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.toughra.ustadmobile.databinding.FragmentAccountUnlockBinding
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.AccountUnlockView
import com.ustadmobile.port.android.presenter.AccountUnlockPresenter

interface AccountUnlockFragmentEventHandler {
    fun onClickUnlock()
}

class AccountUnlockFragment: UstadBaseFragment(

), AccountUnlockView, AccountUnlockFragmentEventHandler  {

    private var mPresenter: AccountUnlockPresenter? = null

    private var mBinding: FragmentAccountUnlockBinding? = null

    override var accountName: String?
        get() = mBinding?.accountNameText?.text?.toString()
        set(value) {
            mBinding?.accountNameText?.text = value
        }

    override var error: String?
        get() = mBinding?.loginErrorText?.text?.toString()
        set(value) {
            val textVal = value ?: ""
            if(mBinding?.loginErrorText?.text?.toString() != textVal) {
                mBinding?.loginErrorText?.text = textVal
                mBinding?.loginErrorText?.visibility = if(value != null) View.VISIBLE else View.INVISIBLE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentAccountUnlockBinding.inflate(inflater, container, false).also {
            mBinding = it
            mBinding?.personPassword?.addTextChangedListener {
                error = null
            }
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = AccountUnlockPresenter(requireContext(), arguments.toStringMap(),
            this, di).withViewLifecycle()
        mBinding?.mEventHandler = this
        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onClickUnlock() {
        val password = mBinding?.personPassword?.text?.toString()?.trim() ?: return
        mPresenter?.onClickUnlock(password)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        mBinding = null
    }

}