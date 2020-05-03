package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPersonDetailBinding
import com.ustadmobile.core.controller.PersonDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.PersonDetailViewField
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails

interface PersonDetailFragmentEventHandler {

}

class PersonDetailFragment: UstadDetailFragment<PersonWithDisplayDetails>(), PersonDetailView, PersonDetailFragmentEventHandler {

    private var mBinding: FragmentPersonDetailBinding? = null

    private var mPresenter: PersonDetailPresenter? = null

    override var presenterFields: DoorLiveData<PersonDetailViewField>? = null
        get() = field
        set(value) {
            field = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentPersonDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = PersonDetailPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()

        //TODO: Set title here
    }

    override var entity: PersonWithDisplayDetails? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value
        }

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            mBinding?.editButtonMode = value
            field = value
        }

}