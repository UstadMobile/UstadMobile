package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.toughra.ustadmobile.databinding.FragmentPersonAccountEditBinding
import com.ustadmobile.core.controller.PersonAccountEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonAccountEditView
import com.ustadmobile.core.view.PersonAccountEditView.Companion.BLOCK_CHARACTER_SET
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.util.ClearErrorTextWatcher


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
    override var usernameVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.usernameVisibility = if(value) View.VISIBLE else View.GONE
        }


    override var fieldsEnabled: Boolean = true
        set(value){
            super.fieldsEnabled = value
            field = value
        }

    override var entity: PersonWithAccount? = null
        set(value) {
            field = value
            mBinding?.person = value
            ustadFragmentTitle = value?.fullName()
        }

    override val mEditPresenter: UstadEditPresenter<*, PersonWithAccount>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView: View
        mBinding = FragmentPersonAccountEditBinding.inflate(inflater, container,
            false).also {
            rootView = it.root
        }

        mPresenter = PersonAccountEditPresenter(requireContext(), arguments.toStringMap(),
            this, di, viewLifecycleOwner).withViewLifecycle()

        mBinding?.currentPasswordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.currentPasswordError = null
        })

        mBinding?.newPasswordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.passwordError = null
            mBinding?.passwordConfirmError = null
        })


        mBinding?.confirmPasswordText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.passwordConfirmError = null
            mBinding?.passwordError = null
        })

        mBinding?.accountUsernameText?.addTextChangedListener(ClearErrorTextWatcher {
            mBinding?.usernameError = null
        })

        mBinding?.accountUsernameText?.filters = arrayOf(NO_CAPS_FILTER)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        if(mBinding?.person != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                    mBinding?.person?.firstNames + " " + mBinding?.person?.lastName
        }
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

        val NO_CAPS_FILTER = InputFilter {source, start, end, _, _, _ ->
            source.toString().lowercase().replace(" ", "");
        }

        val USERNAME_FILTER = InputFilter { source, start, end, _, _, _ ->

            val sb = StringBuilder()

            var changed = false
            for (i in start until end) {
                val c = source[i]
                if(!BLOCK_CHARACTER_SET.contains(c)) {
                    sb.append(c.toString().lowercase())
                }else {
                    changed = true
                }
            }

            if(!changed) {
                null
            } else if(source is Spanned) {
                SpannableString(sb).also {
                    TextUtils.copySpansFrom(source, start, end, null, it, 0)
                }
            }else {
                sb
            }
        }
    }
}