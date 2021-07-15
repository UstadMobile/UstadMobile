package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.databinding.FragmentParentalConsentManagementBinding
import com.ustadmobile.core.controller.ParentalConsentManagementPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ParentalConsentManagementView
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinWithMinorPerson
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.port.android.view.util.ClearErrorTextWatcher


interface ParentAccountLandingFragmentEventHandler {
    fun onClickConsent()
    fun onClickDoNotConsent()
    fun onClickChangeConsent()
}

class ParentalConsentManagementFragment: UstadEditFragment<PersonParentJoinWithMinorPerson>(), ParentalConsentManagementView, ParentAccountLandingFragmentEventHandler {

    private var mBinding: FragmentParentalConsentManagementBinding? = null

    private var mPresenter: ParentalConsentManagementPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, PersonParentJoinWithMinorPerson>?
        get() = mPresenter


    override var infoText: String?
        get() = mBinding?.infoText
        set(value) {
            mBinding?.infoText = value
        }

    override var siteTerms: SiteTerms?
        get() = mBinding?.siteTerms
        set(value) {
            mBinding?.siteTerms = value
        }

    override var relationshipFieldOptions: List<IdOption>?
        get() = mBinding?.relationshipFieldOptions
        set(value) {
            mBinding?.relationshipFieldOptions = value
        }

    override var relationshipFieldError: String?
        get() = mBinding?.relationshipFieldError
        set(value) {
            mBinding?.relationshipFieldError = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentParentalConsentManagementBinding.inflate(inflater, container, false).also { binding ->
            rootView = binding.root
            binding.eventHandler = this
            binding.relationshipValue.addTextChangedListener(ClearErrorTextWatcher {
                binding.relationshipFieldError = null
            })
        }

        mPresenter = ParentalConsentManagementPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()


        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //do nothing: this descends from edit fragment, but does not use the done checkbox
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onClickConsent() {
        entity?.also {
            it.ppjStatus = PersonParentJoin.STATUS_APPROVED
            mPresenter?.handleClickSave(it)
        }
    }

    override fun onClickDoNotConsent() {
        entity?.also {
            it.ppjStatus  = PersonParentJoin.STATUS_REJECTED
            mPresenter?.handleClickSave(it)
        }
    }

    override fun onClickChangeConsent() {
        entity?.also {
            it.ppjStatus = if(it.ppjStatus == PersonParentJoin.STATUS_APPROVED) {
                PersonParentJoin.STATUS_REJECTED
            }else {
                PersonParentJoin.STATUS_APPROVED
            }

            mPresenter?.handleClickSave(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.eventHandler = null
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: PersonParentJoinWithMinorPerson?
        get() = mBinding?.personParentJoin
        set(value) {
            mBinding?.personParentJoin = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }
}