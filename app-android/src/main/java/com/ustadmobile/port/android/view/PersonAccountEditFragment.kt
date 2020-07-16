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
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap


class PersonAccountEditFragment: UstadEditFragment<Person>(), PersonAccountEditView {

    private var mBinding: FragmentPersonAccountEditBinding? = null

    private var mPresenter: PersonAccountEditPresenter? = null

    override var currentPassword: String? = null
        get() = mBinding?.currentPassword

    override var newPassword: String? = null
        get() = mBinding?.newPassword

    override var newPasswordRequiredErrorVisible: Boolean = false
        set(value) {
            field = value
            handleInputError(mBinding?.newPasswordTextinputlayout,
                    value,getString(R.string.field_required_prompt))
        }

    override var currentPasswordRequiredErrorVisible: Boolean = false
        set(value) {
            field = value
            handleInputError(mBinding?.currentPasswordTextinputlayout,
                    value,getString(R.string.field_required_prompt))
        }

    override var usernameRequiredErrorVisible: Boolean = false
        set(value) {
            field = value
            handleInputError(mBinding?.usernameTextinputlayout,
                    value,getString(R.string.field_required_prompt))
        }
    override var errorMessage: String? = null
        set(value) {
            field = value
            mBinding?.errorText?.visibility = if(value != null) View.VISIBLE else View.GONE
            mBinding?.errorText?.text = value
        }

    override fun clearFields() {
        mBinding?.currentPassword = ""
        mBinding?.newPassword = ""
    }


    override var fieldsEnabled: Boolean = true

    override var entity: Person? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value
            if(viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
                (activity as? AppCompatActivity)?.supportActionBar?.title = value?.firstNames + " " + value?.lastName
        }

    override val mEditPresenter: UstadEditPresenter<*, Person>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentPersonAccountEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = PersonAccountEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        mBinding?.accountNewPasswordText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.newPasswordTextinputlayout, false, null)
            }
        })

        mBinding?.accountCurrentPasswordText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handleInputError(mBinding?.currentPasswordTextinputlayout, false, null)
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