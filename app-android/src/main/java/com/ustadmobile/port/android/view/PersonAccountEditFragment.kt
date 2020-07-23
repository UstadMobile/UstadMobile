package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPersonAccountEditBinding
import com.ustadmobile.core.controller.PersonAccountEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap


class PersonAccountEditFragment: UstadEditFragment<PersonWithAccount>(), PersonAccountEditView {

    private var mBinding: FragmentPersonAccountEditBinding? = null

    private var mPresenter: PersonAccountEditPresenter? = null


    override var currentPasswordRequiredErrorVisible: Boolean = false
        set(value) {
            field = value
            handleInputError(mBinding?.currentPasswordTextinputlayout,
                    value,getString(R.string.field_required_prompt))
        }

    override var newPasswordRequiredErrorVisible: Boolean = false
        set(value) {
            field = value
            handleInputError(mBinding?.newPasswordTextinputlayout,
                    value,getString(R.string.field_required_prompt))
        }
    override var confirmedPasswordRequiredErrorVisible: Boolean = false
       set(value) {
            field = value
            handleInputError(mBinding?.confirmPasswordTextinputlayout,
                    value,getString(R.string.field_required_prompt))
        }

    override var passwordDoNotMatchErrorVisible: Boolean = false
        set(value) {
            field = value
            handleInputError(mBinding?.newPasswordTextinputlayout,value,
                    getString(R.string.filed_password_no_match))
            handleInputError(mBinding?.confirmPasswordTextinputlayout,value,
                    getString(R.string.filed_password_no_match))
        }


    override var usernameRequiredErrorVisible: Boolean = false
        set(value) {
            field = value
            handleInputError(mBinding?.usernameTextinputlayout,
                    value,getString(R.string.field_required_prompt))
        }


    override fun showErrorMessage(message: String, isPasswordError: Boolean) {
        if(isPasswordError){
            handleInputError(mBinding?.currentPasswordTextinputlayout,true, message)
            return
        }
        mBinding?.errorText?.visibility =  View.VISIBLE
        mBinding?.errorText?.text = message

    }


    override var fieldsEnabled: Boolean = true

    override var entity: PersonWithAccount? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value
            if(viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
                (activity as? AppCompatActivity)?.supportActionBar?.title = value?.firstNames + " " + value?.lastName
            mBinding?.currentPasswordTextinputlayout?.isEnabled = value != null && !value.admin
        }

    override val mEditPresenter: UstadEditPresenter<*, PersonWithAccount>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentPersonAccountEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = PersonAccountEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        mBinding?.currentPasswordText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.currentPasswordTextinputlayout, false, null)
            }
        })

        mBinding?.newPasswordText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.newPasswordTextinputlayout, false, null)
            }
        })


        mBinding?.confirmPasswordText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.confirmPasswordTextinputlayout, false, null)
            }
        })

        mBinding?.accountUsernameText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.usernameTextinputlayout, false, null)
            }
        })
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if(mBinding?.person != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = mBinding?.person?.firstNames + " " + mBinding?.person?.lastName
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
}