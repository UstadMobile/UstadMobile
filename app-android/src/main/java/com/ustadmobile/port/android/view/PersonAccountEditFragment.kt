package com.ustadmobile.port.android.view

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


    private val usernameFilter = InputFilter { source, start, end, dest, dstart, dend ->

        if (source != null && blockCharacterSet.contains("" + source)) {
            ""
        } else if(source != null){
             source.toString().toLowerCase()
        }else null
    }

    override var currentPasswordError: String? = null
        set(value) {
            field = value
            handleInputError(mBinding?.currentPasswordTextinputlayout, value != null,value)
        }

    override var newPasswordError: String? = null
        set(value) {
            field = value
            handleInputError(mBinding?.newPasswordTextinputlayout, value != null,value)
        }
    override var confirmedPasswordError: String? = null
       set(value) {
            field = value
            handleInputError(mBinding?.confirmPasswordTextinputlayout, value != null,value)
        }
    override var noPasswordMatchError: String? = null
        set(value) {
            if(value != null){
                handleInputError(mBinding?.newPasswordTextinputlayout, true, value)
                handleInputError(mBinding?.confirmPasswordTextinputlayout, true, value)
            }
        }


    override var usernameError: String? = null
        set(value) {
            field = value
            handleInputError(mBinding?.usernameTextinputlayout, value != null,value)
        }

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

        mBinding?.accountUsernameText?.filters = arrayOf(usernameFilter)

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