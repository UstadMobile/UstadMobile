package com.ustadmobile.port.android.view

import android.R.attr
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.toughra.ustadmobile.databinding.FragmentPersonAccountEditBinding
import com.ustadmobile.core.controller.PersonAccountEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.core.view.PersonAccountEditView.Companion.blockCharacterSet
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap


class PersonAccountEditFragment: UstadEditFragment<PersonWithAccount>(), PersonAccountEditView {

    private var mBinding: FragmentPersonAccountEditBinding? = null

    private var mPresenter: PersonAccountEditPresenter? = null

    override var currentPasswordError: String?
        set(value) {
            mBinding?.currentPasswordError = value
        }
        get() = mBinding?.currentPasswordError


    override var newPasswordError: String?
        set(value) {
            mBinding?.passwordError = value
        }
        get() = mBinding?.passwordError


    override var confirmedPasswordError: String?
        set(value) {
            mBinding?.passwordConfirmError = value
        }
        get() = mBinding?.passwordConfirmError

    override var noPasswordMatchError: String?

        set(value) {
            mBinding?.passwordError = value
            mBinding?.passwordConfirmError = value
        }
        get() = mBinding?.passwordError


    override var usernameError: String?
        set(value) {
            mBinding?.usernameError = value
        }
        get() = mBinding?.usernameError


    override var errorMessage: String? = null
        set(value) {
            field = value
            mBinding?.errorText?.visibility =  View.VISIBLE
            mBinding?.errorText?.text = value
        }
    override var currentPasswordVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.currentPasswordVisibility = if(value) View.VISIBLE else View.GONE
        }


    override var fieldsEnabled: Boolean = true

    override var entity: PersonWithAccount? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value
            if(view != null && viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
                (activity as? AppCompatActivity)?.supportActionBar?.title = value?.firstNames + " " + value?.lastName
            mBinding?.currentPasswordTextinputlayout?.isEnabled = value != null && !value.admin
        }

    override val mEditPresenter: UstadEditPresenter<*, PersonWithAccount>?
        get() = mPresenter

    class ClearErrorTextWatcher(private val onTextFunction: () -> Unit ):TextWatcher{
        override fun afterTextChanged(p0: Editable?) {

        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            onTextFunction()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentPersonAccountEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = PersonAccountEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        mBinding?.currentPasswordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.currentPasswordError = null
        })

        mBinding?.newPasswordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.passwordError = null
        })


        mBinding?.confirmPasswordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.passwordConfirmError = null
        })

        mBinding?.accountUsernameText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.usernameError = null
        })

        mBinding?.accountUsernameText?.filters = arrayOf(USERNAME_FILTER)

        return rootView
    }


    override fun onResume() {
        super.onResume()
        if(mBinding?.person != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                    mBinding?.person?.firstNames + " " + mBinding?.person?.lastName
        }
    }

    private fun handleInputError(inputView: TextInputLayout?,error: Boolean,hint: String?){
        inputView?.isErrorEnabled = error
        inputView?.error = if(error) hint else null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mBinding = null
        mPresenter = null
        entity = null
    }

    companion object{
        val USERNAME_FILTER = InputFilter { source, start, end, dest, dstart, dend ->

            val sb = StringBuilder(attr.end - start)

            for (i in start until end) {
                val c = source[i]
                if (blockCharacterSet.contains(c)) {
                    ""
                }else if(c != null || !c.equals("")) {
                    sb.append(c.toString().toLowerCase())
                } else {
                    null
                }
            }
            sb.toString()
        }
    }
}